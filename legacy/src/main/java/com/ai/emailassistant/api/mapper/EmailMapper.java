package com.ai.emailassistant.api.mapper;

import com.ai.emailassistant.api.dto.response.EmailDto;
import com.ai.emailassistant.api.dto.response.ThreadMessageDto;
import com.ai.emailassistant.domain.model.EmailMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between domain models and DTOs.
 */
@Component
public class EmailMapper {

    /**
     * Convert EmailMessage (domain) to EmailDto (API).
     *
     * @param email Domain model
     * @return API DTO
     */
    public EmailDto toDto(EmailMessage email) {
        if (email == null) {
            return null;
        }
        return EmailDto.builder()
            .index(email.getIndex())
            .emailId(email.getEmailId())
            .from(email.getFrom())
            .subject(email.getSubject())
            .snippet(email.getSnippet())
            .body(email.getBody())
            .bodyType(email.getBodyType())
            .threadId(email.getThreadId())
            .receivedAt(email.getReceivedAt())
            .build();
    }

    /**
     * Convert list of EmailMessage to list of EmailDto.
     *
     * @param emails List of domain models
     * @return List of API DTOs
     */
    public List<EmailDto> toDtoList(List<EmailMessage> emails) {
        if (emails == null) {
            return null;
        }
        return emails.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Convert list of EmailMessage to list of ThreadMessageDto.
     * Determines if message was sent by user by comparing "from" field with user email.
     *
     * @param emails List of domain models
     * @param userEmail User's email address
     * @return List of ThreadMessageDto
     */
    public List<ThreadMessageDto> toThreadMessageDtoList(List<EmailMessage> emails, String userEmail) {
        if (emails == null) {
            return null;
        }
        return emails.stream()
            .map(email -> {
                // Determine if message was sent by user
                boolean isSent = userEmail != null && 
                    email.getFrom() != null && 
                    email.getFrom().toLowerCase().contains(userEmail.toLowerCase());
                
                return ThreadMessageDto.builder()
                    .emailId(email.getEmailId())
                    .from(email.getFrom())
                    .subject(email.getSubject())
                    .body(email.getBody())
                    .bodyType(email.getBodyType())
                    .isSent(isSent)
                    .receivedAt(email.getReceivedAt())
                    .build();
            })
            .collect(Collectors.toList());
    }
}
