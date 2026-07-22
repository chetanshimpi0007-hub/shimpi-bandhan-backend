package com.shimpimilan.controller.melava;

import com.shimpimilan.model.melava.Melava;
import com.shimpimilan.model.melava.MelavaRegistration;
import com.shimpimilan.model.melava.RegistrationType;
import com.shimpimilan.service.melava.MelavaRegistrationService;
import com.shimpimilan.service.melava.MelavaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/melava")
@RequiredArgsConstructor
public class PublicMelavaController {

    private final MelavaService melavaService;
    private final MelavaRegistrationService registrationService;

    @GetMapping("/active")
    public ResponseEntity<List<Melava>> getActiveMelavas() {
        return ResponseEntity.ok(melavaService.getActiveMelavas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Melava> getMelavaById(@PathVariable Long id) {
        return melavaService.getMelavaById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{melavaId}/register/{userId}")
    public ResponseEntity<MelavaRegistration> registerForMelava(
            @PathVariable Long melavaId, 
            @PathVariable Long userId, 
            @RequestParam RegistrationType type) {
        // In a real scenario, extract userId from JWT token
        return ResponseEntity.ok(registrationService.register(melavaId, userId, type));
    }
    
    @GetMapping("/user/{userId}/registrations")
    public ResponseEntity<List<MelavaRegistration>> getUserRegistrations(@PathVariable Long userId) {
        return ResponseEntity.ok(registrationService.getRegistrationsByUser(userId));
    }
}
