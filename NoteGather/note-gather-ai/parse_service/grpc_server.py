"""
parse_service/grpc_server.py
gRPC ParseService 实现：对外暴露解析状态查询接口（Java → Python）。
端口由配置 PARSE_GRPC_PORT 决定（默认 50051）。
"""
import grpc
from concurrent import futures
from loguru import logger
from shared.config import get_settings

# 由 grpc_tools 生成，运行 scripts/gen_proto.sh 后可用
try:
    from shared.pb import ng_ai_pb2, ng_ai_pb2_grpc
except ImportError:
    ng_ai_pb2 = None  # type: ignore
    ng_ai_pb2_grpc = None  # type: ignore
    logger.warning("gRPC 存根未生成，请先执行 scripts/gen_proto.sh")


class ParseServiceServicer:
    """ParseService gRPC 实现，查询 file_id 的解析状态。"""

    def GetParseStatus(self, request, context):
        # TODO: 从 Redis / 内存状态字典中查询实际解析进度
        logger.debug("GetParseStatus file_id={}", request.file_id)
        if ng_ai_pb2 is None:
            context.abort(grpc.StatusCode.UNIMPLEMENTED, "存根未生成")
            return None
        return ng_ai_pb2.ParseStatusResponse(
            file_id=request.file_id,
            status="PENDING",
            chunk_count=0,
            error_msg="",
        )


def serve_grpc() -> None:
    """启动 gRPC 服务器（阻塞），适合在独立线程中运行。"""
    if ng_ai_pb2_grpc is None:
        logger.error("gRPC 存根未生成，ParseService gRPC 服务器无法启动")
        return

    settings = get_settings()
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    ng_ai_pb2_grpc.add_ParseServiceServicer_to_server(ParseServiceServicer(), server)
    server.add_insecure_port(f"[::]:{settings.parse_grpc_port}")
    server.start()
    logger.info("ParseService gRPC 服务器已启动 port={}", settings.parse_grpc_port)
    server.wait_for_termination()
