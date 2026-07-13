"""
search_service/retriever.py
混合检索：Qdrant 向量检索 + ES BM25 关键词检索，结果合并去重。
第一阶段：简单 score 归一化合并（RRF 融合）。
"""
from __future__ import annotations

from loguru import logger
from shared.config import get_settings
from shared.embedder import embed_query
from shared.qdrant_store import get_qdrant_client, user_collection
from shared.es_client import get_es_client, user_index


def _vector_search(user_id: str, query_vec: list[float], top_k: int) -> list[dict]:
    """Qdrant 向量检索，返回 top_k 个 chunk。"""
    try:
        client = get_qdrant_client()
        col = user_collection(user_id)
        results = client.search(
            collection_name=col,
            query_vector=query_vec,
            limit=top_k,
        )
        return [
            {
                "note_id":    r.payload.get("note_id", ""),
                "file_id":    r.payload.get("file_id", ""),
                "chunk_text": r.payload.get("chunk_text", ""),
                "chunk_idx":  r.payload.get("chunk_idx", 0),
                "score":      r.score,
                "source":     "vector",
            }
            for r in results
        ]
    except Exception as e:
        logger.warning("向量检索失败 user_id={} error={}", user_id, e)
        return []


def _bm25_search(user_id: str, query: str, top_k: int) -> list[dict]:
    """ES BM25 检索，返回 top_k 个 chunk。"""
    try:
        es = get_es_client()
        idx = user_index(user_id)
        resp = es.search(
            index=idx,
            body={
                "query": {"match": {"chunk_text": {"query": query, "operator": "or"}}},
                "size": top_k,
            },
        )
        return [
            {
                "note_id":    hit["_source"].get("note_id", ""),
                "file_id":    hit["_source"].get("file_id", ""),
                "chunk_text": hit["_source"].get("chunk_text", ""),
                "chunk_idx":  hit["_source"].get("chunk_idx", 0),
                "score":      hit["_score"],
                "source":     "bm25",
            }
            for hit in resp["hits"]["hits"]
        ]
    except Exception as e:
        logger.warning("BM25 检索失败 user_id={} error={}", user_id, e)
        return []


def _rrf_merge(vec_results: list[dict], bm25_results: list[dict], k: int = 60) -> list[dict]:
    """
    Reciprocal Rank Fusion 合并两路结果。
    chunk 唯一标识：(file_id, chunk_idx)
    """
    scores: dict[tuple, float] = {}
    data:   dict[tuple, dict]  = {}

    for rank, r in enumerate(vec_results):
        key = (r["file_id"], r["chunk_idx"])
        scores[key] = scores.get(key, 0.0) + 1.0 / (k + rank + 1)
        data[key] = r

    for rank, r in enumerate(bm25_results):
        key = (r["file_id"], r["chunk_idx"])
        scores[key] = scores.get(key, 0.0) + 1.0 / (k + rank + 1)
        if key not in data:
            data[key] = r

    merged = sorted(scores.keys(), key=lambda k: scores[k], reverse=True)
    result = []
    for key in merged:
        item = data[key].copy()
        item["score"] = scores[key]
        result.append(item)
    return result


def hybrid_retrieve(user_id: str, query: str, top_k: int) -> list[dict]:
    """
    混合检索主入口，返回 rerank 后的 top_k 个 chunk（含 note_id/chunk_text/score）。
    """
    settings = get_settings()
    retrieve_k = settings.retrieve_top_k
    rerank_k = min(settings.rerank_top_k, top_k)

    query_vec = embed_query(query)
    vec_results  = _vector_search(user_id, query_vec, retrieve_k)
    bm25_results = _bm25_search(user_id, query, retrieve_k)

    merged = _rrf_merge(vec_results, bm25_results)
    logger.debug(
        "混合检索完成 user_id={} vec={} bm25={} merged={}",
        user_id, len(vec_results), len(bm25_results), len(merged),
    )
    return merged[:rerank_k]
