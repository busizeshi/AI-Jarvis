"""
search_service/main.py
检索问答服务入口：启动 gRPC ChatService 服务器。
"""
import signal
import threading
from loguru import logger
from shared.config import get_settings
from shared.logger import setup_logger
from search_service.grpc_server import serve_grpc


def main() -> None:
    setup_logger()
    settings = get_settings()
    logger.info("SearchService 启动 grpc_port={} ...", settings.search_grpc_port)

    stop_event = threading.Event()

    def _shutdown(signum, frame):
        logger.info("收到退出信号，SearchService 正在关闭...")
        stop_event.set()

    signal.signal(signal.SIGINT, _shutdown)
    signal.signal(signal.SIGTERM, _shutdown)

    # serve_grpc 自身阻塞，在主线程运行
    grpc_thread = threading.Thread(target=serve_grpc, daemon=True)
    grpc_thread.start()

    stop_event.wait()
    logger.info("SearchService 已停止")


if __name__ == "__main__":
    main()
