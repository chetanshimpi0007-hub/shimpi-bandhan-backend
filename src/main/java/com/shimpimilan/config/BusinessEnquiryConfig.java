package com.shimpimilan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "crm.enquiry")
@Data
public class BusinessEnquiryConfig {
    
    private Priority priority = new Priority();
    private Scoring scoring = new Scoring();
    private Sla sla = new Sla();

    @Data
    public static class Priority {
        private double highBudgetThreshold = 100000.0;
        private int weddingDaysThreshold = 30;
    }

    @Data
    public static class Scoring {
        private double profileWeight = 0.4;
        private double budgetWeight = 0.3;
        private double timelineWeight = 0.3;
    }

    @Data
    public static class Sla {
        private int overdueHoursThreshold = 48; // Hours after which an enquiry without follow-up is flagged
    }
}
