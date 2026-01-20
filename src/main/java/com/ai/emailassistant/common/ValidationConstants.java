package com.ai.emailassistant.common;

/**
 * Constants for validation messages and rules.
 */
public final class ValidationConstants {
    private ValidationConstants() {
        // Prevent instantiation
    }

    // Validation error messages
    public static final String ERR_INVALID_EMAIL_LIMIT = "Limit must be between 1 and 50";
    public static final String ERR_INVALID_EMAIL_INDEX = "Index must be greater than 0";
    public static final String ERR_EMAIL_NOT_FOUND_FORMAT = "Email not found at index: %d";

    // Field validation messages
    public static final String ERR_INDEX_REQUIRED = "Index is required";
    public static final String ERR_INDEX_MIN = "Index must be greater than 0";
}
