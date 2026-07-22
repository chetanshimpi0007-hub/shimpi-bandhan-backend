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
public class PartnerPreferenceExtended {
    private String partnerMarriagePreference;
    private Integer partnerAgeFrom;
    private Integer partnerAgeTo;
    private Double partnerHeight;
    private String partnerEducation;
    private String partnerOccupation;
    private String partnerCity;
    private String partnerState;
    private String partnerOtherExpectations;
}
