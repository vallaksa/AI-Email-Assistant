from typing import List

from app.config import settings
from app.models import EmailMessage
from app.schemas import EmailDto, ThreadMessageDto


def to_email_dto(email: EmailMessage) -> EmailDto:
    return EmailDto(
        index=email.index,
        emailId=email.email_id,
        from_=email.from_addr,
        subject=email.subject,
        snippet=email.snippet,
        body=email.body,
        bodyType=email.body_type,
        threadId=email.thread_id,
        receivedAt=email.received_at,
    )


def to_email_dto_list(emails: List[EmailMessage]) -> List[EmailDto]:
    return [to_email_dto(email) for email in emails]


def to_thread_message_list(emails: List[EmailMessage], user_email: str | None) -> List[ThreadMessageDto]:
    result: List[ThreadMessageDto] = []
    user_email_lower = (user_email or "").lower()
    for email in emails:
        is_sent = user_email_lower and user_email_lower in (email.from_addr or "").lower()
        result.append(
            ThreadMessageDto(
                emailId=email.email_id,
                from_=email.from_addr,
                subject=email.subject,
                body=email.body,
                bodyType=email.body_type,
                isSent=bool(is_sent),
                receivedAt=email.received_at,
            )
        )
    return result
