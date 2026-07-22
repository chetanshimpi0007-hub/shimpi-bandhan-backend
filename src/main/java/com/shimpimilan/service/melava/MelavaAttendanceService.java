package com.shimpimilan.service.melava;

import com.shimpimilan.model.melava.MelavaAttendance;
import com.shimpimilan.model.melava.MelavaRegistration;
import com.shimpimilan.repository.melava.MelavaAttendanceRepository;
import com.shimpimilan.repository.melava.MelavaRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MelavaAttendanceService {

    private final MelavaAttendanceRepository attendanceRepository;
    private final MelavaRegistrationRepository registrationRepository;

    public List<MelavaAttendance> getAttendanceByMelava(Long melavaId) {
        return attendanceRepository.findByRegistrationMelavaId(melavaId);
    }

    @Transactional
    public MelavaAttendance scanQrCode(String qrData, String scannedBy) {
        MelavaRegistration registration = registrationRepository.findByQrCodeData(qrData)
                .orElseThrow(() -> new RuntimeException("Invalid QR Code. Registration not found."));

        Optional<MelavaAttendance> existingOpt = attendanceRepository.findByRegistrationId(registration.getId());
        
        if (existingOpt.isPresent()) {
            MelavaAttendance attendance = existingOpt.get();
            if (attendance.getCheckOutTime() == null) {
                // If checked in but not checked out, mark check out
                attendance.setCheckOutTime(LocalDateTime.now());
                return attendanceRepository.save(attendance);
            } else {
                throw new RuntimeException("Participant has already checked out.");
            }
        } else {
            // First time check-in
            MelavaAttendance attendance = new MelavaAttendance();
            attendance.setRegistration(registration);
            attendance.setCheckInTime(LocalDateTime.now());
            attendance.setScannedBy(scannedBy);
            return attendanceRepository.save(attendance);
        }
    }
}
