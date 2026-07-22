package com.shimpimilan.dto.business;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EnquiryCreateDto {
    private Double budget;
    private LocalDate weddingDate;
    private String message;
}
