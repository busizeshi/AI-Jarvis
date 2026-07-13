"""
shared/minio_client.py
MinIO 对象存储客户端封装，供 parse_service 下载待解析文件。
"""
from __future__ import annotations

import io
import threading
from minio import Minio
from loguru import logger
from shared.config import get_settings

_lock = threading.Lock()
_client: Minio | None = None


def get_minio_client() -> Minio:
    global _client
    if _client is None:
        with _lock:
            if _client is None:
                settings = get_settings()
                _client = Minio(
                    settings.minio_endpoint,
                    access_key=settings.minio_access_key,
                    secret_key=settings.minio_secret_key,
                    secure=settings.minio_secure,
                )
                logger.info("MinIO 已连接 endpoint={}", settings.minio_endpoint)
    return _client


def download_file(bucket: str, object_key: str) -> bytes:
    """从 MinIO 下载对象，返回字节内容。"""
    client = get_minio_client()
    response = client.get_object(bucket, object_key)
    try:
        return response.read()
    finally:
        response.close()
        response.release_conn()
