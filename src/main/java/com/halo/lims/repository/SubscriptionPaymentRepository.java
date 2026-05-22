package com.halo.lims.repository;

import com.halo.lims.model.SubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Integer> {
    Optional<SubscriptionPayment> findByRazorpayPaymentId(String razorpayPaymentId);
    List<SubscriptionPayment> findBySubscriptionIdOrderByCreatedAtDesc(Integer subscriptionId);
    List<SubscriptionPayment> findByStatus(String status);
}
