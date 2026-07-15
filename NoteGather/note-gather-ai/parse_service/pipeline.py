"""文件解析、索引和结果事件发布流水线。"""
from __future__ import annotations

import io

from langchain_text_splitters import RecursiveCharacterTextSplitter
from loguru import logger

from parse_service.chunk_identity import stable_chunk_id
from parse_service.producer import publish_parse_result
from shared.config import get_settings
from shared.embedder import embed_texts
from shared.es_client import ensure_index, get_es_client, user_index
from shared.minio_client import download_file
from shared.qdrant_store import ensure_collection, get_qdrant_client, user_collection

try:
    from elasticsearch.helpers import bulk
    from qdrant_client.models import FieldCondition, Filter, MatchValue, PointStruct

    _HAS_INDEX_CLIENTS = True
except ImportError:
    _HAS_INDEX_CLIENTS = False


class ParseRetryableError(RuntimeError):
    """本次解析已失败但仍可由 RocketMQ 重投。"""


def _extract_text(content: bytes, file_type: str) -> str:
    """从 PDF、TXT 或 Markdown 原始内容提取纯文本。"""
    if file_type.upper() == "PDF":
        from pdfminer.high_level import extract_text as pdf_extract

        return pdf_extract(io.BytesIO(content))
    return content.decode("utf-8", errors="replace")


def run_parse_pipeline(
    *,
    file_id: str,
    parse_task_id: str,
    user_id: str,
    note_id: str,
    note_title: str,
    object_key: str,
    bucket: str,
    file_type: str,
    attempt_count: int,
) -> None:
    """执行一次解析；可重试错误会发布中间失败状态后抛出。"""
    try:
        chunk_count = _parse_and_index(
            file_id=file_id,
            user_id=user_id,
            note_id=note_id,
            note_title=note_title,
            object_key=object_key,
            bucket=bucket,
            file_type=file_type,
        )
    except Exception as error:
        _handle_parse_error(
            file_id=file_id,
            parse_task_id=parse_task_id,
            note_id=note_id,
            attempt_count=attempt_count,
            error=error,
        )
        return
    _send_parse_done(file_id, parse_task_id, note_id, chunk_count, attempt_count)


def _parse_and_index(
    *,
    file_id: str,
    user_id: str,
    note_id: str,
    note_title: str,
    object_key: str,
    bucket: str,
    file_type: str,
) -> int:
    settings = get_settings()
    logger.info("开始解析 file_id={} file_type={}", file_id, file_type)
    content = download_file(bucket, object_key)
    text = _extract_text(content, file_type)
    if not text.strip():
        raise ValueError("提取文本为空")
    chunks = RecursiveCharacterTextSplitter(
        chunk_size=settings.chunk_size,
        chunk_overlap=settings.chunk_overlap,
    ).split_text(text)
    if not chunks:
        raise ValueError("文本分块为空")
    vectors = embed_texts(chunks)
    if len(vectors) != len(chunks):
        raise ValueError("向量数量与文本分块数量不一致")
    _replace_indexes(file_id, user_id, note_id, note_title, chunks, vectors)
    logger.info("解析完成 file_id={} chunks={}", file_id, len(chunks))
    return len(chunks)


def _replace_indexes(
    file_id: str,
    user_id: str,
    note_id: str,
    note_title: str,
    chunks: list[str],
    vectors: list[list[float]],
) -> None:
    if not _HAS_INDEX_CLIENTS:
        raise RuntimeError("Qdrant 或 Elasticsearch 客户端依赖未安装")
    _replace_qdrant(file_id, user_id, note_id, note_title, chunks, vectors)
    _replace_elasticsearch(file_id, user_id, note_id, note_title, chunks)


def _replace_qdrant(
    file_id: str,
    user_id: str,
    note_id: str,
    note_title: str,
    chunks: list[str],
    vectors: list[list[float]],
) -> None:
    collection_name = user_collection(user_id)
    ensure_collection(collection_name)
    client = get_qdrant_client()
    client.delete(
        collection_name=collection_name,
        points_selector=Filter(
            must=[FieldCondition(key="file_id", match=MatchValue(value=file_id))]
        ),
    )
    points = [
        PointStruct(
            id=stable_chunk_id(file_id, index),
            vector=vectors[index],
            payload={
                "file_id": file_id,
                "note_id": note_id,
                "note_title": note_title,
                "user_id": user_id,
                "chunk_idx": index,
                "chunk_text": chunks[index],
            },
        )
        for index in range(len(chunks))
    ]
    client.upsert(collection_name=collection_name, points=points, wait=True)


def _replace_elasticsearch(
    file_id: str,
    user_id: str,
    note_id: str,
    note_title: str,
    chunks: list[str],
) -> None:
    index_name = user_index(user_id)
    ensure_index(index_name)
    client = get_es_client()
    client.delete_by_query(
        index=index_name,
        query={"term": {"file_id": file_id}},
        conflicts="proceed",
        refresh=False,
    )
    actions = [
        {
            "_index": index_name,
            "_id": f"{file_id}_{index}",
            "_source": {
                "note_id": note_id,
                "note_title": note_title,
                "file_id": file_id,
                "user_id": user_id,
                "chunk_text": chunks[index],
                "chunk_idx": index,
            },
        }
        for index in range(len(chunks))
    ]
    bulk(client, actions, refresh="wait_for")


def _handle_parse_error(
    *,
    file_id: str,
    parse_task_id: str,
    note_id: str,
    attempt_count: int,
    error: Exception,
) -> None:
    settings = get_settings()
    final_failed = attempt_count >= settings.parse_max_attempts
    logger.exception(
        "解析失败 file_id={} task_id={} attempt={} final={}",
        file_id,
        parse_task_id,
        attempt_count,
        final_failed,
    )
    _send_parse_failed(file_id, parse_task_id, note_id, str(error), attempt_count, final_failed)
    if not final_failed:
        raise ParseRetryableError(f"解析失败，等待第 {attempt_count + 1} 次尝试") from error


def _send_parse_done(
    file_id: str,
    parse_task_id: str,
    note_id: str,
    chunk_count: int,
    attempt_count: int,
) -> None:
    publish_parse_result(
        "PARSE_DONE",
        {
            "fileId": file_id,
            "parseTaskId": parse_task_id,
            "noteId": note_id,
            "status": "DONE",
            "chunkCount": chunk_count,
            "errorMsg": None,
            "attemptCount": attempt_count,
            "finalFailed": False,
        },
    )


def _send_parse_failed(
    file_id: str,
    parse_task_id: str,
    note_id: str,
    error_msg: str,
    attempt_count: int,
    final_failed: bool,
) -> None:
    publish_parse_result(
        "PARSE_FAILED",
        {
            "fileId": file_id,
            "parseTaskId": parse_task_id,
            "noteId": note_id,
            "status": "FAILED",
            "chunkCount": 0,
            "errorMsg": error_msg,
            "attemptCount": attempt_count,
            "finalFailed": final_failed,
        },
    )
