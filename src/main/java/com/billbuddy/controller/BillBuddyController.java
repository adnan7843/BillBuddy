// BillBuddyController.java
package com.billbuddy.controller;

import com.billbuddy.model.QueryRequest;
import com.billbuddy.model.QueryResponse;
import com.billbuddy.service.ComparisonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billbuddy")
@Slf4j
public class BillBuddyController {

    private final ComparisonService comparisonService;

    public BillBuddyController(ComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @PostMapping("/ask")
    public ResponseEntity<QueryResponse> askQuestion(@RequestBody QueryRequest request) {
        log.info("Received query: {}", request.getQuery());
        QueryResponse response = comparisonService.processQuery(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("BillBuddy is running!");
    }
}
