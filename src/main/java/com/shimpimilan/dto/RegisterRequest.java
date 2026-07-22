package com.shimpimilan.dto;

import com.shimpimilan.model.Community;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
    private String phone;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotNull(message = "Community selection is required")
    private Community community;

    @Builder.Default
    private com.shimpimilan.model.AccountType accountType = com.shimpimilan.model.AccountType.SELF;
    
    private FamilyAccountDTO familyDetails;

    private String referredByCode;
}
