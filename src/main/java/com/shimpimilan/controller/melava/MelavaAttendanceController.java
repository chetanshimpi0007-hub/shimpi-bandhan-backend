package com.shimpimilan.controller.melava;

import com.shimpimilan.model.melava.MelavaAttendance;
import com.shimpimilan.service.melava.MelavaAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/melava/attendance")
@RequiredArgsConstructor
public class MelavaAttendanceController {

    private final MelavaAttendanceService attendanceService;

    @PostMapping("/scan")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MelavaAttendance> scanQrCode(@RequestParam String qrData, @RequestParam String adminId) {
        return ResponseEntity.ok(attendanceService.scanQrCode(qrData, adminId));
    }
}
