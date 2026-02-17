from app.models import EmailMessage


def build_prompt(email: EmailMessage, user_instruction: str | None) -> str:
    prompt = []
    prompt.append(
        "You are a professional email assistant. Generate a concise, polite, and professional email reply.\n"
    )
    prompt.append("\n=== ORIGINAL EMAIL ===\n")
    prompt.append(f"From: {email.from_addr}\n")
    prompt.append(f"Subject: {email.subject}\n\n")
    prompt.append("Body:\n")
    prompt.append(email.snippet)
    prompt.append("\n\n")

    if user_instruction and user_instruction.strip():
        prompt.append("=== INSTRUCTIONS ===\n")
        prompt.append(user_instruction.strip())
        prompt.append("\n\n")

    prompt.append("=== REPLY ===\n")
    prompt.append("Generate ONLY the email body (no subject, no greeting, no closing):\n")

    return "".join(prompt)
