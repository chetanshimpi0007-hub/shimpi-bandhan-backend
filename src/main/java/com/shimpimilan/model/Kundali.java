package com.shimpimilan.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "kundali")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kundali {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    private LocalDate birthDate;
    private LocalTime birthTime;
    private String birthPlace;

    private String rashi;
    private String nakshatra;
    private String charan;
    private String nadi;
    private String gan;
    private String varna;
    private String yoni;
    private String vashya;
    
    private Boolean isManglik;
}
