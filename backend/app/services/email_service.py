from typing import List

from app.config import settings
from app.models import EmailMessage
from app.repositories.in_memory_email_repository import InMemoryEmailRepository
from app.services.ai_provider import HttpAIProvider
from app.services.gmail_provider import GmailEmailProvider


class EmailService:
    def __init__(self) -> None:
        self._gmail = GmailEmailProvider()
        self._ai = HttpAIProvider()
        self._cache = InMemoryEmailRepository()

    def fetch_inbox(self, limit: int) -> List[EmailMessage]:
        if limit < settings.email_min_limit or limit > settings.email_max_limit:
            raise ValueError("Invalid email limit")
        emails = self._gmail.fetch_latest(limit)
        for idx, email in enumerate(emails, start=1):
            email.index = idx
        self._cache.save_all(emails)
        return emails

    def fetch_sent(self, limit: int) -> List[EmailMessage]:
        if limit < settings.email_min_limit or limit > settings.email_max_limit:
            raise ValueError("Invalid email limit")
        emails = self._gmail.fetch_sent(limit)
        for idx, email in enumerate(emails, start=1):
            email.index = idx
        self._cache.save_all(emails)
        return emails

    def get_email(self, email_id: str) -> EmailMessage | None:
        if not email_id or not email_id.strip():
            raise ValueError("Email ID cannot be null or empty")
        return self._gmail.get_email(email_id)

    def get_thread(self, thread_id: str) -> List[EmailMessage]:
        if not thread_id or not thread_id.strip():
            raise ValueError("Thread ID cannot be null or empty")
        return self._gmail.get_thread(thread_id)

    def reply(self, email_id: str, user_instruction: str | None) -> str:
        email = self.get_email(email_id)
        if email is None:
            raise ValueError("Email not found")
        reply_body = self._ai.generate_reply(email, user_instruction)
        self._gmail.reply(email.email_id, reply_body)

        preview_len = settings.reply_preview_length
        if len(reply_body) > preview_len:
            return reply_body[:preview_len] + "..."
        return reply_body
