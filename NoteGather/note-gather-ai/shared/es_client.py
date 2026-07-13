"""
shared/es_client.py
Elasticsearch 连接封装（单例）。
Index 命名规则：note_chunks_{user_id}
"""
from __future__ import annotations

import threading
from elasticsearch import Elasticsearch
from loguru import logger
from shared.config import get_settings

_lock = threading.Lock()
_client: Elasticsearch | None = None


def get_es_client() -> Elasticsearch:
    global _client
    if _client is None:
        with _lock:
            if _client is None:
                settings = get_settings()
                hosts = [
                    {
                        "scheme": settings.es_scheme,
                        "host": settings.es_host,
                        "port": settings.es_port,
                    }
                ]
                kwargs: dict = {}
                if settings.es_username:
                    kwargs["basic_auth"] = (settings.es_username, settings.es_password)
                _client = Elasticsearch(hosts, **kwargs)
                logger.info(
                    "Elasticsearch 已连接 {}://{}:{}", settings.es_scheme, settings.es_host, settings.es_port
                )
    return _client


# Index 名称规则
def user_index(user_id: str) -> str:
    return f"note_chunks_{user_id}"


# 索引映射（首次写入时创建）
_INDEX_MAPPING = {
    "settings": {"number_of_shards": 1, "number_of_replicas": 0},
    "mappings": {
        "properties": {
            "note_id":    {"type": "keyword"},
            "file_id":    {"type": "keyword"},
            "user_id":    {"type": "keyword"},
            "chunk_text": {"type": "text", "analyzer": "ik_max_word", "search_analyzer": "ik_smart"},
            "note_title": {"type": "keyword"},
            "chunk_idx":  {"type": "integer"},
        }
    },
}


def ensure_index(index_name: str) -> None:
    """如果索引不存在则自动创建（含 IK 分词映射）。"""
    client = get_es_client()
    if not client.indices.exists(index=index_name):
        client.indices.create(index=index_name, body=_INDEX_MAPPING)
        logger.info("已创建 ES 索引 index={}", index_name)
