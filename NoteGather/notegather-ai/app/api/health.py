"""健康检查 API"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Dict, Any
import asyncio

from minio import Minio
from qdrant_client import QdrantClient
from neo4j import AsyncGraphDatabase
import redis.asyncio as aioredis
import aiomysql

from shared.config import Settings

router = APIRouter()


class HealthResponse(BaseModel):
    status: str
    service: str
    version: str


class ReadinessResponse(BaseModel):
    status: str
    checks: Dict[str, Any]


@router.get("/health", response_model=HealthResponse)
async def health_check():
    """健康检查端点"""
    return HealthResponse(
        status="healthy",
        service="notegather-ai",
        version="1.0.0"
    )


@router.get("/readiness", response_model=ReadinessResponse)
async def readiness_check():
    """就绪检查 - 检查依赖服务连接"""
    settings = Settings()
    checks = {}
    all_healthy = True

    # 检查 MySQL
    try:
        conn = await aiomysql.connect(
            host=settings.mysql_host,
            port=settings.mysql_port,
            user=settings.mysql_username,
            password=settings.mysql_password,
            db=settings.mysql_database,
            connect_timeout=3
        )
        await conn.ping()
        conn.close()
        checks["mysql"] = {"status": "ok", "host": settings.mysql_host}
    except Exception as e:
        checks["mysql"] = {"status": "error", "error": str(e)}
        all_healthy = False

    # 检查 Redis
    try:
        redis_client = aioredis.from_url(
            f"redis://:{settings.redis_password}@{settings.redis_host}:{settings.redis_port}",
            socket_connect_timeout=3
        )
        await redis_client.ping()
        await redis_client.close()
        checks["redis"] = {"status": "ok", "host": settings.redis_host}
    except Exception as e:
        checks["redis"] = {"status": "error", "error": str(e)}
        all_healthy = False

    # 检查 MinIO
    try:
        minio_client = Minio(
            settings.minio_endpoint,
            access_key=settings.minio_access_key,
            secret_key=settings.minio_secret_key,
            secure=settings.minio_secure
        )
        # 简单检查：列出 buckets
        list(minio_client.list_buckets())
        checks["minio"] = {"status": "ok", "endpoint": settings.minio_endpoint}
    except Exception as e:
        checks["minio"] = {"status": "error", "error": str(e)}
        all_healthy = False

    # 检查 Qdrant
    try:
        qdrant_client = QdrantClient(url=settings.qdrant_url, timeout=3)
        # 简单检查：获取集合列表
        qdrant_client.get_collections()
        checks["qdrant"] = {"status": "ok", "url": settings.qdrant_url}
    except Exception as e:
        checks["qdrant"] = {"status": "error", "error": str(e)}
        all_healthy = False

    # 检查 Neo4j
    try:
        driver = AsyncGraphDatabase.driver(
            settings.neo4j_uri,
            auth=(settings.neo4j_username, settings.neo4j_password)
        )
        await driver.verify_connectivity()
        await driver.close()
        checks["neo4j"] = {"status": "ok", "uri": settings.neo4j_uri}
    except Exception as e:
        checks["neo4j"] = {"status": "error", "error": str(e)}
        all_healthy = False

    status = "ready" if all_healthy else "not_ready"
    
    if not all_healthy:
        raise HTTPException(status_code=503, detail={"status": status, "checks": checks})
    
    return ReadinessResponse(status=status, checks=checks)


@router.get("/liveness")
async def liveness_check():
    """存活检查"""
    return {"status": "alive"}
