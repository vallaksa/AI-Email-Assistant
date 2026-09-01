import os
from dataclasses import dataclass
from dotenv import load_dotenv

load_dotenv()


@dataclass(frozen=True)
class Settings:
    gmail_email: str | None = os.getenv("GMAIL_EMAIL")
    gmail_client_id: str | None = os.getenv("GMAIL_CLIENT_ID")
    gmail_client_secret: str | None = os.getenv("GMAIL_CLIENT_SECRET")

    ai_api_url: str | None = os.getenv("AI_API_URL")
    ai_api_key: str | None = os.getenv("AI_API_KEY")
    ai_auth_header: str | None = os.getenv("AI_AUTH_HEADER")
    ai_auth_prefix: str | None = os.getenv("AI_AUTH_PREFIX")
    ai_content_type: str = os.getenv("AI_CONTENT_TYPE", "application/json")
    ai_model_name: str | None = os.getenv("AI_MODEL_NAME", "mistral")
    ai_connect_timeout_ms: int = int(os.getenv("AI_CONNECT_TIMEOUT", "10000"))
    ai_read_timeout_ms: int = int(os.getenv("AI_READ_TIMEOUT", "60000"))

    email_min_limit: int = 1
    email_max_limit: int = 50
    email_default_limit: int = 10
    reply_preview_length: int = 200
    snippet_max_length: int = 200

    gmail_credentials_file: str = os.getenv("GMAIL_CREDENTIALS_FILE", "credentials.json")
    gmail_tokens_dir: str = os.getenv("GMAIL_TOKENS_DIR", "tokens")
    gmail_oauth_port: int = int(os.getenv("GMAIL_OAUTH_PORT", "8888"))


settings = Settings()
