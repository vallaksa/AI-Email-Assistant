package com.ai.emailassistant.infrastructure.gmail;

import com.ai.emailassistant.domain.model.EmailMessage;
import com.ai.emailassistant.domain.ports.EmailProvider;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/**
 * Gmail implementation of EmailProvider.
 * Handles OAuth2 authentication and Gmail API integration.
 * Infrastructure layer only - no business logic.
 */
@Slf4j
@Component
public class GmailEmailProvider implements EmailProvider {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIR = "tokens";
    private static final List<String> SCOPES = Arrays.asList(
            GmailScopes.GMAIL_READONLY,
            GmailScopes.GMAIL_SEND
    );

    @Value("${GMAIL_EMAIL}")
    private String userEmail;

    private Gmail gmailService;

    private synchronized Gmail getGmailService() {
        if (gmailService != null) {
            return gmailService;
        }

        log.info("Initializing Gmail service for: {}", userEmail);

        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            File credFile = new File("credentials.json");

            if (!credFile.exists()) {
                log.warn("credentials.json not found. Using test mode.");
                return null;
            }

            GoogleClientSecrets secrets = GoogleClientSecrets.load(
                    JSON_FACTORY, new FileReader(credFile));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, secrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIR)))
                    .setAccessType("offline")
                    .build();

            Credential credential = new AuthorizationCodeInstalledApp(
                    flow,
                    new LocalServerReceiver.Builder().setPort(8888).build()
            ).authorize("user");

            gmailService = new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName("Email-Assistant")
                    .build();

            log.info("Gmail service initialized");
            return gmailService;

        } catch (Exception e) {
            log.error("Failed to init Gmail service", e);
            return null;
        }
    }

    @Override
    public List<EmailMessage> fetchLatest(int limit) {
        log.info("Fetching {} emails from Gmail", limit);

        try {
            Gmail service = getGmailService();
            if (service == null) {
                log.warn("Gmail unavailable. Using test emails.");
                return getTestEmails(limit);
            }

            var response = service.users().messages().list("me")
                    .setQ("in:inbox")
                    .setMaxResults((long) Math.min(limit, 10))
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
            return getTestEmails(limit);
        }
    }

    @Override
    public void reply(String emailId, String replyBody) {
        log.info("Sending reply to: {}", emailId);

        try {
            Gmail service = getGmailService();
            if (service == null) {
                log.warn("Gmail unavailable. Reply not sent.");
                return;
            }

            Message original = service.users().messages()
                    .get("me", emailId).setFormat("full").execute();

            String to = getHeader(original, "From");
            String subject = getHeader(original, "Subject");
            String threadId = original.getThreadId();

            MimeMessage email = new MimeMessage(Session.getDefaultInstance(new Properties()));
            email.setFrom(new InternetAddress(userEmail));
            email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
            email.setSubject("Re: " + subject);
            email.setText(replyBody);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            email.writeTo(buffer);
            String encoded = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(buffer.toByteArray());

            Message message = new Message();
            message.setRaw(encoded);
            message.setThreadId(threadId);

            service.users().messages().send("me", message).execute();
            log.info("Reply sent");

        } catch (Exception e) {
            log.error("Failed to send reply", e);
            throw new RuntimeException("Failed to send reply: " + e.getMessage(), e);
        }
    }

    private EmailMessage parseMessage(Message msg, int idx) {
        try {
            String from = getHeader(msg, "From");
            String subject = getHeader(msg, "Subject");
            String body = getBody(msg);
            long date = msg.getInternalDate();

            if (body == null) body = msg.getSnippet();
            if (body != null && body.length() > 200) {
                body = body.substring(0, 200) + "...";
            }

            return EmailMessage.builder()
                    .index(idx)
                    .emailId(msg.getId())
                    .from(from != null ? from : "Unknown")
                    .subject(subject != null ? subject : "(No Subject)")
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
                if ("text/plain".equals(p.getMimeType())) {
                    return decode(p.getBody());
                }
            }

            for (MessagePart p : payload.getParts()) {
                if ("text/html".equals(p.getMimeType())) {
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

    private List<EmailMessage> getTestEmails(int limit) {
        List<EmailMessage> emails = new ArrayList<>();
        for (int i = 1; i <= Math.min(limit, 3); i++) {
            emails.add(EmailMessage.builder()
                    .index(i)
                    .emailId("test_" + i)
                    .from("test" + i + "@example.com")
                    .subject("Test Email " + i)
                    .snippet("Test email for development")
                    .receivedAt(Instant.now().minusSeconds(3600L * i))
                    .build());
        }
        return emails;
    }
}
