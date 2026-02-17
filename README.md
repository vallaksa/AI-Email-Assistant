# Email Assistant (Python + Streamlit)

A FastAPI backend and Streamlit frontend for Gmail-based email assistance with AI-generated replies. This is a migration from the original Spring Boot + React prototype, now simplified to use email IDs only (no index-based replies).

## Architecture
- **Backend**: FastAPI (`backend/app`)
- **Frontend**: Streamlit (`streamlit_app/app.py`)
- **Integrations**: Gmail API (OAuth2) + HTTP AI provider

## Prerequisites
- Python 3.10+
- Gmail OAuth credentials (`credentials.json`)
- Optional AI provider (e.g., Ollama)

## Setup
1. Create a virtual environment and install dependencies:
```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

2. Configure environment variables:
```bash
cp .env.example .env
```
Edit `.env` with your Gmail and AI settings.

3. Start the FastAPI backend:
```bash
uvicorn app.main:app --reload --app-dir backend
```

4. Start the Streamlit app:
```bash
streamlit run streamlit_app/app.py
```

## API Endpoints (FastAPI)
- `POST /api/emails/fetch/{limit}`: Fetch inbox emails
- `POST /api/emails/sent/{limit}`: Fetch sent emails
- `GET /api/emails/{emailId}`: Fetch a single email
- `GET /api/emails/{emailId}/thread`: Fetch thread
- `POST /api/emails/reply`: Generate and send AI reply (by `emailId`)

## Legacy Code
The original Spring Boot and React code has been moved to `legacy/` for reference.
