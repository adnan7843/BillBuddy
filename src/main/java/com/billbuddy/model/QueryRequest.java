// QueryRequest.java
package com.billbuddy.model;

import lombok.Data;

@Data
public class QueryRequest {
    private String query;
    private String sessionId;
    private Integer maxResults = 5;
}
