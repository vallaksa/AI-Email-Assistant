package com.ai.emailassistant.infrastructure.email.gmail;

import com.ai.emailassistant.common.EmailConstants;
import com.ai.emailassistant.domain.model.EmailMessage;
import com.ai.emailassistant.domain.port.EmailProvider;
import com.ai.emailassistant.infrastructure.email.config.GmailProperties;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

/**
 * Gmail implementation of EmailProvider.
 * Handles Gmail API integration.
 * Infrastructure layer only - no business logic.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GmailEmailProvider implements EmailProvider {

    private static final String TEXT_PLAIN_MIME = "text/plain";
    private static final String TEXT_HTML_MIME = "text/html";

    private final GmailOAuthService gmailOAuthService;
    private final GmailProperties gmailProperties;
    private Gmail gmailService;

    private synchronized Gmail getGmailService() {
        if (gmailService == null) {
            gmailService = gmailOAuthService.initializeGmailService();
        }
        return gmailService;
    }

    @Override
    public List<EmailMessage> fetchLatest(int limit) {
        log.info("Fetching {} emails from Gmail", limit);

        try {
            Gmail service = getGmailService();
            
            // Test mode: return mock emails if Gmail service is unavailable
            if (service == null) {
                log.warn("Gmail unavailable. Using test emails.");
                return getTestEmails(limit);
            }

            // Gmail API supports up to 500 messages per request, but we cap at MAX_EMAIL_LIMIT (50)
            int maxResults = Math.min(limit, EmailConstants.MAX_EMAIL_LIMIT);
            var response = service.users().messages().list("me")
                    .setQ(EmailConstants.GMAIL_INBOX_QUERY)
                    .setMaxResults((long) maxResults)
                    .execute();

            List<EmailMessage> emails = new ArrayList<>();

            if (response.getMessages() != null) {
                int index = 1;
                for (Message msg : response.getMessages()) {
                    try {
                        Message fullMsg = service.users().messages()
                                .get("me", msg.getId())
                                .setFormat("full")
                                .execute();

                        EmailMessage email = parseMessage(fullMsg, index++);
                        if (email != null) {
                            emails.add(email);
                        }
                        if (emails.size() >= limit) break;
                    } catch (Exception e) {
                        log.debug("Failed to parse message: {}", e.getMessage());
                    }
                }
            }

            log.info("Fetched {} emails", emails.size());
            return emails;

        } catch (Exception e) {
            log.error("Error fetching emails", e);
            log.warn("Falling back to test emails due to error");
            return getTestEmails(limit);
        }
    }

    @Override
    public List<EmailMessage> fetchSent(int limit) {
        log.info("Fetching {} sent emails from Gmail", limit);

        try {
            Gmail service = getGmailService();
            
            // Test mode: return mock emails if Gmail service is unavailable
            if (service == null) {
                log.warn("Gmail unavailable. Using test emails.");
                return getTestEmails(limit);
            }

            // Gmail API supports up to 500 messages per request, but we cap at MAX_EMAIL_LIMIT (50)
            int maxResults = Math.min(limit, EmailConstants.MAX_EMAIL_LIMIT);
            var response = service.users().messages().list("me")
                    .setQ("in:sent")
                    .setMaxResults((long) maxResults)
                    .execute();

            List<EmailMessage> emails = new ArrayList<>();

            if (response.getMessages() != null) {
                int index = 1;
                for (Message msg : response.getMessages()) {
                    try {
                        Message fullMsg = service.users().messages()
                                .get("me", msg.getId())
                                .setFormat("full")
                                .execute();

                        EmailMessage email = parseMessage(fullMsg, index++);
                        if (email != null) {
                            emails.add(email);
                        }
                        if (emails.size() >= limit) break;
                    } catch (Exception e) {
                        log.debug("Failed to parse message: {}", e.getMessage());
                    }
                }
            }

            log.info("Fetched {} sent emails", emails.size());
            return emails;

        } catch (Exception e) {
            log.error("Error fetching sent emails", e);
            log.warn("Falling back to test emails due to error");
            return getTestEmails(limit);
        }
    }

    @Override
    public EmailMessage getEmail(String emailId) {
        log.info("Fetching email: {}", emailId);

        try {
            Gmail service = getGmailService();
            
            if (service == null) {
                log.warn("Gmail unavailable. Cannot fetch email.");
                return null;
            }

            Message fullMsg = service.users().messages()
                    .get("me", emailId)
                    .setFormat("full")
                    .execute();

            return parseMessage(fullMsg, 0);

        } catch (Exception e) {
            log.error("Error fetching email: {}", emailId, e);
            throw new com.ai.emailassistant.exception.ProviderException("Failed to fetch email: " + e.getMessage(), e);
        }
    }

    @Override
    public List<EmailMessage> getThread(String threadId) {
        log.info("Fetching thread: {}", threadId);

        try {
            Gmail service = getGmailService();
            
            if (service == null) {
                log.warn("Gmail unavailable. Cannot fetch thread.");
                return new ArrayList<>();
            }

            var thread = service.users().threads()
                    .get("me", threadId)
                    .setFormat("full")
                    .execute();

            List<EmailMessage> emails = new ArrayList<>();

            if (thread.getMessages() != null) {
                int index = 1;
                for (Message msg : thread.getMessages()) {
                    try {
                        EmailMessage email = parseMessage(msg, index++);
                        if (email != null) {
                            emails.add(email);
                        }
                    } catch (Exception e) {
                        log.debug("Failed to parse message in thread: {}", e.getMessage());
                    }
                }
            }

            log.info("Fetched {} messages from thread", emails.size());
            return emails;

        } catch (Exception e) {
            log.error("Error fetching thread: {}", threadId, e);
            throw new com.ai.emailassistant.exception.ProviderException("Failed to fetch thread: " + e.getMessage(), e);
        }
    }

    @Override
    public void reply(String emailId, String replyBody) {
        log.info("Sending reply to: {}", emailId);

        try {
            Gmail service = getGmailService();
            
            // Test mode: log but don't send if Gmail service is unavailable
            if (service == null) {
                log.warn("Gmail unavailable. Reply not sent (test mode).");
                return;
            }

            Message original = service.users().messages()
                    .get("me", emailId).setFormat("full").execute();

            String to = getHeader(original, "From");
            String subject = getHeader(original, "Subject");
            String threadId = original.getThreadId();

            MimeMessage email = new MimeMessage(Session.getDefaultInstance(new Properties()));
            email.setFrom(new InternetAddress(gmailProperties.getAccount().getEmail()));
            email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
            email.setSubject(EmailConstants.REPLY_SUBJECT_PREFIX + subject);
            email.setText(replyBody);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            email.writeTo(buffer);
            String encoded = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(buffer.toByteArray());

            Message message = new Message();
            message.setRaw(encoded);
            message.setThreadId(threadId);

            service.users().messages().send("me", message).execute();
            log.info("Reply sent successfully");

        } catch (Exception e) {
            log.error("Failed to send reply", e);
            log.warn("Reply not sent due to error (test mode)");
            // In test mode, we don't throw - just log the error
        }
    }

    private EmailMessage parseMessage(Message msg, int idx) {
        try {
            if (msg == null || msg.getId() == null) {
                log.debug("Message or message ID is null");
                return null;
            }

            String from = getHeader(msg, "From");
            String subject = getHeader(msg, "Subject");
            BodyResult bodyResult = getBodyAndType(msg);
            String fullBody = bodyResult.body;
            String bodyType = bodyResult.bodyType;
            Long dateValue = msg.getInternalDate();
            String threadId = msg.getThreadId();

            // Use snippet if body is null
            if (fullBody == null || fullBody.isBlank()) {
                String snippet = msg.getSnippet();
                fullBody = snippet != null && !snippet.isBlank() ? snippet : "";
            }

            // Create snippet from body (truncated version)
            String snippet = fullBody != null ? fullBody : "";
            if (!snippet.isBlank() && snippet.length() > EmailConstants.SNIPPET_MAX_LENGTH) {
                snippet = snippet.substring(0, EmailConstants.SNIPPET_MAX_LENGTH) + "...";
            }

            // Handle date - use current time if date is null or invalid
            Instant receivedAt = Instant.now();
            if (dateValue != null && dateValue > 0) {
                try {
                    receivedAt = Instant.ofEpochMilli(dateValue);
                } catch (Exception e) {
                    log.debug("Invalid date value: {}, using current time", dateValue);
                    // receivedAt already set to Instant.now()
                }
            } else {
                log.debug("Date is null or invalid, using current time");
                // receivedAt already set to Instant.now()
            }

            return EmailMessage.builder()
                    .index(idx)
                    .emailId(msg.getId())
                    .from(from != null && !from.isBlank() ? from : EmailConstants.UNKNOWN_SENDER)
                    .subject(subject != null && !subject.isBlank() ? subject : EmailConstants.NO_SUBJECT)
                    .snippet(snippet)
                    .body(fullBody)
                    .bodyType(bodyType != null ? bodyType : "text")
                    .threadId(threadId != null ? threadId : "")
                    .receivedAt(receivedAt)
                    .build();

        } catch (Exception e) {
            log.warn("Parse error for message: {}", msg != null ? msg.getId() : "null", e);
            return null;
        }
    }

    private String getHeader(Message msg, String name) {
        try {
            return msg.getPayload().getHeaders().stream()
                    .filter(h -> name.equalsIgnoreCase(h.getName()))
                    .map(com.google.api.services.gmail.model.MessagePartHeader::getValue)
                    .findFirst().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper class to hold body content and type.
     */
    private static class BodyResult {
        String body;
        String bodyType;
        
        BodyResult(String body, String bodyType) {
            this.body = body;
            this.bodyType = bodyType;
        }
    }

    /**
     * Get email body and determine its type (HTML or text).
     * Prefers HTML over plain text for rich email display.
     *
     * @param msg Gmail message
     * @return BodyResult containing body content and type ("html" or "text")
     */
    private BodyResult getBodyAndType(Message msg) {
        try {
            MessagePart payload = msg.getPayload();
            if (payload == null) {
                return new BodyResult(null, "text");
            }

            // Handle simple messages without parts
            if (payload.getParts() == null || payload.getParts().isEmpty()) {
                String body = decode(payload.getBody());
                if (body != null && !body.isBlank()) {
                    String mimeType = payload.getMimeType();
                    if (TEXT_HTML_MIME.equals(mimeType)) {
                        // Return HTML as-is (don't strip)
                        return new BodyResult(body, "html");
                    }
                    return new BodyResult(body, "text");
                }
                return new BodyResult(null, "text");
            }

            // Handle multipart messages - collect both plain text and HTML
            StringBuilder plainTextBody = new StringBuilder();
            StringBuilder htmlBody = new StringBuilder();

            for (MessagePart part : payload.getParts()) {
                if (part == null) continue;
                
                String mimeType = part.getMimeType();
                if (mimeType == null) continue;

                // Handle nested parts (multipart/alternative, etc.)
                if (mimeType.startsWith("multipart/")) {
                    if (part.getParts() != null) {
                        for (MessagePart nestedPart : part.getParts()) {
                            if (nestedPart == null) continue;
                            String nestedMimeType = nestedPart.getMimeType();
                            if (nestedMimeType == null) continue;

                            if (TEXT_PLAIN_MIME.equals(nestedMimeType)) {
                                String text = decode(nestedPart.getBody());
                                if (text != null && !text.isBlank()) {
                                    plainTextBody.append(text);
                                }
                            } else if (TEXT_HTML_MIME.equals(nestedMimeType)) {
                                String html = decode(nestedPart.getBody());
                                if (html != null && !html.isBlank()) {
                                    htmlBody.append(html);
                                }
                            }
                        }
                    }
                } else if (TEXT_PLAIN_MIME.equals(mimeType)) {
                    String text = decode(part.getBody());
                    if (text != null && !text.isBlank()) {
                        plainTextBody.append(text);
                    }
                } else if (TEXT_HTML_MIME.equals(mimeType)) {
                    String html = decode(part.getBody());
                    if (html != null && !html.isBlank()) {
                        htmlBody.append(html);
                    }
                }
            }

            // Prefer HTML over plain text for rich email display
            if (htmlBody.length() > 0) {
                return new BodyResult(htmlBody.toString(), "html");
            }
            if (plainTextBody.length() > 0) {
                return new BodyResult(plainTextBody.toString(), "text");
            }
        } catch (Exception e) {
            log.debug("Body extract error: {}", e.getMessage());
        }
        return new BodyResult(null, "text");
    }

    /**
     * @deprecated Use getBodyAndType() instead
     */
    @Deprecated
    private String getBody(Message msg) {
        return getBodyAndType(msg).body;
    }

    private String decode(com.google.api.services.gmail.model.MessagePartBody body) {
        if (body == null || body.getData() == null) return null;
        try {
            return new String(Base64.getUrlDecoder().decode(body.getData()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private String stripHtml(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }
        
        // Remove HTML tags but preserve text content
        String text = html
                .replaceAll("<script[^>]*>.*?</script>", "") // Remove script tags
                .replaceAll("<style[^>]*>.*?</style>", "") // Remove style tags
                .replaceAll("<[^>]+>", " ") // Remove all HTML tags
                .replaceAll("&nbsp;", " ")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&")
                .replaceAll("&quot;", "\"")
                .replaceAll("&#39;", "'")
                .replaceAll("&apos;", "'")
                .replaceAll("\\s+", " ") // Normalize whitespace
                .trim();
        
        return text;
    }

    /**
     * Generate test emails for development/testing when Gmail is unavailable.
     *
     * @param limit Number of test emails to generate
     * @return List of test EmailMessage objects
     */
    private List<EmailMessage> getTestEmails(int limit) {
        List<EmailMessage> emails = new ArrayList<>();
        int testEmailCount = Math.min(limit, 3);
        
        for (int i = 1; i <= testEmailCount; i++) {
            emails.add(EmailMessage.builder()
                    .index(i)
                    .emailId("test_" + i)
                    .from("test" + i + "@example.com")
                    .subject("Test Email " + i)
                    .snippet("Test email for development. This is a mock email used when Gmail credentials are not available.")
                    .body("Test email for development. This is a mock email used when Gmail credentials are not available. Full body content here.")
                    .bodyType("text")
                    .threadId("test_thread_" + i)
                    .receivedAt(Instant.now().minusSeconds(3600L * i))
                    .build());
        }
        
        log.info("Generated {} test emails", emails.size());
        return emails;
    }
}
