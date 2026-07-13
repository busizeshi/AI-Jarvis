"""
shared/logger.py
统一使用 loguru，自动识别配置的 LOG_LEVEL，格式化带时间/级别/模块的日志。
"""
import sys
from loguru import logger
from shared.config import get_settings


def setup_logger() -> None:
    """初始化全局日志（每个服务的 main.py / grpc_server.py 入口调用一次）。"""
    settings = get_settings()
    logger.remove()
    logger.add(
        sys.stderr,
        level=settings.log_level.upper(),
        format=(
            "<green>{time:YYYY-MM-DD HH:mm:ss.SSS}</green> | "
            "<level>{level: <8}</level> | "
            "<cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> - "
            "<level>{message}</level>"
        ),
        colorize=True,
        backtrace=True,
        diagnose=True,
    )


__all__ = ["logger", "setup_logger"]
