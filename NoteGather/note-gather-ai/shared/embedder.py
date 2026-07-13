"""
shared/embedder.py
BGE-M3 向量嵌入封装（单例，避免模型重复加载）。
BGE-M3 同时支持稠密（dense）/ 稀疏（sparse）向量，第一阶段只用稠密。
"""
from __future__ import annotations

import threading
from typing import Union

from FlagEmbedding import BGEM3FlagModel
from loguru import logger

from shared.config import get_settings

_lock = threading.Lock()
_model: BGEM3FlagModel | None = None


def _load_model() -> BGEM3FlagModel:
    settings = get_settings()
    logger.info(
        "加载嵌入模型 model={} device={}", settings.embedding_model, settings.embedding_device
    )
    return BGEM3FlagModel(
        settings.embedding_model,
        use_fp16=settings.embedding_use_fp16,
        device=settings.embedding_device,
    )


def get_embedder() -> BGEM3FlagModel:
    """线程安全单例，首次调用时加载模型。"""
    global _model
    if _model is None:
        with _lock:
            if _model is None:
                _model = _load_model()
    return _model


def embed_texts(texts: list[str]) -> list[list[float]]:
    """
    对文本列表做稠密嵌入，返回归一化向量列表。

    :param texts: 待嵌入的文本（已分块）
    :return: list of float vectors（维度 1024）
    """
    embedder = get_embedder()
    output = embedder.encode(
        texts,
        batch_size=12,
        max_length=512,
        return_dense=True,
        return_sparse=False,
        return_colbert_vecs=False,
    )
    return output["dense_vecs"].tolist()


def embed_query(query: str) -> list[float]:
    """单条查询嵌入，用于检索阶段。"""
    return embed_texts([query])[0]
