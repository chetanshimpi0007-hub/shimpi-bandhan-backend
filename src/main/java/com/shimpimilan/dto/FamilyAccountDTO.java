package com.shimpimilan.dto;

import com.shimpimilan.model.FamilyRelationship;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FamilyAccountDTO {
    @NotBlank(message = "Family member name is required")
    private String familyMemberName;
    
    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;
    
    private String whatsappNumber;
    private String email;
    
    @NotNull(message = "Relationship is required")
    private FamilyRelationship relationshipWithCandidate;
}
