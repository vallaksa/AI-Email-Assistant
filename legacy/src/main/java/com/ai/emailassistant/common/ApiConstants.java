package com.ai.emailassistant.common;

/**
 * Constants related to API operations.
 */
public final class ApiConstants {
    private ApiConstants() {
        // Prevent instantiation
    }

    // API paths
    public static final String API_BASE_PATH = "/api/emails";
    public static final String FETCH_EMAILS_PATH = "/fetch";
    public static final String FETCH_SENT_EMAILS_PATH = "/sent";
    public static final String REPLY_EMAIL_PATH = "/reply";

    // Response messages
    public static final String MSG_EMAILS_FETCHED = "Successfully fetched emails";
    public static final String MSG_SENT_EMAILS_FETCHED = "Successfully fetched sent emails";
    public static final String MSG_REPLY_SENT = "Reply sent successfully";

    // Error messages
    public static final String ERR_EMAIL_NOT_FOUND = "Email not found at index: %d";
    public static final String ERR_INVALID_LIMIT = "Limit must be between %d and %d";
    public static final String ERR_INVALID_INDEX = "Index must be greater than 0";

    // HTTP defaults
    public static final String DEFAULT_CONTENT_TYPE = "application/json";
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 10000;
    public static final int DEFAULT_READ_TIMEOUT_MS = 60000;
}
