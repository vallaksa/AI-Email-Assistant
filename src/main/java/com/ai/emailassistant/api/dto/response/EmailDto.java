package com.ai.emailassistant.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for email representation in API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailDto {
    private int index;
    private String emailId;
    private String from;
    private String subject;
    private String snippet;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant receivedAt;
}
