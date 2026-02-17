from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.config import settings
from app.schemas import ApiResponse, EmailDto, ReplyResponse, ReplyToEmailRequest, ThreadMessageDto
from app.services.email_service import EmailService
from app.utils.mappers import to_email_dto_list, to_email_dto, to_thread_message_list


app = FastAPI(title="Email Assistant API", version="1.0.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

service = EmailService()


@app.post("/api/emails/fetch/{limit}", response_model=ApiResponse[list[EmailDto]])
def fetch_emails(limit: int):
    try:
        emails = service.fetch_inbox(limit)
        return ApiResponse.ok("Emails fetched successfully", to_email_dto_list(emails))
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@app.post("/api/emails/sent/{limit}", response_model=ApiResponse[list[EmailDto]])
def fetch_sent_emails(limit: int):
    try:
        emails = service.fetch_sent(limit)
        return ApiResponse.ok("Sent emails fetched successfully", to_email_dto_list(emails))
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@app.get("/api/emails/{email_id}", response_model=ApiResponse[EmailDto])
def get_email(email_id: str):
    email = service.get_email(email_id)
    if not email:
        raise HTTPException(status_code=404, detail="Email not found")
    return ApiResponse.ok("Email retrieved successfully", to_email_dto(email))


@app.get("/api/emails/{email_id}/thread", response_model=ApiResponse[list[ThreadMessageDto]])
def get_thread(email_id: str):
    email = service.get_email(email_id)
    if not email:
        raise HTTPException(status_code=404, detail="Email not found")
    if not email.thread_id:
        return ApiResponse.ok("No thread found", [])
    try:
        messages = service.get_thread(email.thread_id)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    return ApiResponse.ok(
        "Thread retrieved successfully",
        to_thread_message_list(messages, settings.gmail_email),
    )


@app.post("/api/emails/reply", response_model=ApiResponse[ReplyResponse])
def reply_to_email(request: ReplyToEmailRequest):
    try:
        preview = service.reply(request.emailId, request.userInstruction)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    return ApiResponse.ok(
        "Reply sent successfully",
        ReplyResponse(emailId=request.emailId, replyPreview=preview),
    )


@app.exception_handler(HTTPException)
def http_exception_handler(_, exc: HTTPException):
    payload = ApiResponse.fail("Request failed", exc.detail)
    return JSONResponse(status_code=exc.status_code, content=payload.model_dump())


@app.exception_handler(Exception)
def unhandled_exception_handler(_, exc: Exception):
    payload = ApiResponse.fail("Unexpected error", str(exc))
    return JSONResponse(status_code=500, content=payload.model_dump())
