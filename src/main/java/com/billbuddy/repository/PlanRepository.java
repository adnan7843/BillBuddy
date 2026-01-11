// PlanRepository.java
package com.billbuddy.repository;

import com.billbuddy.model.Plan;
import com.billbuddy.model.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByType(PlanType type);

    @Query("SELECT p FROM Plan p WHERE p.monthlyPrice BETWEEN :minPrice AND :maxPrice")
    List<Plan> findByPriceRange(Double minPrice, Double maxPrice);
}
