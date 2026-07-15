"""gRPC ChatService entry point for the RAG pipeline."""
from __future__ import annotations

import asyncio
from concurrent import futures
from collections.abc import Iterator

import grpc
from loguru import logger

from shared.config import get_settings

try:
    from shared.pb import ng_ai_pb2, ng_ai_pb2_grpc

    HAS_STUBS = True
except ImportError:
    ng_ai_pb2 = None  # type: ignore[assignment]
    ng_ai_pb2_grpc = None  # type: ignore[assignment]
    HAS_STUBS = False
    logger.warning("gRPC stubs are missing; run scripts/gen_proto.ps1 or scripts/gen_proto.sh")

DEFAULT_TOP_K = 5


class ChatServiceServicer:
    """Synchronous gRPC adapter around async LiteLLM streaming."""

    def Chat(self, request, context) -> Iterator[object]:
        if not HAS_STUBS:
            context.abort(grpc.StatusCode.UNIMPLEMENTED, "gRPC stubs are not generated")
        if not request.user_id.strip() or not request.question.strip():
            context.abort(grpc.StatusCode.INVALID_ARGUMENT, "user_id and question are required")

        from search_service.chat import build_prompt
        from search_service.retriever import hybrid_retrieve
        from shared.llm_client import stream_chat

        top_k = request.top_k if request.top_k > 0 else DEFAULT_TOP_K
        logger.info("Chat request user_id={} question_length={} top_k={}", request.user_id, len(request.question), top_k)
        try:
            citations = hybrid_retrieve(request.user_id, request.question, top_k)
            prompt = build_prompt(citations)
            yield from self._stream_response(context, stream_chat(prompt, request.question), citations)
        except Exception as error:
            logger.exception("Chat request failed user_id={} error={}", request.user_id, error)
            context.abort(grpc.StatusCode.INTERNAL, "RAG chat processing failed")

    def _stream_response(self, context, token_stream, citations: list[dict]) -> Iterator[object]:
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
        generator = token_stream.__aiter__()
        try:
            while context.is_active():
                try:
                    token = loop.run_until_complete(generator.__anext__())
                except StopAsyncIteration:
                    break
                if token:
                    yield ng_ai_pb2.ChatChunk(content=token, is_done=False)
            if context.is_active():
                yield ng_ai_pb2.ChatChunk(
                    is_done=True,
                    citations=[
                        ng_ai_pb2.Citation(
                            note_id=citation["note_id"],
                            note_title=citation["note_title"],
                            chunk_text=citation["chunk_text"],
                            score=citation["score"],
                        )
                        for citation in citations
                    ],
                )
        finally:
            loop.run_until_complete(generator.aclose())
            loop.close()
            asyncio.set_event_loop(None)


def serve_grpc() -> None:
    """Start the blocking ChatService gRPC server."""
    if not HAS_STUBS:
        logger.error("ChatService cannot start because gRPC stubs are missing")
        return
    settings = get_settings()
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=20))
    ng_ai_pb2_grpc.add_ChatServiceServicer_to_server(ChatServiceServicer(), server)
    server.add_insecure_port(f"[::]:{settings.search_grpc_port}")
    server.start()
    logger.info("ChatService gRPC server started port={}", settings.search_grpc_port)
    server.wait_for_termination()
