package com.shimpimilan.repository.melava;

import com.shimpimilan.model.melava.MelavaAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MelavaAttendanceRepository extends JpaRepository<MelavaAttendance, Long> {
    List<MelavaAttendance> findByRegistrationMelavaId(Long melavaId);
    Optional<MelavaAttendance> findByRegistrationId(Long registrationId);
}
