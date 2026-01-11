// Plan.java
package com.billbuddy.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PlanType type;

    private String provider;
    private String name;

    @Column(length = 5000)
    private String description;

    private Double monthlyPrice;
    private String dataLimit;
    private String speed;
    private String contractLength;

    @Column(length = 2000)
    private String features;

    @Column(length = 2000)
    private String limitations;

    @Column(length = 2000)
    private String bestFor;

    // Vector embedding stored as JSON string
    @Lob
    @Column(columnDefinition = "CLOB")
    private String embedding;
}
