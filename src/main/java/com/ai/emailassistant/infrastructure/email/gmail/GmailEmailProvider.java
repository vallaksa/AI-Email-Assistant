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

            var response = service.users().messages().list("me")
                    .setQ(EmailConstants.GMAIL_INBOX_QUERY)
                    .setMaxResults((long) Math.min(limit, EmailConstants.GMAIL_MAX_RESULTS))
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
            String from = getHeader(msg, "From");
            String subject = getHeader(msg, "Subject");
            String body = getBody(msg);
            long date = msg.getInternalDate();

            if (body == null) body = msg.getSnippet();
            if (body != null && body.length() > EmailConstants.SNIPPET_MAX_LENGTH) {
                body = body.substring(0, EmailConstants.SNIPPET_MAX_LENGTH) + "...";
            }

            return EmailMessage.builder()
                    .index(idx)
                    .emailId(msg.getId())
                    .from(from != null ? from : EmailConstants.UNKNOWN_SENDER)
                    .subject(subject != null ? subject : EmailConstants.NO_SUBJECT)
                    .snippet(body != null ? body : "")
                    .receivedAt(Instant.ofEpochMilli(date))
                    .build();

        } catch (Exception e) {
            log.debug("Parse error: {}", e.getMessage());
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

    private String getBody(Message msg) {
        try {
            MessagePart payload = msg.getPayload();

            if (payload.getParts() == null) {
                return decode(payload.getBody());
            }

            for (MessagePart p : payload.getParts()) {
                if (TEXT_PLAIN_MIME.equals(p.getMimeType())) {
                    return decode(p.getBody());
                }
            }

            for (MessagePart p : payload.getParts()) {
                if (TEXT_HTML_MIME.equals(p.getMimeType())) {
                    String html = decode(p.getBody());
                    return html != null ? stripHtml(html) : null;
                }
            }
        } catch (Exception e) {
            log.debug("Body extract error");
        }
        return null;
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
        return html.replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&");
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
                    .receivedAt(Instant.now().minusSeconds(3600L * i))
                    .build());
        }
        
        log.info("Generated {} test emails", emails.size());
        return emails;
    }
}
