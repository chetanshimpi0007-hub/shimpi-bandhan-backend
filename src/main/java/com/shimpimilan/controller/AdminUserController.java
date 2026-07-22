package com.shimpimilan.controller;

import com.shimpimilan.model.AuditLog;
import com.shimpimilan.model.Community;
import com.shimpimilan.model.User;
import com.shimpimilan.model.UserStatus;
import com.shimpimilan.repository.AuditLogRepository;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String community,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal CustomUserDetails adminDetails) {

        Community communityEnum = null;
        try { if (community != null) communityEnum = Community.valueOf(community); } catch (Exception ignored) {}

        UserStatus statusEnum = null;
        try { if (status != null) statusEnum = UserStatus.valueOf(status); } catch (Exception ignored) {}

        Page<User> users = userRepository.findAllWithFilters(
            search, communityEnum, statusEnum,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return ResponseEntity.ok(users);
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<User>> getPendingUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<User> pendingUsers = userRepository.findAllWithFilters(
            null, null, UserStatus.PENDING,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return ResponseEntity.ok(pendingUsers);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{userId}/suspend")
    public ResponseEntity<String> suspendUser(@PathVariable Long userId,
                                              @AuthenticationPrincipal CustomUserDetails adminDetails) {
        return updateStatus(userId, UserStatus.SUSPENDED, "SUSPEND_USER", adminDetails.getUser());
    }

    @PutMapping("/{userId}/activate")
    public ResponseEntity<String> activateUser(@PathVariable Long userId,
                                               @AuthenticationPrincipal CustomUserDetails adminDetails) {
        return updateStatus(userId, UserStatus.APPROVED, "ACTIVATE_USER", adminDetails.getUser());
    }

    @PutMapping("/{userId}/approve")
    public ResponseEntity<String> approveUser(@PathVariable Long userId,
                                              @AuthenticationPrincipal CustomUserDetails adminDetails) {
        return updateStatus(userId, UserStatus.APPROVED, "APPROVE_USER", adminDetails.getUser());
    }

    @PutMapping("/{userId}/reject")
    public ResponseEntity<String> rejectUser(@PathVariable Long userId,
                                             @AuthenticationPrincipal CustomUserDetails adminDetails) {
        return updateStatus(userId, UserStatus.REJECTED, "REJECT_USER", adminDetails.getUser());
    }

    @PutMapping("/{userId}/block")
    public ResponseEntity<String> blockUser(@PathVariable Long userId,
                                            @AuthenticationPrincipal CustomUserDetails adminDetails) {
        return updateStatus(userId, UserStatus.BLOCKED, "BLOCK_USER", adminDetails.getUser());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId,
                                             @AuthenticationPrincipal CustomUserDetails adminDetails) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        String oldStatus = user.getStatus().name();
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
        logAction(userId, "DELETE_USER", "USER_MANAGEMENT", oldStatus, "DELETED",
                  adminDetails.getUser().getPhone());
        return ResponseEntity.ok("User soft-deleted successfully");
    }

    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long userId,
                                                              @AuthenticationPrincipal CustomUserDetails adminDetails) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        String tempPassword = "Temp@" + UUID.randomUUID().toString().substring(0, 6);
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        userRepository.save(user);
        logAction(userId, "RESET_PASSWORD", "USER_MANAGEMENT", null, "PASSWORD_RESET",
                  adminDetails.getUser().getPhone());
        return ResponseEntity.ok(Map.of("message", "Password reset successful", "temporaryPassword", tempPassword));
    }

    @PostMapping("/{userId}/impersonate")
    public ResponseEntity<Map<String, String>> impersonateUser(@PathVariable Long userId,
                                                               @AuthenticationPrincipal CustomUserDetails adminDetails) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        // This will be handled by the frontend passing a special admin token or we generate a standard user JWT here
        // For security, we'll return a short-lived impersonation token (we would inject JwtService if doing it locally)
        // Since we don't have JwtService injected here yet, let's inject it.
        logAction(userId, "IMPERSONATE_USER", "USER_MANAGEMENT", null, "IMPERSONATION_STARTED", adminDetails.getUser().getPhone());
        return ResponseEntity.ok(Map.of("message", "Impersonation logged, token generation deferred to auth service"));
    }

    private ResponseEntity<String> updateStatus(Long userId, UserStatus newStatus, String action, User admin) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        String oldStatus = user.getStatus().name();
        user.setStatus(newStatus);
        userRepository.save(user);
        logAction(userId, action, "USER_MANAGEMENT", oldStatus, newStatus.name(), admin.getPhone());
        return ResponseEntity.ok("User status updated to " + newStatus);
    }

    private void logAction(Long userId, String action, String module, String oldVal, String newVal, String adminName) {
        auditLogRepository.save(AuditLog.builder()
                .userId(userId)
                .action(action)
                .module(module)
                .oldValue(oldVal)
                .newValue(newVal)
                .adminName(adminName)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
