package com.shimpimilan.controller.melava;

import com.shimpimilan.model.melava.Melava;
import com.shimpimilan.service.melava.MelavaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/melava")
@RequiredArgsConstructor
public class AdminMelavaController {

    private final MelavaService melavaService;
    private final com.shimpimilan.service.melava.MelavaRegistrationService melavaRegistrationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Melava>> getAllMelavas() {
        return ResponseEntity.ok(melavaService.getAllMelavas());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Melava> createMelava(@RequestBody Melava melava) {
        return ResponseEntity.ok(melavaService.createMelava(melava));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Melava> updateMelava(@PathVariable Long id, @RequestBody Melava melavaDetails) {
        return ResponseEntity.ok(melavaService.updateMelava(id, melavaDetails));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMelava(@PathVariable Long id) {
        melavaService.deleteMelava(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/registrations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<com.shimpimilan.model.melava.MelavaRegistration>> getMelavaRegistrations(@PathVariable Long id) {
        return ResponseEntity.ok(melavaRegistrationService.getRegistrationsByMelava(id));
    }

    @PostMapping("/registrations/{id}/checkin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.shimpimilan.model.melava.MelavaRegistration> checkInRegistration(@PathVariable Long id) {
        // Find registration and update checkin status (we will add logic in service later if missing)
        return ResponseEntity.ok(new com.shimpimilan.model.melava.MelavaRegistration());
    }
}
