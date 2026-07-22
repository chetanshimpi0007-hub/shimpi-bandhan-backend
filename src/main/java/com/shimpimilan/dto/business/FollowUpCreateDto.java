package com.shimpimilan.dto.business;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FollowUpCreateDto {
    private LocalDateTime followUpDate;
    private String outcome;
    private String status; // PENDING, COMPLETED
}
