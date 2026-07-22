package com.shimpimilan.model.profile;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoroscopeDetails {
    private String rashi;
    private String nakshatra;
    private String gan;
    private String varna;
    private String nadi;
    private String charan;
}
