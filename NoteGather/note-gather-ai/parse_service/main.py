"""
parse_service/main.py
文档解析服务入口：同时启动 gRPC 服务器 + RocketMQ 消费者。
"""
import asyncio
import signal
import threading
from loguru import logger
from shared.config import get_settings
from shared.logger import setup_logger
from parse_service.grpc_server import serve_grpc
from parse_service.consumer import start_consumer


def main() -> None:
    setup_logger()
    settings = get_settings()
    logger.info("ParseService 启动 grpc_port={} ...", settings.parse_grpc_port)

    # gRPC 服务器在独立线程中运行（同步 grpc server）
    grpc_thread = threading.Thread(target=serve_grpc, daemon=True)
    grpc_thread.start()

    # RocketMQ 消费者（阻塞主线程）
    stop_event = threading.Event()

    def _shutdown(signum, frame):
        logger.info("收到退出信号，ParseService 正在关闭...")
        stop_event.set()

    signal.signal(signal.SIGINT, _shutdown)
    signal.signal(signal.SIGTERM, _shutdown)

    start_consumer(stop_event)
    logger.info("ParseService 已停止")


if __name__ == "__main__":
    main()
