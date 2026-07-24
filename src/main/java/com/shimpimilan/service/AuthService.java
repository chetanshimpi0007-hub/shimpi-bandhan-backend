package com.shimpimilan.service;

import com.shimpimilan.dto.AuthResponse;
import com.shimpimilan.dto.LoginRequest;
import com.shimpimilan.dto.RegisterRequest;
import com.shimpimilan.dto.VerifyOtpRequest;
import com.shimpimilan.model.Role;
import com.shimpimilan.model.User;
import com.shimpimilan.model.UserStatus;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.security.CustomUserDetails;
import com.shimpimilan.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final com.shimpimilan.repository.FamilyAccountRepository familyAccountRepository;
    private final com.shimpimilan.repository.ReferralRepository referralRepository;
    private final com.shimpimilan.repository.ReferralWalletRepository referralWalletRepository;

    // Temporary storage for OTPs (In a real app, use Redis)
    private final Map<String, String> otpStorage = new HashMap<>();

    @org.springframework.transaction.annotation.Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already registered");
        }

        if (request.getCommunity() != com.shimpimilan.model.Community.AHER_SHIMPI && 
            request.getCommunity() != com.shimpimilan.model.Community.NAMDEV_SHIMPI) {
            throw new RuntimeException("Registration is strictly limited to Aher Shimpi and Namdev Shimpi communities.");
        }

        User referrer = null;
        if (request.getReferredByCode() != null && !request.getReferredByCode().trim().isEmpty()) {
            referrer = userRepository.findByReferralCode(request.getReferredByCode())
                    .orElseThrow(() -> new RuntimeException("Invalid referral code provided."));
            if (referrer.getStatus() != UserStatus.APPROVED) {
                throw new RuntimeException("The referral code belongs to an inactive or pending account.");
            }
        }

        String newReferralCode = "SM" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        User user = User.builder()
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .community(request.getCommunity())
                .accountType(request.getAccountType() != null ? request.getAccountType() : com.shimpimilan.model.AccountType.SELF)
                .role(Role.USER)
                .status(UserStatus.PENDING)
                .referralCode(newReferralCode)
                .build();

        user = userRepository.save(user);

        if (user.getAccountType() == com.shimpimilan.model.AccountType.FAMILY) {
            if (request.getFamilyDetails() == null) {
                throw new RuntimeException("Family details are required for Family Account");
            }
            com.shimpimilan.model.FamilyAccount familyAccount = com.shimpimilan.model.FamilyAccount.builder()
                    .user(user)
                    .familyMemberName(request.getFamilyDetails().getFamilyMemberName())
                    .mobileNumber(request.getFamilyDetails().getMobileNumber())
                    .whatsappNumber(request.getFamilyDetails().getWhatsappNumber())
                    .email(request.getFamilyDetails().getEmail())
                    .relationshipWithCandidate(request.getFamilyDetails().getRelationshipWithCandidate())
                    .build();
            familyAccountRepository.save(familyAccount);
        }

        // Initialize empty Referral Wallet
        com.shimpimilan.model.ReferralWallet wallet = com.shimpimilan.model.ReferralWallet.builder()
                .user(user)
                .totalEarnings(0.0)
                .availableBalance(0.0)
                .usedBalance(0.0)
                .build();
        referralWalletRepository.save(wallet);

        // Record Referral if applied
        if (referrer != null) {
            com.shimpimilan.model.Referral referral = com.shimpimilan.model.Referral.builder()
                    .referrer(referrer)
                    .referredUser(user)
                    .isRewardProcessed(false)
                    .build();
            referralRepository.save(referral);
        }

        // Generate and send mock OTP
        String otp = "123456"; // Mocked OTP
        otpStorage.put(request.getPhone(), otp);
        
        return AuthResponse.builder()
                .message("User registered successfully. OTP sent for verification: " + otp)
                .build();
    }

    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String storedOtp = otpStorage.get(request.getPhone());
        if (storedOtp != null && storedOtp.equals(request.getOtp())) {
            otpStorage.remove(request.getPhone());
            
            // Generate JWT Token
            String jwtToken = jwtService.generateToken(new CustomUserDetails(user));
            return AuthResponse.builder()
                    .token(jwtToken)
                    .message("OTP verified successfully. Admin approval pending for full access.")
                    .user(AuthResponse.UserDto.builder()
                            .id(user.getId())
                            .phone(user.getPhone())
                            .role(user.getRole().name())
                            .status(user.getStatus().name())
                            .build())
                    .build();
        } else {
            throw new RuntimeException("Invalid OTP");
        }
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getPhone(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.ADMIN) {
            if (user.getStatus() == UserStatus.SUSPENDED) {
                throw new RuntimeException("Account suspended");
            }
            if (user.getStatus() == UserStatus.REJECTED) {
                throw new RuntimeException("Account rejected by admin");
            }
        }

        String jwtToken = jwtService.generateToken(new CustomUserDetails(user));
        return AuthResponse.builder()
                .token(jwtToken)
                .message("Login successful")
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .phone(user.getPhone())
                        .role(user.getRole().name())
                        .status(user.getStatus().name())
                        .build())
                .build();
    }
}
