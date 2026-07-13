"""
parse_service/consumer.py
RocketMQ 消费者：监听 NG_FILE_TOPIC / FILE_UPLOADED，触发文档解析流水线。
"""
import json
import threading
from loguru import logger
from shared.config import get_settings
from parse_service.pipeline import run_parse_pipeline

try:
    from rocketmq.client import PushConsumer, ConsumeStatus
    _HAS_ROCKETMQ = True
except ImportError:
    _HAS_ROCKETMQ = False
    logger.warning("rocketmq-client-python 未安装，消费者功能不可用")


def _on_message(msg) -> "ConsumeStatus":
    """RocketMQ 消息回调，在消费者线程中执行。"""
    from rocketmq.client import ConsumeStatus  # type: ignore
    try:
        body = json.loads(msg.body.decode("utf-8"))
        logger.info(
            "收到文件上传消息 file_id={} user_id={} object_key={}",
            body.get("fileId"), body.get("userId"), body.get("objectKey"),
        )
        run_parse_pipeline(
            file_id=body["fileId"],
            user_id=body["userId"],
            note_id=body.get("noteId", ""),
            object_key=body["objectKey"],
            bucket=body.get("bucket", get_settings().minio_bucket),
            file_type=body.get("fileType", ""),
        )
        return ConsumeStatus.CONSUME_SUCCESS
    except Exception as e:
        logger.exception("消息处理失败 error={}", e)
        return ConsumeStatus.RECONSUME_LATER


def start_consumer(stop_event: threading.Event) -> None:
    """启动消费者（阻塞，直到 stop_event 被设置）。"""
    if not _HAS_ROCKETMQ:
        logger.warning("跳过 RocketMQ 消费者（依赖未安装）")
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
    stop_event.wait()
    consumer.shutdown()
