"""RocketMQ 文件上传消费者。"""
from __future__ import annotations

import json
import threading
from typing import Any

from loguru import logger

from parse_service.pipeline import ParseRetryableError, run_parse_pipeline
from parse_service.producer import shutdown_producer
from shared.config import get_settings

try:
    from rocketmq.client import ConsumeStatus, PushConsumer

    _HAS_ROCKETMQ = True
except ImportError:
    _HAS_ROCKETMQ = False
    logger.warning("rocketmq-client-python 未安装，消费者功能不可用")


def _on_message(message: Any) -> "ConsumeStatus":
    """处理上传事件；中间失败交给 RocketMQ 按同一消息重投。"""
    from rocketmq.client import ConsumeStatus  # type: ignore

    try:
        body = _parse_message_body(message.body)
    except (UnicodeDecodeError, json.JSONDecodeError, KeyError, TypeError, ValueError) as error:
        logger.error("忽略非法 FILE_UPLOADED 消息 error={}", error)
        return ConsumeStatus.CONSUME_SUCCESS
    attempt_count = getattr(message, "reconsume_times", 0) + 1
    try:
        run_parse_pipeline(
            file_id=body["fileId"],
            parse_task_id=body["parseTaskId"],
            user_id=body["userId"],
            note_id=body["noteId"],
            note_title=body["noteTitle"],
            object_key=body["objectKey"],
            bucket=body["bucket"],
            file_type=body["fileType"],
            attempt_count=attempt_count,
        )
    except ParseRetryableError as error:
        logger.warning("文件解析将在 RocketMQ 重试 file_id={} error={}", body["fileId"], error)
        return ConsumeStatus.RECONSUME_LATER
    except Exception as error:
        logger.exception("文件解析结果发布失败，将重新消费 file_id={} error={}", body["fileId"], error)
        return ConsumeStatus.RECONSUME_LATER
    return ConsumeStatus.CONSUME_SUCCESS


def _parse_message_body(raw_body: bytes) -> dict[str, str]:
    body = json.loads(raw_body.decode("utf-8"))
    required_fields = (
        "fileId",
        "parseTaskId",
        "userId",
        "noteId",
        "objectKey",
        "bucket",
        "fileType",
    )
    for field in required_fields:
        if not body.get(field):
            raise ValueError(f"缺少字段 {field}")
    body["noteTitle"] = body.get("noteTitle") or body.get("fileName") or "未命名笔记"
    return body


def start_consumer(stop_event: threading.Event) -> None:
    """启动阻塞式消费者，并在服务退出时释放 MQ 连接。"""
    if not _HAS_ROCKETMQ:
        logger.warning("跳过 RocketMQ 消费者，依赖未安装")
        stop_event.wait()
        return
    settings = get_settings()
    consumer = PushConsumer(settings.rocketmq_consumer_group)
    consumer.set_name_server_address(settings.rocketmq_namesrv)
    consumer.subscribe(settings.rocketmq_file_topic, _on_message, "FILE_UPLOADED")
    consumer.start()
    logger.info(
        "RocketMQ 消费者已启动 group={} topic={}",
        settings.rocketmq_consumer_group,
        settings.rocketmq_file_topic,
    )
    try:
        stop_event.wait()
    finally:
        consumer.shutdown()
        shutdown_producer()
