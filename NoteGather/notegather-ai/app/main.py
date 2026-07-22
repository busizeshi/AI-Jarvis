"""
NoteGather AI Service 主入口
提供文件解析、混合检索、RAG 问答、图谱抽取和工作流编排能力
"""
import structlog
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from prometheus_client import make_asgi_app

from shared.config import Settings
from shared.observability import setup_logging, setup_tracing

logger = structlog.get_logger()


@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期管理"""
    logger.info("notegather_ai_starting", version="1.0.0")
    
    # 初始化配置
    settings = Settings()
    app.state.settings = settings
    
    # 初始化客户端（延迟导入避免循环依赖）
    # from shared.clients import init_clients
    # await init_clients(settings)
    
    logger.info("notegather_ai_started")
    
    yield
    
    logger.info("notegather_ai_stopping")
    # 清理资源
    logger.info("notegather_ai_stopped")


def create_app() -> FastAPI:
    """创建 FastAPI 应用实例"""
    
    # 配置日志
    setup_logging()
    
    # 配置 Trace
    setup_tracing()
    
    app = FastAPI(
        title="NoteGather AI Service",
        description="文件解析、检索、图谱与工作流服务",
        version="1.0.0",
        lifespan=lifespan
    )
    
    # CORS 中间件
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],  # 生产环境应配置具体来源
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )
    
    # 注册路由
    from api.health import router as health_router
    # from api.parsing import router as parsing_router
    # from api.retrieval import router as retrieval_router
    # from api.workflow import router as workflow_router
    
    app.include_router(health_router, prefix="/api/v1", tags=["health"])
    # app.include_router(parsing_router, prefix="/api/v1/parsing", tags=["parsing"])
    # app.include_router(retrieval_router, prefix="/api/v1/retrieval", tags=["retrieval"])
    # app.include_router(workflow_router, prefix="/api/v1/workflow", tags=["workflow"])
    
    # Prometheus 指标端点
    metrics_app = make_asgi_app()
    app.mount("/metrics", metrics_app)
    
    return app


app = create_app()


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_config=None  # 使用自定义日志配置
    )
