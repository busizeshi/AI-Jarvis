"""
parse_service/__init__.py
文档解析服务：
  - 消费 RocketMQ NG_FILE_TOPIC（FILE_UPLOADED tag）
  - 下载 MinIO 文件 → 文本抽取 → 分块 → BGE-M3 嵌入 → 写入 Qdrant + ES
  - 通过 gRPC ParseService 暴露解析状态查询接口（供 Java module-ai 调用）
  - 解析完成后发布 PARSE_DONE / PARSE_FAILED 消息回 Java
"""
