from typing import Dict, List, Optional
from threading import Lock
from app.models import EmailMessage


class InMemoryEmailRepository:
    def __init__(self) -> None:
        self._lock = Lock()
        self._cache: Dict[int, EmailMessage] = {}

    def save_all(self, emails: List[EmailMessage]) -> None:
        with self._lock:
            self._cache = {email.index: email for email in emails}

    def find_by_index(self, index: int) -> Optional[EmailMessage]:
        with self._lock:
            return self._cache.get(index)

    def find_all(self) -> List[EmailMessage]:
        with self._lock:
            return list(self._cache.values())

    def clear(self) -> None:
        with self._lock:
            self._cache.clear()
