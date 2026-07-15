"""
shared/config.py
所有配置通过环境变量（.env.dev / .env.prod）注入，使用 pydantic-settings 管理。
"""
from functools import lru_cache
from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env.dev",
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
    )

    # ---- 服务端口 ----
    parse_grpc_port: int = 50051
    parse_http_port: int = 8001
    search_grpc_port: int = 50052

    # ---- Qdrant ----
    qdrant_host: str = "localhost"
    qdrant_port: int = 6333
    qdrant_grpc_port: int = 6334
    qdrant_prefer_grpc: bool = True

    # ---- Elasticsearch ----
    es_host: str = "localhost"
    es_port: int = 9200
    es_scheme: str = "http"
    es_username: str = ""
    es_password: str = ""

    # ---- MinIO ----
    minio_endpoint: str = "localhost:9100"
    minio_access_key: str = "minioadmin"
    minio_secret_key: str = "minioadmin"
    minio_secure: bool = False
    minio_bucket: str = "notegather"

    # ---- RocketMQ ----
    rocketmq_namesrv: str = "localhost:9876"
    rocketmq_file_topic: str = "NG_FILE_TOPIC"
    rocketmq_consumer_group: str = "ng_parse_consumer_group"
    rocketmq_producer_group: str = "ng_parse_producer_group"
    parse_max_attempts: int = Field(default=3, ge=1)

    # ---- 嵌入模型 ----
    embedding_model: str = "BAAI/bge-m3"
    embedding_device: str = "cpu"
    embedding_use_fp16: bool = False

    # ---- 分块 ----
    chunk_size: int = 512
    chunk_overlap: int = 64

    # ---- LLM ----
    llm_model: str = "gpt-4o-mini"
    llm_api_base: str = ""
    llm_api_key: str = ""
    llm_temperature: float = Field(default=0.3, ge=0, le=2)

    # ---- 检索 ----
    retrieve_top_k: int = Field(default=10, ge=1)
    rerank_top_k: int = Field(default=5, ge=1)

    # ---- 日志 ----
    log_level: str = "INFO"


@lru_cache
def get_settings() -> Settings:
    """全局单例配置，应用启动时调用一次后缓存。"""
    return Settings()
