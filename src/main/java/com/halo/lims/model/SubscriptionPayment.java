package com.halo.lims.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "subscription_payments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SubscriptionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "razorpay_payment_id", unique = true, length = 255)
    private String razorpayPaymentId;

    @Column(name = "razorpay_invoice_id", length = 255)
    private String razorpayInvoiceId;

    @Column(name = "payment_status", nullable = false, length = 50)
    private String status; // PENDING, CAPTURED, FAILED, REFUNDED

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal amount;

    @Column(name = "transaction_fee", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal transactionFee;

    @Column(name = "net_amount", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal netAmount;

    @Column(name = "currency", length = 10)
    private String currency; // INR, USD, etc.

    @Column(name = "payment_date")
    private OffsetDateTime paymentDate;

    @Column(name = "cycle_start")
    private OffsetDateTime cycleStart;

    @Column(name = "cycle_end")
    private OffsetDateTime cycleEnd;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
