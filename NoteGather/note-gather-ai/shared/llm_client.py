"""
shared/llm_client.py
LiteLLM 调用封装，统一管理 LLM 模型、API Key、流式输出。
"""
from __future__ import annotations

from typing import AsyncIterator
import litellm
from litellm import acompletion
from loguru import logger
from shared.config import get_settings


def _configure_litellm() -> None:
    settings = get_settings()
    if settings.llm_api_key:
        litellm.api_key = settings.llm_api_key
    if settings.llm_api_base:
        litellm.api_base = settings.llm_api_base


_configure_litellm()


async def stream_chat(
    system_prompt: str,
    user_message: str,
) -> AsyncIterator[str]:
    """
    流式调用 LLM，逐 token yield 文本片段。

    :param system_prompt: 含检索上下文的系统提示
    :param user_message: 用户原始问题
    :yields: str - 每次返回一个 token 片段
    """
    settings = get_settings()
    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_message},
    ]
    logger.debug("LLM 请求 model={} msg_len={}", settings.llm_model, len(user_message))

    response = await acompletion(
        model=settings.llm_model,
        messages=messages,
        temperature=settings.llm_temperature,
        stream=True,
    )
    async for chunk in response:
        delta = chunk.choices[0].delta.content
        if delta:
            yield delta
