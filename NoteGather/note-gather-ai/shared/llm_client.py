"""LiteLLM streaming client."""
from __future__ import annotations

from collections.abc import AsyncIterator

from litellm import acompletion
from loguru import logger

from shared.config import get_settings


async def stream_chat(system_prompt: str, user_message: str) -> AsyncIterator[str]:
    """Yield text fragments returned by the configured LLM provider."""
    settings = get_settings()
    logger.debug("Start LLM stream model={} question_length={}", settings.llm_model, len(user_message))
    response = await acompletion(
        model=settings.llm_model,
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_message},
        ],
        temperature=settings.llm_temperature,
        stream=True,
        api_base=settings.llm_api_base or None,
        api_key=settings.llm_api_key or None,
    )
    async for chunk in response:
        choices = getattr(chunk, "choices", None)
        if not choices:
            continue
        content = getattr(choices[0].delta, "content", None)
        if content:
            yield content
