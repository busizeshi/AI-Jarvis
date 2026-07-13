"""
search_service/grpc_server.py
gRPC ChatService 实现：接收 Java module-ai 的 Chat 请求，流式返回 ChatChunk。
端口由配置 SEARCH_GRPC_PORT 决定（默认 50052）。
"""
import grpc
from concurrent import futures
from loguru import logger
from shared.config import get_settings

try:
    from shared.pb import ng_ai_pb2, ng_ai_pb2_grpc
    _HAS_STUBS = True
except ImportError:
    ng_ai_pb2 = None  # type: ignore
    ng_ai_pb2_grpc = None  # type: ignore
    _HAS_STUBS = False
    logger.warning("gRPC 存根未生成，请先执行 scripts/gen_proto.sh")


class ChatServiceServicer:
    """ChatService gRPC 实现，流式 RAG 问答。"""

    def Chat(self, request, context):
        from search_service.retriever import hybrid_retrieve
        from search_service.chat import build_prompt
        from shared.llm_client import stream_chat
        import asyncio

        if not _HAS_STUBS:
            context.abort(grpc.StatusCode.UNIMPLEMENTED, "存根未生成")
            return

        logger.info(
            "Chat 请求 user_id={} question_len={} top_k={}",
            request.user_id, len(request.question), request.top_k,
        )

        try:
            # 1. 混合检索
            top_k = request.top_k if request.top_k > 0 else 5
            citations = hybrid_retrieve(request.user_id, request.question, top_k)

            # 2. 构建 Prompt
            system_prompt = build_prompt(citations)

            # 3. 流式 LLM（grpc ServicerContext 不支持 async，用 asyncio.run）
            async def _stream():
                async for token in stream_chat(system_prompt, request.question):
                    yield ng_ai_pb2.ChatChunk(content=token, is_done=False)
                # 最后一个包携带 citations
                grpc_citations = [
                    ng_ai_pb2.Citation(
                        note_id=c["note_id"],
                        note_title=c.get("note_title", ""),
                        chunk_text=c["chunk_text"],
                        score=c.get("score", 0.0),
                    )
                    for c in citations
                ]
                yield ng_ai_pb2.ChatChunk(content="", is_done=True, citations=grpc_citations)

            loop = asyncio.new_event_loop()
            gen = _stream()
            try:
                while True:
                    chunk = loop.run_until_complete(gen.__anext__())
                    yield chunk
            except StopAsyncIteration:
                pass
            finally:
                loop.close()

        except Exception as e:
            logger.exception("Chat 处理失败 error={}", e)
            context.abort(grpc.StatusCode.INTERNAL, str(e))


def serve_grpc() -> None:
    """启动 ChatService gRPC 服务器（阻塞）。"""
    if not _HAS_STUBS:
        logger.error("gRPC 存根未生成，SearchService gRPC 服务器无法启动")
        return

    settings = get_settings()
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=20))
    ng_ai_pb2_grpc.add_ChatServiceServicer_to_server(ChatServiceServicer(), server)
    server.add_insecure_port(f"[::]:{settings.search_grpc_port}")
    server.start()
    logger.info("ChatService gRPC 服务器已启动 port={}", settings.search_grpc_port)
    server.wait_for_termination()
