from __future__ import annotations

import json
from typing import Any
import requests

from app.config import settings
from app.utils.prompt_builder import build_prompt
from app.models import EmailMessage


class AIProviderError(RuntimeError):
    pass


class HttpAIProvider:
    def __init__(self) -> None:
        if not settings.ai_api_url or "${" in settings.ai_api_url:
            raise AIProviderError(
                "AI_API_URL is not configured. Set AI_API_URL in your environment."
            )

    def generate_reply(self, email: EmailMessage, user_instruction: str | None) -> str:
        prompt = build_prompt(email, user_instruction)
        payload = {
            "model": settings.ai_model_name,
            "prompt": prompt,
            "stream": False,
        }
        headers = {"Content-Type": settings.ai_content_type}
        if settings.ai_api_key:
            if not settings.ai_auth_header:
                raise AIProviderError("AI_AUTH_HEADER is missing. Set AI_AUTH_HEADER.")
            if settings.ai_auth_prefix:
                headers[settings.ai_auth_header] = f"{settings.ai_auth_prefix.strip()} {settings.ai_api_key}"
            else:
                headers[settings.ai_auth_header] = settings.ai_api_key

        try:
            response = requests.post(
                settings.ai_api_url,
                data=json.dumps(payload),
                headers=headers,
                timeout=(settings.ai_connect_timeout_ms / 1000, settings.ai_read_timeout_ms / 1000),
            )
            if response.status_code < 200 or response.status_code >= 300:
                raise AIProviderError(
                    f"AI service unavailable (status {response.status_code}). "
                    f"Check if the AI service is running at {settings.ai_api_url}"
                )
            data = response.json()
        except requests.RequestException as exc:
            raise AIProviderError(
                f"Failed to connect to AI service at {settings.ai_api_url}."
            ) from exc
        except ValueError as exc:
            raise AIProviderError("AI provider returned invalid JSON.") from exc

        reply = _extract_reply(data)
        if not reply or not reply.strip():
            raise AIProviderError("AI provider returned empty response")
        return reply.strip()


def _extract_reply(payload: Any) -> str | None:
    if payload is None:
        return None

    reply = _find_reply_in_common_fields(payload)
    if reply:
        return reply

    return _find_first_text_value(payload)


def _find_reply_in_common_fields(node: Any) -> str | None:
    if not isinstance(node, dict):
        return None
    reply_fields = [
        "response",
        "content",
        "text",
        "message",
        "reply",
        "output",
        "answer",
        "generated_text",
    ]
    for field in reply_fields:
        if field in node:
            value = node[field]
            if isinstance(value, str):
                return value
            if isinstance(value, dict):
                nested = _find_reply_in_common_fields(value)
                if nested:
                    return nested
            if isinstance(value, list) and value:
                first = value[0]
                if isinstance(first, dict):
                    nested = _find_reply_in_common_fields(first)
                    if nested:
                        return nested
                if isinstance(first, str):
                    return first
    return None


def _find_first_text_value(node: Any) -> str | None:
    if isinstance(node, str):
        return node
    if isinstance(node, list):
        for child in node:
            value = _find_first_text_value(child)
            if value:
                return value
    if isinstance(node, dict):
        skip_fields = {
            "model",
            "prompt",
            "stream",
            "temperature",
            "max_tokens",
            "top_p",
            "frequency_penalty",
            "presence_penalty",
        }
        for key, child in node.items():
            if key.lower() in skip_fields:
                continue
            value = _find_first_text_value(child)
            if value:
                return value
    return None
