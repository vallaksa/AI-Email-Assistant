import os
from datetime import datetime
import requests
import streamlit as st
from dotenv import load_dotenv

load_dotenv()

API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8000")

EMAIL_COUNT_OPTIONS = [10 + (i * 5) for i in range(9)]


def api_post(path: str, payload: dict | None = None):
    url = f"{API_BASE_URL}{path}"
    response = requests.post(url, json=payload, timeout=30)
    data = response.json()
    if not response.ok or not data.get("success", False):
        error = data.get("error") or f"Request failed with status {response.status_code}"
        raise RuntimeError(error)
    return data["data"]


def api_get(path: str):
    url = f"{API_BASE_URL}{path}"
    response = requests.get(url, timeout=30)
    data = response.json()
    if not response.ok or not data.get("success", False):
        error = data.get("error") or f"Request failed with status {response.status_code}"
        raise RuntimeError(error)
    return data["data"]


def format_received_at(value: str) -> str:
    try:
        dt = datetime.fromisoformat(value.replace("Z", "+00:00"))
        return dt.strftime("%b %d, %Y %I:%M %p")
    except Exception:
        return value


def render_email_list(emails: list[dict]):
    if not emails:
        st.info("No emails loaded yet.")
        return None

    options = [
        f"{email['subject']} — {email['from']} ({format_received_at(email['receivedAt'])})"
        for email in emails
    ]
    selected_idx = st.radio("", list(range(len(options))), format_func=lambda i: options[i])
    return emails[selected_idx]


def render_email_detail(email: dict):
    st.subheader(email["subject"])
    st.caption(f"From: {email['from']} | Received: {format_received_at(email['receivedAt'])}")

    if email.get("bodyType") == "html":
        st.markdown(email.get("body") or "", unsafe_allow_html=True)
    else:
        st.write(email.get("body") or email.get("snippet") or "")


def render_thread(email_id: str):
    st.markdown("#### Thread")
    try:
        messages = api_get(f"/api/emails/{email_id}/thread")
    except Exception as exc:
        st.error(str(exc))
        return

    if not messages:
        st.info("No thread messages.")
        return

    for msg in messages:
        label = "Sent" if msg.get("isSent") else "Received"
        with st.expander(f"{label}: {msg['subject']} ({format_received_at(msg['receivedAt'])})"):
            if msg.get("bodyType") == "html":
                st.markdown(msg.get("body") or "", unsafe_allow_html=True)
            else:
                st.write(msg.get("body") or "")


def render_reply(email_id: str):
    st.markdown("#### Reply Assistant")
    instruction = st.text_area("Instruction (optional)", key=f"instruction_{email_id}")
    if st.button("Generate and Send Reply", key=f"reply_{email_id}"):
        try:
            response = api_post(
                "/api/emails/reply",
                {"emailId": email_id, "userInstruction": instruction or None},
            )
            st.success("Reply sent")
            st.text_area("Reply preview", value=response["replyPreview"], height=150)
        except Exception as exc:
            st.error(str(exc))


st.set_page_config(page_title="Email Assistant", layout="wide")

st.title("Email Assistant")

if "inbox_emails" not in st.session_state:
    st.session_state.inbox_emails = []
if "sent_emails" not in st.session_state:
    st.session_state.sent_emails = []

inbox_tab, sent_tab = st.tabs(["Inbox", "Sent"])

with inbox_tab:
    st.subheader("Inbox")
    email_count = st.selectbox("Emails to fetch", EMAIL_COUNT_OPTIONS, index=0, key="inbox_count")
    if st.button("Fetch Inbox", key="fetch_inbox"):
        try:
            st.session_state.inbox_emails = api_post(f"/api/emails/fetch/{email_count}")
        except Exception as exc:
            st.error(str(exc))

    selected = render_email_list(st.session_state.inbox_emails)
    if selected:
        render_email_detail(selected)
        render_reply(selected["emailId"])
        render_thread(selected["emailId"])

with sent_tab:
    st.subheader("Sent")
    email_count = st.selectbox("Emails to fetch", EMAIL_COUNT_OPTIONS, index=0, key="sent_count")
    if st.button("Fetch Sent", key="fetch_sent"):
        try:
            st.session_state.sent_emails = api_post(f"/api/emails/sent/{email_count}")
        except Exception as exc:
            st.error(str(exc))

    selected = render_email_list(st.session_state.sent_emails)
    if selected:
        render_email_detail(selected)
        render_reply(selected["emailId"])
        render_thread(selected["emailId"])
