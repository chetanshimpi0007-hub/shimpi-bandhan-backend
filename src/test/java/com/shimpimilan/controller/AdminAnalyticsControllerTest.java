package com.shimpimilan.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserDailyRegistrations_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/users/daily"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetUserDailyRegistrations_AsUser_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/users/daily"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserCommunityDistribution_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/users/community"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserVerificationStatus_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/users/verification"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserGenderDistribution_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/users/gender"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserAgeDistribution_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/users/age"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetMembershipDistribution_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/membership/distribution"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetDailyRevenue_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/revenue/daily"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetBusinessCategoryDistribution_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/business/category"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetBusinessCityDistribution_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/business/city"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetProfileStatusDistribution_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/matrimonial/profile-status"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetInterestTrend_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/matrimonial/interests"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetCrmPipeline_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/crm/pipeline"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetChatSummary_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/chat/summary"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetNotificationSummary_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/notifications/summary"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetSystemMetrics_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/system/metrics"))
                .andExpect(status().isOk());
    }
}
