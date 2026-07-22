package com.shimpimilan.service.melava;

import com.shimpimilan.model.User;
import com.shimpimilan.model.melava.Melava;
import com.shimpimilan.model.melava.MelavaRegistration;
import com.shimpimilan.model.melava.PaymentStatus;
import com.shimpimilan.model.melava.RegistrationStatus;
import com.shimpimilan.model.melava.RegistrationType;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.repository.melava.MelavaRegistrationRepository;
import com.shimpimilan.repository.melava.MelavaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MelavaRegistrationService {

    private final MelavaRegistrationRepository registrationRepository;
    private final MelavaRepository melavaRepository;
    private final UserRepository userRepository;

    public List<MelavaRegistration> getRegistrationsByMelava(Long melavaId) {
        return registrationRepository.findByMelavaId(melavaId);
    }

    public List<MelavaRegistration> getRegistrationsByUser(Long userId) {
        return registrationRepository.findByUserId(userId);
    }
    
    public Optional<MelavaRegistration> getRegistrationByQrData(String qrData) {
        return registrationRepository.findByQrCodeData(qrData);
    }

    @Transactional
    public MelavaRegistration register(Long melavaId, Long userId, RegistrationType type) {
        if (registrationRepository.existsByMelavaIdAndUserId(melavaId, userId)) {
            throw new RuntimeException("User is already registered for this Melava.");
        }

        Melava melava = melavaRepository.findById(melavaId)
                .orElseThrow(() -> new RuntimeException("Melava not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MelavaRegistration registration = new MelavaRegistration();
        registration.setMelava(melava);
        registration.setUser(user);
        registration.setRegistrationType(type);
        registration.setRegistrationStatus(RegistrationStatus.CONFIRMED);
        registration.setPaymentStatus(PaymentStatus.PENDING);
        
        // Generate unique QR token
        String qrToken = "MELAVA-" + melavaId + "-USER-" + userId + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        registration.setQrCodeData(qrToken);

        return registrationRepository.save(registration);
    }

    @Transactional
    public MelavaRegistration updatePaymentStatus(Long registrationId, PaymentStatus status, String transactionId) {
        return registrationRepository.findById(registrationId).map(reg -> {
            reg.setPaymentStatus(status);
            if (transactionId != null) {
                reg.setPaymentTransactionId(transactionId);
            }
            return registrationRepository.save(reg);
        }).orElseThrow(() -> new RuntimeException("Registration not found"));
    }
}
