package com.shimpimilan.model.melava;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "melava_payment")
@Data
public class MelavaPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "melava_registration_id", nullable = false)
    private MelavaRegistration registration;

    private Double amount;
    
    @Column(length = 50)
    private String paymentMethod; // UPI, CARD, CASH

    @Column(length = 100)
    private String transactionId;

    @Column(length = 100)
    private String receiptNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
