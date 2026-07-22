"""配置管理"""
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """应用配置"""
    
    # 应用配置
    app_name: str = "notegather-ai"
    app_env: str = "dev"
    debug: bool = True
    
    # MySQL
    mysql_host: str = "115.190.125.94"
    mysql_port: int = 3306
    mysql_database: str = "notegather"
    mysql_username: str = "root"
    mysql_password: str = "JWDdmm@2552"
    
    # Redis
    redis_host: str = "115.190.125.94"
    redis_port: int = 6379
    redis_password: str = "root"
    redis_database: int = 0
    
    # MinIO
    minio_endpoint: str = "192.168.1.12:9000"
    minio_access_key: str = "root"
    minio_secret_key: str = "qdrantroot"
    minio_secure: bool = False
    minio_bucket_original: str = "notegather-original"
    minio_bucket_derived: str = "notegather-derived"
    
    # Qdrant
    qdrant_url: str = "http://192.168.1.12:6333"
    qdrant_api_key: str = "qdrantroot"
    qdrant_collection: str = "knowledge_chunks_v1"
    
    # Neo4j
    neo4j_uri: str = "bolt://192.168.1.12:7687"
    neo4j_username: str = "notegather_app"
    neo4j_password: str = "root"
    
    # RocketMQ
    rocketmq_nameserver: str = "192.168.1.12:9876"
    rocketmq_group: str = "notegather-ai-consumer"
    
    # Nacos（可选）
    nacos_server_addr: str = "192.168.1.12:8848"
    nacos_namespace: str = "dev"
    nacos_username: str = "nacos"
    nacos_password: str = "nacos"
    
    # 模型配置
    embedding_model: str = "sentence-transformers/all-MiniLM-L6-v2"
    llm_provider: str = "openai"
    llm_model: str = "gpt-4o-mini"
    llm_api_key: str = ""
    llm_base_url: str = ""
    
    # 日志与监控
    log_level: str = "INFO"
    otel_exporter_endpoint: str = ""
    
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore"
    )
    
    @property
    def mysql_dsn(self) -> str:
        """MySQL DSN"""
        return (
            f"mysql+aiomysql://{self.mysql_username}:{self.mysql_password}"
            f"@{self.mysql_host}:{self.mysql_port}/{self.mysql_database}"
            f"?charset=utf8mb4"
        )
    
    @property
    def redis_url(self) -> str:
        """Redis URL"""
        password_part = f":{self.redis_password}@" if self.redis_password else ""
        return f"redis://{password_part}{self.redis_host}:{self.redis_port}/{self.redis_database}"
