from dataclasses import dataclass
from datetime import datetime


@dataclass
class EmailMessage:
    index: int
    email_id: str
    from_addr: str
    subject: str
    snippet: str
    body: str
    body_type: str
    thread_id: str
    received_at: datetime
