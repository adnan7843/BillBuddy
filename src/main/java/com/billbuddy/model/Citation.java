// Citation.java
package com.billbuddy.model;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class Citation {
    private String provider;
    private String planName;
    private String relevantText;
    private Double relevanceScore;
}
