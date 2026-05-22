package com.halo.lims.repository;

import com.halo.lims.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Integer> {
    Optional<SubscriptionPlan> findByPlanName(String planName);
    List<SubscriptionPlan> findAllByIsActiveTrue();
}
