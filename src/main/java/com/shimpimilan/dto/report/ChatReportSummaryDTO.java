package com.shimpimilan.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatReportSummaryDTO {
    private Long totalConversations;
    private Long totalMessages;
    private Long reportedConversations;
    private Long deletedMessages;
    private Long blockedUsers;
    private Long moderationActionsToday;
}
