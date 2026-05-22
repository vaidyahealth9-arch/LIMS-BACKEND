package com.halo.lims.repository;

import com.halo.lims.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    Optional<Subscription> findByOrganizationId(Integer organizationId);
    Optional<Subscription> findByRazorpaySubscriptionId(String razorpaySubscriptionId);
    List<Subscription> findByStatus(String status);
    List<Subscription> findByOrganizationIdAndStatus(Integer organizationId, String status);
}
