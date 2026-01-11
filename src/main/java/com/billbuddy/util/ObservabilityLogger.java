// ObservabilityLogger.java
package com.billbuddy.util;

import com.billbuddy.model.QueryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class ObservabilityLogger {

    private final ObjectMapper objectMapper;
    private static final String LOG_FILE = "billbuddy-queries.log";

    public ObservabilityLogger() {
        this.objectMapper = new ObjectMapper();
    }

    public void logQuery(String sessionId, String query) {
        String logEntry = String.format(
                "[%s] SESSION: %s | QUERY: %s\n",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                sessionId,
                query
        );
        writeLog(logEntry);
        log.info("Query logged: {}", sessionId);
    }

    public void logResponse(String sessionId, QueryResponse response) {
        try {
            String logEntry = String.format(
                    "[%s] SESSION: %s | RESPONSE: %s | LATENCY: %dms\n",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    sessionId,
                    objectMapper.writeValueAsString(response),
                    response.getProcessingTimeMs()
            );
            writeLog(logEntry);
            log.info("Response logged: {} ({}ms)", sessionId, response.getProcessingTimeMs());
        } catch (Exception e) {
            log.error("Error logging response", e);
        }
    }

    public void logError(String sessionId, Exception e) {
        String logEntry = String.format(
                "[%s] SESSION: %s | ERROR: %s\n",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                sessionId,
                e.getMessage()
        );
        writeLog(logEntry);
        log.error("Error logged: {}", sessionId, e);
    }

    private void writeLog(String entry) {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            writer.write(entry);
        } catch (IOException e) {
            log.error("Failed to write log", e);
        }
    }
}
