package com.shimpimilan.dto.business;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BusinessRegistrationRequest {
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Valid 10-digit mobile number required")
    private String mobileNumber;

    @Pattern(regexp = "^[0-9]{10}$", message = "Valid 10-digit mobile number required")
    private String whatsappNumber;

    @Email(message = "Valid email is required")
    private String email;

    @NotBlank(message = "Description is required")
    private String description;

    private String gstNumber;
    private String website;
    private String instagram;
    private String facebook;
    private String youtube;

    @NotBlank(message = "Address line is required")
    private String addressLine;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "PIN code is required")
    private String pinCode;

    private String googleMapsUrl;
    
    // In a real application with AWS S3, these would be URLs returned from the file upload service.
    // We will accept URLs as strings for now.
    private String logoUrl;
    private String coverUrl;

    private String workingHours;
    private Integer yearsOfExperience;
}
