from datetime import datetime
from typing import Generic, Optional, TypeVar
from pydantic import BaseModel, Field


T = TypeVar("T")


class ApiResponse(BaseModel, Generic[T]):
    success: bool
    message: str
    data: Optional[T] = None
    error: Optional[str] = None

    @staticmethod
    def ok(message: str, data: T):
        return ApiResponse(success=True, message=message, data=data)

    @staticmethod
    def fail(message: str, error: str):
        return ApiResponse(success=False, message=message, error=error)


class EmailDto(BaseModel):
    index: int
    emailId: str
    from_: str = Field(alias="from")
    subject: str
    snippet: str
    body: Optional[str] = None
    bodyType: Optional[str] = None
    threadId: Optional[str] = None
    receivedAt: datetime

    class Config:
        populate_by_name = True


class ThreadMessageDto(BaseModel):
    emailId: str
    from_: str = Field(alias="from")
    subject: str
    body: str
    bodyType: Optional[str] = None
    isSent: bool
    receivedAt: datetime

    class Config:
        populate_by_name = True


class ReplyToEmailRequest(BaseModel):
    emailId: str
    userInstruction: Optional[str] = None


class ReplyResponse(BaseModel):
    emailId: str
    replyPreview: str
