package com.shimpimilan.service;

import com.shimpimilan.model.PlatformSetting;
import com.shimpimilan.repository.PlatformSettingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformSettingsService {

    private final PlatformSettingRepository settingRepository;
    
    // In-memory cache for fast reads
    private final Map<String, PlatformSetting> settingsCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void seedDefaults() {
        if (settingRepository.count() == 0) {
            log.info("Seeding default platform settings...");
            settingRepository.saveAll(List.of(
                PlatformSetting.builder().key("PREMIUM_PRICE").value("99.0").category("PRICING").description("Price for Premium Membership (INR)").build(),
                PlatformSetting.builder().key("FREE_TRIAL_DAYS").value("30").category("PRICING").description("Duration of Free Trial (Days)").build(),
                PlatformSetting.builder().key("REFERRAL_DISCOUNT").value("10.0").category("PRICING").description("Discount per referral (INR)").build(),
                PlatformSetting.builder().key("BIZ_PLAN_BASIC").value("299.0").category("PRICING").description("Basic Business Plan Price").build(),
                PlatformSetting.builder().key("BIZ_PLAN_SILVER").value("599.0").category("PRICING").description("Silver Business Plan Price").build(),
                PlatformSetting.builder().key("BIZ_PLAN_GOLD").value("999.0").category("PRICING").description("Gold Business Plan Price").build(),
                PlatformSetting.builder().key("BIZ_PLAN_PLATINUM").value("1999.0").category("PRICING").description("Platinum Business Plan Price").build(),
                PlatformSetting.builder().key("MAINTENANCE_MODE").value("false").category("GENERAL").description("Enable Maintenance Mode").build(),
                PlatformSetting.builder().key("SITE_LOGO_URL").value("/assets/logo.png").category("UI").description("Main Site Logo URL").build()
            ));
            refreshCache();
        }
    }

    public void refreshCache() {
        List<PlatformSetting> allSettings = settingRepository.findAll();
        settingsCache.clear();
        for (PlatformSetting setting : allSettings) {
            settingsCache.put(setting.getKey(), setting);
        }
        log.info("Loaded {} platform settings into cache.", settingsCache.size());
    }

    public String getSettingValue(String key, String defaultValue) {
        PlatformSetting setting = settingsCache.get(key);
        return (setting != null && setting.getValue() != null) ? setting.getValue() : defaultValue;
    }
    
    public int getSettingAsInt(String key, int defaultValue) {
        String val = getSettingValue(key, null);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public double getSettingAsDouble(String key, double defaultValue) {
        String val = getSettingValue(key, null);
        if (val == null) return defaultValue;
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getSettingAsBoolean(String key, boolean defaultValue) {
        String val = getSettingValue(key, null);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val);
    }

    public List<PlatformSetting> getAllSettings() {
        return settingRepository.findAll();
    }
    
    public Map<String, List<PlatformSetting>> getSettingsGroupedByCategory() {
        return settingRepository.findAll().stream()
                .collect(Collectors.groupingBy(s -> s.getCategory() == null ? "UNCATEGORIZED" : s.getCategory()));
    }

    public void updateSettings(List<PlatformSetting> settings) {
        for (PlatformSetting s : settings) {
            PlatformSetting existing = settingRepository.findById(s.getKey()).orElse(new PlatformSetting());
            existing.setKey(s.getKey());
            existing.setValue(s.getValue());
            if (s.getCategory() != null) existing.setCategory(s.getCategory());
            if (s.getDescription() != null) existing.setDescription(s.getDescription());
            settingRepository.save(existing);
        }
        refreshCache();
    }
}
