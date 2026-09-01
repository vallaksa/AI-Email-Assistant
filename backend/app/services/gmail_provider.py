from __future__ import annotations

import base64
import os
from datetime import datetime, timezone
from email.mime.text import MIMEText
from typing import List

from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build

from app.config import settings
from app.models import EmailMessage


GMAIL_SCOPES = [
    "https://www.googleapis.com/auth/gmail.readonly",
    "https://www.googleapis.com/auth/gmail.send",
    "https://www.googleapis.com/auth/gmail.modify",
]


class GmailEmailProvider:
    def __init__(self) -> None:
        self._gmail_service = None
        self._test_emails: List[EmailMessage] = []

    def _get_service(self):
        if self._gmail_service is None:
            self._gmail_service = self._initialize_service()
        return self._gmail_service

    def _initialize_service(self):
        if not os.path.exists(settings.gmail_credentials_file):
            return None

        os.makedirs(settings.gmail_tokens_dir, exist_ok=True)
        token_path = os.path.join(settings.gmail_tokens_dir, "token.json")
        creds = None
        if os.path.exists(token_path):
            creds = Credentials.from_authorized_user_file(token_path, GMAIL_SCOPES)

        if not creds or not creds.valid:
            if creds and creds.expired and creds.refresh_token:
                creds.refresh(Request())
            else:
                flow = InstalledAppFlow.from_client_secrets_file(
                    settings.gmail_credentials_file,
                    GMAIL_SCOPES,
                )
                creds = flow.run_local_server(port=settings.gmail_oauth_port)
            with open(token_path, "w", encoding="utf-8") as token:
                token.write(creds.to_json())

        return build("gmail", "v1", credentials=creds)

    def fetch_latest(self, limit: int) -> List[EmailMessage]:
        service = self._get_service()
        if service is None:
            return self._get_test_emails(limit)

        response = (
            service.users()
            .messages()
            .list(userId="me", q="in:inbox", maxResults=min(limit, settings.email_max_limit))
            .execute()
        )

        emails: List[EmailMessage] = []
        for idx, msg in enumerate(response.get("messages", []), start=1):
            full_msg = (
                service.users().messages().get(userId="me", id=msg["id"], format="full").execute()
            )
            parsed = self._parse_message(full_msg, idx)
            if parsed:
                emails.append(parsed)
            if len(emails) >= limit:
                break
        return emails

    def fetch_sent(self, limit: int) -> List[EmailMessage]:
        service = self._get_service()
        if service is None:
            return self._get_test_emails(limit)

        response = (
            service.users()
            .messages()
            .list(userId="me", q="in:sent", maxResults=min(limit, settings.email_max_limit))
            .execute()
        )

        emails: List[EmailMessage] = []
        for idx, msg in enumerate(response.get("messages", []), start=1):
            full_msg = (
                service.users().messages().get(userId="me", id=msg["id"], format="full").execute()
            )
            parsed = self._parse_message(full_msg, idx)
            if parsed:
                emails.append(parsed)
            if len(emails) >= limit:
                break
        return emails

    def get_email(self, email_id: str) -> EmailMessage | None:
        service = self._get_service()
        if service is None:
            for email in self._get_test_emails(3):
                if email.email_id == email_id:
                    return email
            return None

        msg = service.users().messages().get(userId="me", id=email_id, format="full").execute()
        return self._parse_message(msg, 0)

    def get_thread(self, thread_id: str) -> List[EmailMessage]:
        service = self._get_service()
        if service is None:
            return self._get_test_emails(1)

        thread = service.users().threads().get(userId="me", id=thread_id, format="full").execute()
        emails: List[EmailMessage] = []
        for idx, msg in enumerate(thread.get("messages", []), start=1):
            parsed = self._parse_message(msg, idx)
            if parsed:
                emails.append(parsed)
        return emails

    def reply(self, email_id: str, reply_body: str) -> None:
        service = self._get_service()
        if service is None:
            return

        original = service.users().messages().get(userId="me", id=email_id, format="full").execute()
        to_addr = self._get_header(original, "From") or ""
        subject = self._get_header(original, "Subject") or ""
        thread_id = original.get("threadId")

        message = MIMEText(reply_body)
        message["to"] = to_addr
        message["from"] = settings.gmail_email or ""
        message["subject"] = f"Re: {subject}"
        raw = base64.urlsafe_b64encode(message.as_bytes()).decode("utf-8")

        body = {"raw": raw, "threadId": thread_id}
        service.users().messages().send(userId="me", body=body).execute()

    def search(self, query: str, limit: int, label_ids: List[str] | None = None) -> List[EmailMessage]:
        service = self._get_service()
        if service is None:
            return self._get_test_emails(min(limit, 3))

        request = (
            service.users()
            .messages()
            .list(
                userId="me",
                q=query,
                maxResults=min(limit, settings.email_max_limit),
                labelIds=label_ids or None,
            )
        )
        response = request.execute()
        emails: List[EmailMessage] = []
        for idx, msg in enumerate(response.get("messages", []), start=1):
            full_msg = (
                service.users().messages().get(userId="me", id=msg["id"], format="full").execute()
            )
            parsed = self._parse_message(full_msg, idx)
            if parsed:
                emails.append(parsed)
            if len(emails) >= limit:
                break
        return emails

    def modify_labels(
        self,
        email_id: str,
        add_labels: List[str] | None = None,
        remove_labels: List[str] | None = None,
    ) -> None:
        service = self._get_service()
        if service is None:
            return
        body = {
            "addLabelIds": add_labels or [],
            "removeLabelIds": remove_labels or [],
        }
        service.users().messages().modify(userId="me", id=email_id, body=body).execute()

    def archive(self, email_id: str) -> None:
        self.modify_labels(email_id, remove_labels=["INBOX"])

    def delete(self, email_id: str) -> None:
        service = self._get_service()
        if service is None:
            return
        service.users().messages().trash(userId="me", id=email_id).execute()

    def _parse_message(self, msg: dict, idx: int) -> EmailMessage | None:
        if not msg or not msg.get("id"):
            return None

        from_addr = self._get_header(msg, "From") or "Unknown"
        subject = self._get_header(msg, "Subject") or "(No Subject)"
        body, body_type = self._get_body_and_type(msg)
        snippet = body or msg.get("snippet", "")
        if snippet and len(snippet) > settings.snippet_max_length:
            snippet = snippet[: settings.snippet_max_length] + "..."

        internal_date = msg.get("internalDate")
        received_at = datetime.now(tz=timezone.utc)
        if internal_date:
            try:
                received_at = datetime.fromtimestamp(int(internal_date) / 1000, tz=timezone.utc)
            except Exception:
                pass

        return EmailMessage(
            index=idx,
            email_id=msg.get("id"),
            from_addr=from_addr,
            subject=subject,
            snippet=snippet or "",
            body=body or "",
            body_type=body_type or "text",
            thread_id=msg.get("threadId", ""),
            received_at=received_at,
        )

    def _get_header(self, msg: dict, name: str) -> str | None:
        payload = msg.get("payload") or {}
        headers = payload.get("headers") or []
        for header in headers:
            if header.get("name", "").lower() == name.lower():
                return header.get("value")
        return None

    def _get_body_and_type(self, msg: dict) -> tuple[str | None, str | None]:
        payload = msg.get("payload") or {}
        if not payload:
            return None, "text"

        plain_parts: List[str] = []
        html_parts: List[str] = []

        def walk(part: dict):
            mime_type = part.get("mimeType")
            body = part.get("body") or {}
            data = body.get("data")
            if mime_type == "text/plain" and data:
                plain_parts.append(_decode(data))
            elif mime_type == "text/html" and data:
                html_parts.append(_decode(data))
            elif mime_type and mime_type.startswith("multipart/"):
                for sub in part.get("parts") or []:
                    walk(sub)

        walk(payload)

        if html_parts:
            return "".join(html_parts), "html"
        if plain_parts:
            return "".join(plain_parts), "text"

        body = payload.get("body", {}).get("data")
        if body:
            return _decode(body), "text"
        return None, "text"

    def _get_test_emails(self, limit: int) -> List[EmailMessage]:
        if not self._test_emails:
            now = datetime.now(tz=timezone.utc)
            for i in range(1, 4):
                self._test_emails.append(
                    EmailMessage(
                        index=i,
                        email_id=f"test_{i}",
                        from_addr=f"test{i}@example.com",
                        subject=f"Test Email {i}",
                        snippet="Test email for development. This is a mock email used when Gmail credentials are not available.",
                        body="Test email for development. This is a mock email used when Gmail credentials are not available. Full body content here.",
                        body_type="text",
                        thread_id=f"test_thread_{i}",
                        received_at=now,
                    )
                )
        return self._test_emails[: min(limit, len(self._test_emails))]


def _decode(data: str) -> str:
    try:
        return base64.urlsafe_b64decode(data.encode("utf-8")).decode("utf-8")
    except Exception:
        return ""
