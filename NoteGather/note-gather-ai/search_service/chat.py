"""
search_service/chat.py
Prompt 构建工具：将检索召回的 chunk 组装为 LLM 系统提示。
"""
from __future__ import annotations

_SYSTEM_TEMPLATE = """\
你是「拾笺」个人知识库的智能助手，只根据以下用户笔记片段回答问题。
如果笔记中没有足够信息，请如实告知，不要凭空捏造。
回答请简洁、准确，必要时引用对应笔记标题。

---
{context}
---
"""


def build_prompt(citations: list[dict]) -> str:
    """
    将检索结果拼装为 system prompt 的 context 块。

    :param citations: hybrid_retrieve 返回的 chunk 列表
    :return: 填充好 context 的完整 system prompt 字符串
    """
    if not citations:
        context = "（暂无相关笔记内容）"
    else:
        parts = []
        for i, c in enumerate(citations, 1):
            title = c.get("note_title") or c.get("note_id", "未知笔记")
            text  = c.get("chunk_text", "").strip()
            parts.append(f"[{i}] 笔记：{title}\n{text}")
        context = "\n\n".join(parts)

    return _SYSTEM_TEMPLATE.format(context=context)
