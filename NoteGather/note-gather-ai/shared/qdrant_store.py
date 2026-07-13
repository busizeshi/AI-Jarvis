"""
shared/qdrant_client.py
Qdrant 向量库连接封装（单例）。
Collection 命名规则：user_{user_id}
"""
from __future__ import annotations

import threading
from qdrant_client import QdrantClient
from qdrant_client.models import Distance, VectorParams
from loguru import logger
from shared.config import get_settings

_lock = threading.Lock()
_client: QdrantClient | None = None

VECTOR_SIZE = 1024  # BGE-M3 稠密向量维度


def get_qdrant_client() -> QdrantClient:
    global _client
    if _client is None:
        with _lock:
            if _client is None:
                settings = get_settings()
                _client = QdrantClient(
                    host=settings.qdrant_host,
                    port=settings.qdrant_port,
                    grpc_port=settings.qdrant_grpc_port,
                    prefer_grpc=settings.qdrant_prefer_grpc,
                )
                logger.info(
                    "Qdrant 已连接 host={} port={}",
                    settings.qdrant_host,
                    settings.qdrant_port,
                )
    return _client


def ensure_collection(collection_name: str) -> None:
    """如果 collection 不存在则自动创建。"""
    client = get_qdrant_client()
    existing = {c.name for c in client.get_collections().collections}
    if collection_name not in existing:
        client.create_collection(
            collection_name=collection_name,
            vectors_config=VectorParams(size=VECTOR_SIZE, distance=Distance.COSINE),
        )
        logger.info("已创建 Qdrant collection={}", collection_name)


def user_collection(user_id: str) -> str:
    return f"user_{user_id}"
