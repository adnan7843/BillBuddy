// QueryResponse.java
package com.billbuddy.model;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class QueryResponse {
    private String recommendation;
    private String explanation;
    private Double estimatedMonthlyCost;
    private List<String> tradeoffs;
    private List<Citation> citations;
    private String sessionId;
    private Long processingTimeMs;
}
