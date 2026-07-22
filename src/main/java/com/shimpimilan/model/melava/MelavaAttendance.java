package com.shimpimilan.model.melava;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "melava_attendance")
@Data
public class MelavaAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "melava_registration_id", nullable = false)
    private MelavaRegistration registration;

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    @Column(length = 50)
    private String scannedBy; // Admin or volunteer ID who scanned the QR

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
