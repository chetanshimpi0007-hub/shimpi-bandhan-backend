package com.shimpimilan.dto.business;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BusinessPaymentVerifyRequest {
    @NotBlank(message = "Razorpay Order ID is required")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay Payment ID is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay Signature is required")
    private String razorpaySignature;
}
