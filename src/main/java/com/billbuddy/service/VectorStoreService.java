// VectorStoreService.java
package com.billbuddy.service;

import com.billbuddy.model.Plan;
import com.billbuddy.repository.PlanRepository;
import com.billbuddy.util.DocumentChunker;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VectorStoreService {

    private final PlanRepository planRepository;
    private final EmbeddingService embeddingService;
    private final DocumentChunker documentChunker;
    private final ObjectMapper objectMapper;

    public VectorStoreService(PlanRepository planRepository,
                              EmbeddingService embeddingService,
                              DocumentChunker documentChunker) {
        this.planRepository = planRepository;
        this.embeddingService = embeddingService;
        this.documentChunker = documentChunker;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public void indexPlan(Plan plan) {
        log.info("Indexing plan: {} - {}", plan.getProvider(), plan.getName());

        // Create searchable text
        String fullText = buildSearchableText(plan);

        // Generate embedding
        List<Double> embedding = embeddingService.generateEmbedding(fullText);

        // Store embedding as JSON
        try {
            plan.setEmbedding(objectMapper.writeValueAsString(embedding));
            planRepository.save(plan);
            log.info("Successfully indexed plan ID: {}", plan.getId());
        } catch (Exception e) {
            log.error("Error indexing plan", e);
            throw new RuntimeException("Failed to index plan", e);
        }
    }

    public List<Map.Entry<Plan, Double>> searchSimilar(String query, int topK) {
        log.info("Searching for similar plans to query: {}", query);

        // Generate query embedding
        List<Double> queryEmbedding = embeddingService.generateEmbedding(query);

        // Calculate similarity with all plans
        List<Plan> allPlans = planRepository.findAll();
        Map<Plan, Double> similarities = new HashMap<>();

        for (Plan plan : allPlans) {
            if (plan.getEmbedding() != null) {
                try {
                    List<Double> planEmbedding = objectMapper.readValue(
                            plan.getEmbedding(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Double.class)
                    );

                    double similarity = embeddingService.cosineSimilarity(queryEmbedding, planEmbedding);
                    similarities.put(plan, similarity);

                } catch (Exception e) {
                    log.warn("Error processing embedding for plan {}", plan.getId(), e);
                }
            }
        }

        // Return top K results
        return similarities.entrySet().stream()
                .sorted(Map.Entry.<Plan, Double>comparingByValue().reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    private String buildSearchableText(Plan plan) {
        return String.format(
                "Provider: %s. Plan: %s. Type: %s. Price: $%.2f/month. %s Features: %s. Best for: %s",
                plan.getProvider(),
                plan.getName(),
                plan.getType(),
                plan.getMonthlyPrice(),
                plan.getDescription(),
                plan.getFeatures(),
                plan.getBestFor()
        );
    }
}
