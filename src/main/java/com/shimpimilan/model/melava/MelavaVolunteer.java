package com.shimpimilan.model.melava;

import com.shimpimilan.model.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "melava_volunteer")
@Data
public class MelavaVolunteer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "melava_id", nullable = false)
    private Melava melava;

    @ManyToOne(fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "user_id") // Can be null if volunteer is not a registered user
    private User user;

    @Column(nullable = false, length = 150)
    private String volunteerName;

    @Column(length = 20)
    private String mobileNumber;

    private String role; // e.g., "Gate Coordinator", "Stage Manager"

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
