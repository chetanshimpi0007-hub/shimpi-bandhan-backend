package com.shimpimilan.dto.business;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MeetingCreateDto {
    private String type; // SHOP_VISIT, VIDEO_MEETING, PHONE_CALL
    private LocalDateTime scheduledAt;
    private String notes;
}
