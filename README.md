# Email Assistant (Python + Streamlit + MCP)

A FastAPI backend and Streamlit frontend for Gmail-based email assistance with AI-generated replies. Includes a standalone MCP (Model Context Protocol) server for tool-based access from clients like Cursor.

## Architecture
- **Backend**: FastAPI (`backend/app`)
- **Frontend**: Streamlit (`streamlit_app/app.py`)
- **MCP Server**: Stdio JSON-RPC (`mcp_server/server.py`)
- **Integrations**: Gmail API (OAuth2) + HTTP AI provider

## Prerequisites
- Python 3.10+
- Gmail OAuth credentials (`credentials.json`)
- Optional AI provider (e.g., Ollama)

> Note: MCP operations that modify Gmail (labels/archive/delete) require the `gmail.modify` scope. You may be prompted to re-auth when scopes change.

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

## MCP Server (Cursor)
Run the MCP server from repo root:
```bash
python3 -m mcp_server.server
```

Cursor config (created at `.cursor/mcp.json`):
```json
{
  "mcpServers": {
    "email-assistant": {
      "command": "python3",
      "args": ["-m", "mcp_server.server"],
      "env": {}
    }
  }
}
```

## MCP Tools
- `list_inbox(limit, query)`
- `list_sent(limit, query)`
- `get_email(email_id)`
- `get_thread(thread_id)`
- `reply_email(email_id, instruction)`
- `search_emails(query, limit, label_ids)`
- `add_labels(email_id, label_ids)`
- `remove_labels(email_id, label_ids)`
- `archive_email(email_id)`
- `delete_email(email_id)`

## API Endpoints (FastAPI)
- `POST /api/emails/fetch/{limit}`: Fetch inbox emails
- `POST /api/emails/sent/{limit}`: Fetch sent emails
- `GET /api/emails/{emailId}`: Fetch a single email
- `GET /api/emails/{emailId}/thread`: Fetch thread
- `POST /api/emails/reply`: Generate and send AI reply (by `emailId`)
