package com.ai.emailassistant.model.RequestResponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OllamaRequest {
    private String model;
    private String prompt;
    private boolean stream;
    private Double temperature;

    @JsonProperty("num_predict")
    private Integer numPredict;
}