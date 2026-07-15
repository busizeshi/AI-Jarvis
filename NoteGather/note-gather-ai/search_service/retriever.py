"""Hybrid retrieval backed by Qdrant, Elasticsearch, and RRF."""
from __future__ import annotations

from typing import Any, Literal, TypedDict

from elastic_transport import ConnectionError as ElasticsearchConnectionError
from elastic_transport import ConnectionTimeout
from elasticsearch import ApiError
from loguru import logger
from qdrant_client.http.exceptions import ResponseHandlingException, UnexpectedResponse

from shared.config import get_settings
from shared.embedder import embed_query
from shared.es_client import get_es_client, user_index
from shared.qdrant_store import get_qdrant_client, user_collection

RRF_RANK_CONSTANT = 60


class RetrievalHit(TypedDict):
    note_id: str
    file_id: str
    note_title: str
    chunk_text: str
    chunk_idx: int
    score: float
    source: Literal["vector", "bm25"]


def _normalise_hit(payload: dict[str, Any], score: float, source: Literal["vector", "bm25"]) -> RetrievalHit | None:
    note_id = str(payload.get("note_id") or "").strip()
    file_id = str(payload.get("file_id") or "").strip()
    note_title = str(payload.get("note_title") or "").strip()
    chunk_text = str(payload.get("chunk_text") or "").strip()
    if not note_id or not file_id or not note_title or not chunk_text:
        logger.warning("Skip incomplete retrieval hit source={} note_id={} file_id={}", source, note_id, file_id)
        return None
    try:
        chunk_idx = int(payload.get("chunk_idx", 0))
    except (TypeError, ValueError):
        logger.warning("Skip retrieval hit with invalid chunk index source={} file_id={}", source, file_id)
        return None
    return {
        "note_id": note_id,
        "file_id": file_id,
        "note_title": note_title,
        "chunk_text": chunk_text,
        "chunk_idx": chunk_idx,
        "score": float(score),
        "source": source,
    }


def _vector_search(user_id: str, query_vec: list[float], top_k: int) -> list[RetrievalHit]:
    try:
        results = get_qdrant_client().search(
            collection_name=user_collection(user_id), query_vector=query_vec, limit=top_k
        )
    except (OSError, TimeoutError, ResponseHandlingException, UnexpectedResponse) as error:
        logger.warning("Vector retrieval unavailable user_id={} error={}", user_id, error)
        return []

    hits: list[RetrievalHit] = []
    for result in results:
        hit = _normalise_hit(result.payload or {}, result.score, "vector")
        if hit is not None:
            hits.append(hit)
    return hits


def _bm25_search(user_id: str, query: str, top_k: int) -> list[RetrievalHit]:
    try:
        response = get_es_client().search(
            index=user_index(user_id),
            body={
                "query": {"match": {"chunk_text": {"query": query, "operator": "or"}}},
                "size": top_k,
            },
        )
    except (ApiError, ElasticsearchConnectionError, ConnectionTimeout, OSError, TimeoutError) as error:
        logger.warning("BM25 retrieval unavailable user_id={} error={}", user_id, error)
        return []

    hits: list[RetrievalHit] = []
    for result in response.get("hits", {}).get("hits", []):
        hit = _normalise_hit(result.get("_source", {}), result.get("_score") or 0.0, "bm25")
        if hit is not None:
            hits.append(hit)
    return hits


def _rrf_merge(vector_hits: list[RetrievalHit], bm25_hits: list[RetrievalHit]) -> list[RetrievalHit]:
    scores: dict[tuple[str, str, int], float] = {}
    hits: dict[tuple[str, str, int], RetrievalHit] = {}
    for ranked_hits in (vector_hits, bm25_hits):
        for rank, hit in enumerate(ranked_hits, start=1):
            key = (hit["note_id"], hit["file_id"], hit["chunk_idx"])
            scores[key] = scores.get(key, 0.0) + 1.0 / (RRF_RANK_CONSTANT + rank)
            hits.setdefault(key, hit)

    merged: list[RetrievalHit] = []
    for key in sorted(scores, key=scores.get, reverse=True):
        hit = hits[key].copy()
        hit["score"] = scores[key]
        merged.append(hit)
    return merged


def hybrid_retrieve(user_id: str, query: str, top_k: int) -> list[RetrievalHit]:
    """Return user-isolated RRF results with citation fields ready for persistence."""
    if not user_id or not query.strip():
        return []

    settings = get_settings()
    result_limit = min(settings.rerank_top_k, max(top_k, 1))
    query_vector = embed_query(query)
    vector_hits = _vector_search(user_id, query_vector, settings.retrieve_top_k)
    bm25_hits = _bm25_search(user_id, query, settings.retrieve_top_k)
    merged = _rrf_merge(vector_hits, bm25_hits)
    logger.debug(
        "Hybrid retrieval completed user_id={} vector={} bm25={} merged={}",
        user_id,
        len(vector_hits),
        len(bm25_hits),
        len(merged),
    )
    return merged[:result_limit]
