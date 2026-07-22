package com.shimpimilan.controller;

import com.shimpimilan.dto.PublicStatsDTO;
import com.shimpimilan.service.PublicStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicStatsController {

    private final PublicStatsService publicStatsService;

    @GetMapping("/stats")
    public ResponseEntity<PublicStatsDTO> getStats() {
        PublicStatsDTO stats = publicStatsService.getPublicStats();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                .body(stats);
    }
}
