package com.shimpimilan.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.shimpimilan.model.MembershipSource;
import com.shimpimilan.model.Payment;
import com.shimpimilan.model.PaymentStatus;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MembershipService membershipService;
    private final com.shimpimilan.repository.ReferralWalletRepository referralWalletRepository;
    private final ReferralService referralService;
    private final PlatformSettingsService platformSettingsService;

    @Value("${razorpay.key.id:rzp_test_placeholder}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:placeholder_secret}")
    private String razorpayKeySecret;

    @Transactional
    public Payment createPremiumOrder(User user, boolean useReferralDiscount) {
        try {
            double finalPrice = platformSettingsService.getSettingAsDouble("PREMIUM_PRICE", 99.0);
            double originalPrice = finalPrice;
            double discountApplied = 0.0;
            double referralDiscountAmount = platformSettingsService.getSettingAsDouble("REFERRAL_DISCOUNT", 10.0);

            if (useReferralDiscount) {
                Optional<com.shimpimilan.model.ReferralWallet> walletOpt = referralWalletRepository.findByUserId(user.getId());
                if (walletOpt.isPresent()) {
                    com.shimpimilan.model.ReferralWallet wallet = walletOpt.get();
                    if (wallet.getAvailableBalance() > 0) {
                        discountApplied = Math.min(wallet.getAvailableBalance(), finalPrice - 1.0); // Keep at least ₹1
                        finalPrice -= discountApplied;
                    }
                }
            }

            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int) (finalPrice * 100)); // Amount in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

            Order order = razorpay.orders.create(orderRequest);

            Payment payment = Payment.builder()
                    .user(user)
                    .razorpayOrderId(order.get("id"))
                    .amount(originalPrice)
                    .discountApplied(discountApplied)
                    .finalAmountPaid(finalPrice)
                    .status(PaymentStatus.CREATED)
                    .build();

            return paymentRepository.save(payment);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Razorpay order: " + e.getMessage());
        }
    }

    @Transactional
    public void verifyAndActivatePremium(User user, String orderId, String paymentId, String signature) {
        // Duplicate Payment Protection
        Optional<Payment> existingPayment = paymentRepository.findByRazorpayPaymentId(paymentId);
        if (existingPayment.isPresent()) {
            throw new RuntimeException("Payment ID already processed to prevent duplicate activation.");
        }

        Payment payment = paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order ID not found"));

        if (payment.getStatus() == PaymentStatus.CAPTURED) {
            throw new RuntimeException("Order is already captured.");
        }

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (isValid) {
                payment.setRazorpayPaymentId(paymentId);
                payment.setRazorpaySignature(signature);
                payment.setStatus(PaymentStatus.CAPTURED);
                payment.setPaymentMethod("RAZORPAY_GATEWAY"); // Real gateway provides method details via API
                
                // Deduct Referral Balance if applied
                if (payment.getDiscountApplied() != null && payment.getDiscountApplied() > 0) {
                    com.shimpimilan.model.ReferralWallet wallet = referralWalletRepository.findByUserId(user.getId())
                            .orElseThrow(() -> new RuntimeException("Referral wallet not found but discount was applied."));
                    wallet.setAvailableBalance(wallet.getAvailableBalance() - payment.getDiscountApplied());
                    wallet.setUsedBalance(wallet.getUsedBalance() + payment.getDiscountApplied());
                    referralWalletRepository.save(wallet);
                }

                // Activate Premium for 30 days via MembershipService
                membershipService.activatePremium(user, 30, orderId, paymentId, MembershipSource.PURCHASE);

                // Process ₹10 Reward for the referrer (if any)
                referralService.processRewardForPremiumPurchase(user);

                // Populate Membership dates for the Invoice/History
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                payment.setMembershipStartDate(now);
                
                // Assuming no active membership logic for simplicity in payment record, just +30 days
                payment.setMembershipExpiryDate(user.getProfile().getPremiumExpiryDate() != null && user.getProfile().getPremiumExpiryDate().isAfter(now) 
                        ? user.getProfile().getPremiumExpiryDate().plusDays(30) 
                        : now.plusDays(30));

                paymentRepository.save(payment);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                throw new RuntimeException("Signature verification failed.");
            }
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Error verifying payment: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public java.util.List<Payment> getPaymentHistory(User user) {
        // In a real application, you might want to use a custom repository method or DTOs
        return paymentRepository.findAll().stream()
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByRazorpayId(String paymentId) {
        return paymentRepository.findByRazorpayPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
}
