// RAGService.java
package com.billbuddy.service;

import com.billbuddy.model.Citation;
import com.billbuddy.model.Plan;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RAGService {

    private final VectorStoreService vectorStoreService;
    private final OpenAiService openAiService;

    public RAGService(VectorStoreService vectorStoreService,
                      @Value("${openai.api.key}") String apiKey) {
        this.vectorStoreService = vectorStoreService;
        this.openAiService = new OpenAiService(apiKey);
    }

    public Map<String, Object> generateAnswer(String query, int topK) {
        log.info("Generating RAG answer for query: {}", query);
        long startTime = System.currentTimeMillis();

        // Retrieve relevant documents
        List<Map.Entry<Plan, Double>> relevantPlans =
                vectorStoreService.searchSimilar(query, topK);

        // Build context from retrieved plans
        String context = buildContext(relevantPlans);

        // Generate answer using GPT
        String prompt = buildPrompt(query, context);
        String answer = callOpenAI(prompt);

        // Extract citations
        List<Citation> citations = buildCitations(relevantPlans);

        long processingTime = System.currentTimeMillis() - startTime;

        return Map.of(
                "answer", answer,
                "citations", citations,
                "processingTimeMs", processingTime,
                "retrievedDocs", relevantPlans.size()
        );
    }

    private String buildContext(List<Map.Entry<Plan, Double>> plans) {
        return plans.stream()
                .map(entry -> {
                    Plan plan = entry.getKey();
                    return String.format(
                            "[%s - %s]\nPrice: $%.2f/month\n%s\nFeatures: %s\nBest for: %s",
                            plan.getProvider(),
                            plan.getName(),
                            plan.getMonthlyPrice(),
                            plan.getDescription(),
                            plan.getFeatures(),
                            plan.getBestFor()
                    );
                })
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    private String buildPrompt(String query, String context) {
        return String.format("""
            You are BillBuddy, an expert assistant helping customers choose the best utility plans.
            
            User Question: %s
            
            Available Plans:
            %s
            
            Instructions:
            1. Recommend the BEST plan based on the user's needs
            2. Explain WHY it's the best choice
            3. Mention key tradeoffs or alternatives
            4. Provide estimated monthly cost
            5. Be specific and cite plan names
            6. If information is unclear, ask clarifying questions
            
            Format your response as:
            RECOMMENDATION: [Plan name]
            EXPLANATION: [Why it's best]
            MONTHLY COST: [Estimate]
            TRADEOFFS: [Key considerations]
            """, query, context);
    }

    private String callOpenAI(String prompt) {
        try {
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-4")
                    .messages(List.of(
                            new ChatMessage("system", "You are a helpful utility plan comparison assistant."),
                            new ChatMessage("user", prompt)
                    ))
                    .temperature(0.7)
                    .maxTokens(800)
                    .build();

            var response = openAiService.createChatCompletion(request);
            return response.getChoices().get(0).getMessage().getContent();

        } catch (Exception e) {
            log.error("Error calling OpenAI", e);
            throw new RuntimeException("Failed to generate response", e);
        }
    }

    private List<Citation> buildCitations(List<Map.Entry<Plan, Double>> plans) {
        return plans.stream()
                .map(entry -> Citation.builder()
                        .provider(entry.getKey().getProvider())
                        .planName(entry.getKey().getName())
                        .relevantText(entry.getKey().getDescription())
                        .relevanceScore(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}
