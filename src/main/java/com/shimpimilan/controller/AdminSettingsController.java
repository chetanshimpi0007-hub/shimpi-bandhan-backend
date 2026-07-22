package com.shimpimilan.controller;

import com.shimpimilan.model.PlatformSetting;
import com.shimpimilan.service.PlatformSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/settings")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final PlatformSettingsService settingsService;

    @GetMapping
    public ResponseEntity<List<PlatformSetting>> getAllSettings() {
        return ResponseEntity.ok(settingsService.getAllSettings());
    }

    @GetMapping("/grouped")
    public ResponseEntity<Map<String, List<PlatformSetting>>> getSettingsGroupedByCategory() {
        return ResponseEntity.ok(settingsService.getSettingsGroupedByCategory());
    }

    @PutMapping
    public ResponseEntity<String> updateSettings(@RequestBody List<PlatformSetting> settings) {
        settingsService.updateSettings(settings);
        return ResponseEntity.ok("Settings updated successfully");
    }
}
