// EmbeddingService.java
package com.billbuddy.service;

import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@Slf4j
public class EmbeddingService {

    private final OpenAiService openAiService;

    public EmbeddingService(@Value("${openai.api.key}") String apiKey) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("openai.api.key is empty - check config sources");
        }

        String trimmed = apiKey.trim();
        String suffix = trimmed.length() >= 4 ? trimmed.substring(trimmed.length() - 4) : trimmed;
        log.info("OpenAI key loaded (len={}, suffix=****{})", trimmed.length(), suffix);

        this.openAiService = new OpenAiService(trimmed);

//        this.openAiService = new OpenAiService(apiKey);
    }

    public List<Double> generateEmbedding(String text) {
        log.debug("Generating embedding for text of length: {}", text.length());

        try {
            EmbeddingRequest request = EmbeddingRequest.builder()
                    .model("text-embedding-ada-002")
                    .input(List.of(text))
                    .build();

            var result = openAiService.createEmbeddings(request);
            return result.getData().get(0).getEmbedding();

        } catch (Exception e) {
            log.error("Error generating embedding", e);
            throw new RuntimeException("Failed to generate embedding", e);
        }
    }

    public double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("Vectors must have same dimension");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += Math.pow(vec1.get(i), 2);
            norm2 += Math.pow(vec2.get(i), 2);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
