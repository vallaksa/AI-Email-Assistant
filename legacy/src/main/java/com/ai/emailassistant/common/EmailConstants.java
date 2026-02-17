package com.ai.emailassistant.common;

/**
 * Constants related to email operations.
 */
public final class EmailConstants {
    private EmailConstants() {
        // Prevent instantiation
    }

    // Email fetch limits
    public static final int MAX_EMAIL_LIMIT = 50;
    public static final int DEFAULT_EMAIL_LIMIT = 10;
    public static final int MIN_EMAIL_LIMIT = 1;

    // Gmail query strings
    public static final String GMAIL_INBOX_QUERY = "in:inbox";

    // Email prefixes
    public static final String REPLY_SUBJECT_PREFIX = "Re: ";

    // Gmail scopes
    public static final String GMAIL_SCOPE_READONLY = "https://www.googleapis.com/auth/gmail.readonly";
    public static final String GMAIL_SCOPE_SEND = "https://www.googleapis.com/auth/gmail.send";
    public static final String GMAIL_SCOPE_MODIFY = "https://www.googleapis.com/auth/gmail.modify";

    // Default values
    public static final String UNKNOWN_SENDER = "Unknown";
    public static final String NO_SUBJECT = "(No Subject)";
    public static final int REPLY_PREVIEW_LENGTH = 200;
    public static final int SNIPPET_MAX_LENGTH = 200;
    public static final int EMAIL_BODY_PREVIEW_LENGTH = 200;

    // Gmail OAuth configuration
    public static final String GMAIL_TOKENS_DIR = "tokens";
    public static final String GMAIL_CREDENTIALS_FILE = "credentials.json";
    public static final int GMAIL_OAUTH_PORT = 8888;
    public static final int GMAIL_MAX_RESULTS = 10;
}
