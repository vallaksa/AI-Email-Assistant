package com.ai.emailassistant.model.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request model for fetching emails.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchEmailsRequest {
    private Integer limit;
}
