"""
parse_service/pipeline.py
文档解析流水线：
  下载文件 → 文本抽取 → 分块 → BGE-M3 嵌入 → 写入 Qdrant + ES
"""
from __future__ import annotations

from loguru import logger
from langchain_text_splitters import RecursiveCharacterTextSplitter

from shared.config import get_settings
from shared.embedder import embed_texts
from shared.minio_client import download_file
from shared.qdrant_store import ensure_collection, user_collection
from shared.es_client import ensure_index, user_index

try:
    from qdrant_client.models import PointStruct
    from qdrant_client import QdrantClient
    from shared.qdrant_store import get_qdrant_client
    _HAS_QDRANT = True
except ImportError:
    _HAS_QDRANT = False

try:
    from shared.es_client import get_es_client
    _HAS_ES = True
except ImportError:
    _HAS_ES = False


def _extract_text(content: bytes, file_type: str) -> str:
    """从原始字节抽取纯文本。支持 PDF / TXT / MD。"""
    ft = file_type.upper()
    if ft == "PDF":
        from pdfminer.high_level import extract_text as pdf_extract
        import io
        return pdf_extract(io.BytesIO(content))
    # TXT / MD / 其他，直接 UTF-8 解码
    return content.decode("utf-8", errors="replace")


def run_parse_pipeline(
    file_id: str,
    user_id: str,
    note_id: str,
    object_key: str,
    bucket: str,
    file_type: str,
) -> None:
    """
    完整解析流水线入口（同步，在消费者线程中执行）。
    成功后通过 RocketMQ 发布 PARSE_DONE，失败发布 PARSE_FAILED。
    """
    settings = get_settings()
    logger.info("开始解析 file_id={} file_type={}", file_id, file_type)

    try:
        # 1. 下载文件
        content = download_file(bucket, object_key)
        logger.debug("文件下载完成 file_id={} size={}", file_id, len(content))

        # 2. 文本抽取
        text = _extract_text(content, file_type)
        if not text.strip():
            raise ValueError("抽取文本为空")

        # 3. 分块
        splitter = RecursiveCharacterTextSplitter(
            chunk_size=settings.chunk_size,
            chunk_overlap=settings.chunk_overlap,
        )
        chunks: list[str] = splitter.split_text(text)
        logger.debug("分块完成 file_id={} chunks={}", file_id, len(chunks))

        # 4. 向量嵌入
        vectors = embed_texts(chunks)

        # 5. 写入 Qdrant
        if _HAS_QDRANT:
            col = user_collection(user_id)
            ensure_collection(col)
            client: QdrantClient = get_qdrant_client()
            points = [
                PointStruct(
                    id=abs(hash(f"{file_id}_{i}")) % (2 ** 63),  # 确定性 uint64 ID
                    vector=vectors[i],
                    payload={
                        "file_id": file_id,
                        "note_id": note_id,
                        "user_id": user_id,
                        "chunk_idx": i,
                        "chunk_text": chunks[i],
                    },
                )
                for i in range(len(chunks))
            ]
            client.upsert(collection_name=col, points=points)
            logger.debug("Qdrant 写入完成 file_id={} points={}", file_id, len(points))

        # 6. 写入 ES（BM25 索引）
        if _HAS_ES:
            idx = user_index(user_id)
            ensure_index(idx)
            es = get_es_client()
            from elasticsearch.helpers import bulk
            actions = [
                {
                    "_index": idx,
                    "_id": f"{file_id}_{i}",
                    "_source": {
                        "note_id": note_id,
                        "file_id": file_id,
                        "user_id": user_id,
                        "chunk_text": chunks[i],
                        "chunk_idx": i,
                    },
                }
                for i in range(len(chunks))
            ]
            bulk(es, actions)
            logger.debug("ES 写入完成 file_id={} docs={}", file_id, len(actions))

        logger.info("解析完成 file_id={} chunks={}", file_id, len(chunks))
        _send_parse_done(file_id, note_id, len(chunks))

    except Exception as e:
        logger.exception("解析失败 file_id={} error={}", file_id, e)
        _send_parse_failed(file_id, note_id, str(e))


def _send_parse_done(file_id: str, note_id: str, chunk_count: int) -> None:
    """发布 PARSE_DONE 消息到 Java 服务（可选，走 RocketMQ）。"""
    # TODO: 使用 rocketmq Producer 发送 ParseDoneMessage JSON
    logger.info("TODO: 发送 PARSE_DONE file_id={} chunks={}", file_id, chunk_count)


def _send_parse_failed(file_id: str, note_id: str, error_msg: str) -> None:
    """发布 PARSE_FAILED 消息。"""
    logger.info("TODO: 发送 PARSE_FAILED file_id={} error={}", file_id, error_msg)
