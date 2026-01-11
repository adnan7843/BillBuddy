// DataInitializer.java
package com.billbuddy.config;

import com.billbuddy.model.Plan;
import com.billbuddy.model.PlanType;
import com.billbuddy.service.VectorStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final VectorStoreService vectorStoreService;

    public DataInitializer(VectorStoreService vectorStoreService) {
        this.vectorStoreService = vectorStoreService;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing sample plan data...");

        List<Plan> samplePlans = Arrays.asList(
                createPlan(PlanType.INTERNET, "Telstra", "Family Unlimited NBN",
                        "Unlimited data on NBN 100 with no speed throttling. Perfect for streaming and gaming.",
                        99.0, "Unlimited", "100 Mbps", "No contract",
                        "Unlimited data, 24/7 support, Free modem, No excess charges",
                        "Premium pricing",
                        "Families with 3+ members, heavy streaming users, gamers"),

                createPlan(PlanType.INTERNET, "Optus", "Internet Everyday Plus",
                        "500GB data on NBN 50. Great value for moderate users.",
                        75.0, "500GB", "50 Mbps", "12 months",
                        "Good speed, Affordable, Optus Sport included",
                        "Data cap, Contract commitment",
                        "Small families, moderate internet users"),

                createPlan(PlanType.MOBILE, "Vodafone", "Mobile Max",
                        "Unlimited calls, texts, and 80GB data with 5G access.",
                        55.0, "80GB", "5G", "Month-to-month",
                        "5G network, Unlimited calls/SMS, Roaming in NZ",
                        "Network coverage varies",
                        "Heavy mobile users, travelers"),

                createPlan(PlanType.ENERGY, "AGL", "Residential Saver",
                        "Competitive electricity rates with solar feed-in tariff.",
                        220.0, null, null, "No lock-in",
                        "Solar buyback, No exit fees, Online account management",
                        "Rates vary by usage",
                        "Homeowners, solar panel owners")
        );

        for (Plan plan : samplePlans) {
            try {
                vectorStoreService.indexPlan(plan);

            } catch (RuntimeException e) {
                log.error("Indexing skipped at startup (embeddings unavailable). App will continue.", e);
                // optionally set a flag / health indicator / schedule retry later
            }
        }

        log.info("Sample data initialized successfully!");
    }

    private Plan createPlan(PlanType type, String provider, String name,
                            String description, Double price, String dataLimit,
                            String speed, String contract, String features,
                            String limitations, String bestFor) {
        Plan plan = new Plan();
        plan.setType(type);
        plan.setProvider(provider);
        plan.setName(name);
        plan.setDescription(description);
        plan.setMonthlyPrice(price);
        plan.setDataLimit(dataLimit);
        plan.setSpeed(speed);
        plan.setContractLength(contract);
        plan.setFeatures(features);
        plan.setLimitations(limitations);
        plan.setBestFor(bestFor);
        return plan;
    }
}
