package com.shimpimilan.repository.melava;

import com.shimpimilan.model.melava.MelavaPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MelavaPaymentRepository extends JpaRepository<MelavaPayment, Long> {
    List<MelavaPayment> findByRegistrationMelavaId(Long melavaId);
    List<MelavaPayment> findByRegistrationId(Long registrationId);
}
