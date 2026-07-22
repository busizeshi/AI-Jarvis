"""健康检查 API"""
from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter()


class HealthResponse(BaseModel):
    status: str
    service: str
    version: str


@router.get("/health", response_model=HealthResponse)
async def health_check():
    """健康检查端点"""
    return HealthResponse(
        status="healthy",
        service="notegather-ai",
        version="1.0.0"
    )


@router.get("/readiness")
async def readiness_check():
    """就绪检查 - 检查依赖服务连接"""
    # TODO: 检查 MinIO、Qdrant、Neo4j、Redis、MySQL 连接
    return {"status": "ready"}


@router.get("/liveness")
async def liveness_check():
    """存活检查"""
    return {"status": "alive"}
