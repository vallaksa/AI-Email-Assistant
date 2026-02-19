from __future__ import annotations

import json
import os
import sys
from datetime import datetime
from typing import Any, Callable, Dict, List

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
BACKEND_PATH = os.path.join(ROOT, "backend")
if BACKEND_PATH not in sys.path:
    sys.path.insert(0, BACKEND_PATH)

from app.config import settings  # noqa: E402
from app.services.gmail_provider import GmailEmailProvider  # noqa: E402
from app.services.ai_provider import HttpAIProvider  # noqa: E402
from app.utils.mappers import to_email_dto, to_thread_message_list  # noqa: E402


SERVER_INFO = {"name": "email-assistant-mcp", "version": "0.1.0"}


gmail_provider = GmailEmailProvider()
ai_provider = HttpAIProvider()


def _email_to_dict(email) -> Dict[str, Any]:
    dto = to_email_dto(email)
    return dto.model_dump(by_alias=True)


def _emails_to_dict(emails) -> List[Dict[str, Any]]:
    return [_email_to_dict(email) for email in emails]


def _thread_to_dict(messages) -> List[Dict[str, Any]]:
    dtos = to_thread_message_list(messages, settings.gmail_email)
    return [dto.model_dump(by_alias=True) for dto in dtos]


def _ok(message: str, data: Any = None) -> Dict[str, Any]:
    return {"success": True, "message": message, "data": data, "error": None}


def _err(message: str, error: str) -> Dict[str, Any]:
    return {"success": False, "message": message, "data": None, "error": error}


def list_inbox(limit: int = 10, query: str | None = None) -> Dict[str, Any]:
    emails = gmail_provider.fetch_latest(limit)
    if query:
        q = query.lower()
        emails = [
            email
            for email in emails
            if q in (email.subject or "").lower()
            or q in (email.from_addr or "").lower()
            or q in (email.snippet or "").lower()
            or q in (email.body or "").lower()
        ]
    return _ok("Inbox emails retrieved", _emails_to_dict(emails))


def list_sent(limit: int = 10, query: str | None = None) -> Dict[str, Any]:
    emails = gmail_provider.fetch_sent(limit)
    if query:
        q = query.lower()
        emails = [
            email
            for email in emails
            if q in (email.subject or "").lower()
            or q in (email.from_addr or "").lower()
            or q in (email.snippet or "").lower()
            or q in (email.body or "").lower()
        ]
    return _ok("Sent emails retrieved", _emails_to_dict(emails))


def get_email(email_id: str) -> Dict[str, Any]:
    email = gmail_provider.get_email(email_id)
    if not email:
        return _err("Email not found", "Email not found")
    return _ok("Email retrieved", _email_to_dict(email))


def get_thread(thread_id: str) -> Dict[str, Any]:
    messages = gmail_provider.get_thread(thread_id)
    return _ok("Thread retrieved", _thread_to_dict(messages))


def reply_email(email_id: str, instruction: str | None = None) -> Dict[str, Any]:
    email = gmail_provider.get_email(email_id)
    if not email:
        return _err("Email not found", "Email not found")
    reply_body = ai_provider.generate_reply(email, instruction)
    gmail_provider.reply(email_id, reply_body)
    preview = reply_body[: settings.reply_preview_length]
    if len(reply_body) > settings.reply_preview_length:
        preview += "..."
    return _ok("Reply sent", {"emailId": email_id, "replyPreview": preview})


def search_emails(query: str, limit: int = 10, label_ids: List[str] | None = None) -> Dict[str, Any]:
    emails = gmail_provider.search(query, limit, label_ids)
    return _ok("Search results", _emails_to_dict(emails))


def add_labels(email_id: str, label_ids: List[str]) -> Dict[str, Any]:
    gmail_provider.modify_labels(email_id, add_labels=label_ids)
    return _ok("Labels added", {"emailId": email_id, "labels": label_ids})


def remove_labels(email_id: str, label_ids: List[str]) -> Dict[str, Any]:
    gmail_provider.modify_labels(email_id, remove_labels=label_ids)
    return _ok("Labels removed", {"emailId": email_id, "labels": label_ids})


def archive_email(email_id: str) -> Dict[str, Any]:
    gmail_provider.archive(email_id)
    return _ok("Email archived", {"emailId": email_id})


def delete_email(email_id: str) -> Dict[str, Any]:
    gmail_provider.delete(email_id)
    return _ok("Email deleted", {"emailId": email_id})


TOOLS: Dict[str, Callable[..., Dict[str, Any]]] = {
    "list_inbox": list_inbox,
    "list_sent": list_sent,
    "get_email": get_email,
    "get_thread": get_thread,
    "reply_email": reply_email,
    "search_emails": search_emails,
    "add_labels": add_labels,
    "remove_labels": remove_labels,
    "archive_email": archive_email,
    "delete_email": delete_email,
}


TOOL_SCHEMAS = [
    {
        "name": "list_inbox",
        "description": "List recent inbox emails",
        "inputSchema": {
            "type": "object",
            "properties": {
                "limit": {"type": "integer", "default": 10, "minimum": 1, "maximum": 50},
                "query": {"type": "string"},
            },
        },
    },
    {
        "name": "list_sent",
        "description": "List recent sent emails",
        "inputSchema": {
            "type": "object",
            "properties": {
                "limit": {"type": "integer", "default": 10, "minimum": 1, "maximum": 50},
                "query": {"type": "string"},
            },
        },
    },
    {
        "name": "get_email",
        "description": "Get a single email by ID",
        "inputSchema": {
            "type": "object",
            "required": ["email_id"],
            "properties": {"email_id": {"type": "string"}},
        },
    },
    {
        "name": "get_thread",
        "description": "Get a thread by thread ID",
        "inputSchema": {
            "type": "object",
            "required": ["thread_id"],
            "properties": {"thread_id": {"type": "string"}},
        },
    },
    {
        "name": "reply_email",
        "description": "Generate and send a reply to an email",
        "inputSchema": {
            "type": "object",
            "required": ["email_id"],
            "properties": {
                "email_id": {"type": "string"},
                "instruction": {"type": "string"},
            },
        },
    },
    {
        "name": "search_emails",
        "description": "Search emails using Gmail query syntax",
        "inputSchema": {
            "type": "object",
            "required": ["query"],
            "properties": {
                "query": {"type": "string"},
                "limit": {"type": "integer", "default": 10, "minimum": 1, "maximum": 50},
                "label_ids": {"type": "array", "items": {"type": "string"}},
            },
        },
    },
    {
        "name": "add_labels",
        "description": "Add labels to an email",
        "inputSchema": {
            "type": "object",
            "required": ["email_id", "label_ids"],
            "properties": {
                "email_id": {"type": "string"},
                "label_ids": {"type": "array", "items": {"type": "string"}},
            },
        },
    },
    {
        "name": "remove_labels",
        "description": "Remove labels from an email",
        "inputSchema": {
            "type": "object",
            "required": ["email_id", "label_ids"],
            "properties": {
                "email_id": {"type": "string"},
                "label_ids": {"type": "array", "items": {"type": "string"}},
            },
        },
    },
    {
        "name": "archive_email",
        "description": "Archive an email (remove INBOX label)",
        "inputSchema": {
            "type": "object",
            "required": ["email_id"],
            "properties": {"email_id": {"type": "string"}},
        },
    },
    {
        "name": "delete_email",
        "description": "Move an email to trash",
        "inputSchema": {
            "type": "object",
            "required": ["email_id"],
            "properties": {"email_id": {"type": "string"}},
        },
    },
]


def _write(payload: Dict[str, Any]) -> None:
    sys.stdout.write(json.dumps(payload) + "\n")
    sys.stdout.flush()


def _error_response(req_id: Any, code: int, message: str) -> Dict[str, Any]:
    return {"jsonrpc": "2.0", "id": req_id, "error": {"code": code, "message": message}}


def _result_response(req_id: Any, result: Any) -> Dict[str, Any]:
    return {"jsonrpc": "2.0", "id": req_id, "result": result}


def _handle_initialize(req_id: Any) -> None:
    result = {
        "protocolVersion": "0.1.0",
        "serverInfo": SERVER_INFO,
        "capabilities": {"tools": {}},
    }
    _write(_result_response(req_id, result))


def _handle_tools_list(req_id: Any) -> None:
    _write(_result_response(req_id, {"tools": TOOL_SCHEMAS}))


def _handle_tools_call(req_id: Any, params: Dict[str, Any]) -> None:
    name = params.get("name")
    arguments = params.get("arguments") or {}
    if name not in TOOLS:
        _write(_error_response(req_id, -32601, f"Unknown tool: {name}"))
        return
    try:
        result = TOOLS[name](**arguments)
    except TypeError as exc:
        _write(_error_response(req_id, -32602, f"Invalid arguments: {exc}"))
        return
    except Exception as exc:
        _write(_error_response(req_id, -32000, f"Tool error: {exc}"))
        return

    content = [{"type": "text", "text": json.dumps(result)}]
    _write(_result_response(req_id, {"content": content}))


def main() -> None:
    for line in sys.stdin:
        line = line.strip()
        if not line:
            continue
        try:
            payload = json.loads(line)
        except json.JSONDecodeError:
            _write(_error_response(None, -32700, "Parse error"))
            continue

        req_id = payload.get("id")
        method = payload.get("method")
        params = payload.get("params") or {}

        if method == "initialize":
            _handle_initialize(req_id)
        elif method == "tools/list":
            _handle_tools_list(req_id)
        elif method == "tools/call":
            _handle_tools_call(req_id, params)
        else:
            _write(_error_response(req_id, -32601, f"Method not found: {method}"))


if __name__ == "__main__":
    main()
