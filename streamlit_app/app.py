import os
from datetime import datetime
from typing import Iterable

import requests
import streamlit as st
from dotenv import load_dotenv

load_dotenv()

API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8000")

EMAIL_COUNT_OPTIONS = [10 + (i * 5) for i in range(9)]
PAGE_SIZE_OPTIONS = [5, 10, 15, 20]


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


def normalize_text(value: str | None) -> str:
    return (value or "").lower()


def filter_emails(emails: Iterable[dict], query: str) -> list[dict]:
    if not query:
        return list(emails)
    query = query.lower().strip()
    filtered = []
    for email in emails:
        haystack = " ".join(
            [
                normalize_text(email.get("subject")),
                normalize_text(email.get("from")),
                normalize_text(email.get("snippet")),
                normalize_text(email.get("body")),
            ]
        )
        if query in haystack:
            filtered.append(email)
    return filtered


def paginate(items: list[dict], page: int, page_size: int) -> list[dict]:
    start = (page - 1) * page_size
    end = start + page_size
    return items[start:end]


def email_label(email: dict) -> str:
    subject = email.get("subject") or "(No Subject)"
    sender = email.get("from") or "Unknown"
    received = format_received_at(email.get("receivedAt", ""))
    snippet = (email.get("snippet") or "").strip()
    snippet = snippet[:80] + ("..." if len(snippet) > 80 else "")
    return f"{subject} — {sender} · {received} · {snippet}"


def render_email_list(emails: list[dict], tab_key: str):
    if not emails:
        st.info("No emails loaded yet.")
        return None

    search_query = st.text_input("Search", key=f"search_{tab_key}", placeholder="Search subject, sender, body")
    filtered = filter_emails(emails, search_query)
    st.caption(f"{len(filtered)} of {len(emails)} emails")

    page_size = st.selectbox("Page size", PAGE_SIZE_OPTIONS, index=1, key=f"page_size_{tab_key}")
    total_pages = max(1, (len(filtered) + page_size - 1) // page_size)
    page = st.number_input("Page", min_value=1, max_value=total_pages, value=1, step=1, key=f"page_{tab_key}")

    page_items = paginate(filtered, page, page_size)
    if not page_items:
        st.info("No emails on this page.")
        return None

    ids = [email["emailId"] for email in page_items]
    selected_id = st.selectbox(
        "Emails",
        ids,
        format_func=lambda eid: email_label(next(item for item in page_items if item["emailId"] == eid)),
        key=f"selected_{tab_key}",
    )

    return next((item for item in filtered if item["emailId"] == selected_id), None)


def render_email_detail(email: dict):
    st.subheader(email.get("subject") or "(No Subject)")
    st.caption(
        f"From: {email.get('from') or 'Unknown'} | Received: {format_received_at(email.get('receivedAt', ''))}"
    )

    show_html = st.toggle("Render HTML body", value=email.get("bodyType") == "html")
    if show_html and email.get("bodyType") == "html":
        st.markdown(email.get("body") or "", unsafe_allow_html=True)
    else:
        st.write(email.get("body") or email.get("snippet") or "")


def render_thread(email_id: str):
    st.markdown("#### Thread")
    if st.button("Refresh thread", key=f"refresh_thread_{email_id}"):
        st.session_state.thread_cache.pop(email_id, None)

    if email_id not in st.session_state.thread_cache:
        try:
            st.session_state.thread_cache[email_id] = api_get(f"/api/emails/{email_id}/thread")
        except Exception as exc:
            st.error(str(exc))
            return

    messages = st.session_state.thread_cache.get(email_id, [])
    if not messages:
        st.info("No thread messages.")
        return

    for msg in messages:
        label = "Sent" if msg.get("isSent") else "Received"
        title = f"{label}: {msg.get('subject') or '(No Subject)'} ({format_received_at(msg.get('receivedAt', ''))})"
        with st.expander(title):
            if msg.get("bodyType") == "html":
                st.markdown(msg.get("body") or "", unsafe_allow_html=True)
            else:
                st.write(msg.get("body") or "")


def render_reply(email_id: str):
    st.markdown("#### Reply Assistant")
    instruction = st.text_area("Instruction (optional)", key=f"instruction_{email_id}")
    cols = st.columns([1, 1, 2])
    if cols[0].button("Generate and Send", key=f"reply_{email_id}"):
        try:
            response = api_post(
                "/api/emails/reply",
                {"emailId": email_id, "userInstruction": instruction or None},
            )
            st.success("Reply sent")
            st.text_area("Reply preview", value=response["replyPreview"], height=150)
        except Exception as exc:
            st.error(str(exc))
    if cols[1].button("Clear", key=f"clear_reply_{email_id}"):
        st.session_state[f"instruction_{email_id}"] = ""


st.set_page_config(page_title="Email Assistant", layout="wide")

st.title("Email Assistant")

if "inbox_emails" not in st.session_state:
    st.session_state.inbox_emails = []
if "sent_emails" not in st.session_state:
    st.session_state.sent_emails = []
if "thread_cache" not in st.session_state:
    st.session_state.thread_cache = {}

with st.sidebar:
    st.header("Settings")
    st.text_input("API Base URL", value=API_BASE_URL, disabled=True)
    st.caption("Set API_BASE_URL in .env if needed")

inbox_tab, sent_tab = st.tabs(["Inbox", "Sent"])

with inbox_tab:
    st.subheader("Inbox")
    controls = st.columns([2, 2, 3])
    email_count = controls[0].selectbox("Emails to fetch", EMAIL_COUNT_OPTIONS, index=0, key="inbox_count")
    if controls[1].button("Fetch Inbox", key="fetch_inbox"):
        try:
            st.session_state.inbox_emails = api_post(f"/api/emails/fetch/{email_count}")
        except Exception as exc:
            st.error(str(exc))

    list_col, detail_col = st.columns([1, 2], gap="large")
    with list_col:
        selected = render_email_list(st.session_state.inbox_emails, "inbox")

    with detail_col:
        if selected:
            render_email_detail(selected)
            render_reply(selected["emailId"])
            render_thread(selected["emailId"])

with sent_tab:
    st.subheader("Sent")
    controls = st.columns([2, 2, 3])
    email_count = controls[0].selectbox("Emails to fetch", EMAIL_COUNT_OPTIONS, index=0, key="sent_count")
    if controls[1].button("Fetch Sent", key="fetch_sent"):
        try:
            st.session_state.sent_emails = api_post(f"/api/emails/sent/{email_count}")
        except Exception as exc:
            st.error(str(exc))

    list_col, detail_col = st.columns([1, 2], gap="large")
    with list_col:
        selected = render_email_list(st.session_state.sent_emails, "sent")

    with detail_col:
        if selected:
            render_email_detail(selected)
            render_reply(selected["emailId"])
            render_thread(selected["emailId"])
