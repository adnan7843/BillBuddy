// ComparisonService.java
package com.billbuddy.service;

import com.billbuddy.model.QueryRequest;
import com.billbuddy.model.QueryResponse;
import com.billbuddy.util.ObservabilityLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ComparisonService {

    private final RAGService ragService;
    private final ObservabilityLogger observabilityLogger;

    public ComparisonService(RAGService ragService, ObservabilityLogger observabilityLogger) {
        this.ragService = ragService;
        this.observabilityLogger = observabilityLogger;
    }

    public QueryResponse processQuery(QueryRequest request) {
        long startTime = System.currentTimeMillis();
        String sessionId = request.getSessionId() != null ?
                request.getSessionId() : UUID.randomUUID().toString();

        log.info("Processing query for session: {}", sessionId);
        observabilityLogger.logQuery(sessionId, request.getQuery());

        try {
            // Use RAG to generate answer
            Map<String, Object> ragResult = ragService.generateAnswer(
                    request.getQuery(),
                    request.getMaxResults()
            );

            // Parse structured response
            String answer = (String) ragResult.get("answer");
            QueryResponse response = parseResponse(answer, ragResult, sessionId);

            long processingTime = System.currentTimeMillis() - startTime;
            response.setProcessingTimeMs(processingTime);

            observabilityLogger.logResponse(sessionId, response);

            return response;

        } catch (Exception e) {
            log.error("Error processing query", e);
            observabilityLogger.logError(sessionId, e);
            throw new RuntimeException("Failed to process query", e);
        }
    }

    private QueryResponse parseResponse(String answer, Map<String, Object> ragResult, String sessionId) {
        // Extract structured information using regex
        String recommendation = extractSection(answer, "RECOMMENDATION:");
        String explanation = extractSection(answer, "EXPLANATION:");
        String monthlyCostStr = extractSection(answer, "MONTHLY COST:");
        String tradeoffsStr = extractSection(answer, "TRADEOFFS:");

        Double monthlyCost = extractCost(monthlyCostStr);
        List<String> tradeoffs = Arrays.asList(tradeoffsStr.split("\n"));

        return QueryResponse.builder()
                .recommendation(recommendation)
                .explanation(explanation)
                .estimatedMonthlyCost(monthlyCost)
                .tradeoffs(tradeoffs)
                .citations((List) ragResult.get("citations"))
                .sessionId(sessionId)
                .build();
    }

    private String extractSection(String text, String marker) {
        Pattern pattern = Pattern.compile(marker + "\\s*(.+?)(?=\\n[A-Z]+:|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    private Double extractCost(String text) {
        Pattern pattern = Pattern.compile("\\$?([0-9]+\\.?[0-9]*)");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : null;
    }
}
