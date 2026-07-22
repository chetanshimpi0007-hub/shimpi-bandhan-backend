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
public class FamilyDetailsExtended {
    private String fatherName;
    private String fatherOccupation;
    private String motherName;
    private String motherOccupation;
    
    @Builder.Default
    private Integer brothers = 0;
    
    @Builder.Default
    private Integer marriedBrothers = 0;
    
    @Builder.Default
    private Integer sisters = 0;
    
    @Builder.Default
    private Integer marriedSisters = 0;
    
    private String maternalUncle;
    private String mamaKul;
    private String familyStatus;
    private String nativePlace;
}
