package com.shimpimilan.repository.melava;

import com.shimpimilan.model.melava.MelavaRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MelavaRegistrationRepository extends JpaRepository<MelavaRegistration, Long> {
    List<MelavaRegistration> findByMelavaId(Long melavaId);
    List<MelavaRegistration> findByUserId(Long userId);
    Optional<MelavaRegistration> findByQrCodeData(String qrCodeData);
    boolean existsByMelavaIdAndUserId(Long melavaId, Long userId);
}
