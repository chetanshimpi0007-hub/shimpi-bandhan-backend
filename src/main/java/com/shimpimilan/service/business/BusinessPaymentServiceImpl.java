package com.shimpimilan.service.business;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.shimpimilan.dto.business.BusinessPaymentRequest;
import com.shimpimilan.dto.business.BusinessPaymentVerifyRequest;
import com.shimpimilan.exception.ResourceNotFoundException;
import com.shimpimilan.model.business.AdvertisementPlan;
import com.shimpimilan.model.business.Business;
import com.shimpimilan.model.business.BusinessPayment;
import com.shimpimilan.model.business.BusinessStatus;
import com.shimpimilan.model.business.BusinessSubscription;
import com.shimpimilan.repository.business.BusinessPaymentRepository;
import com.shimpimilan.repository.business.BusinessRepository;
import com.shimpimilan.repository.business.BusinessSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BusinessPaymentServiceImpl implements BusinessPaymentService {

    private final BusinessRepository businessRepository;
    private final BusinessPaymentRepository paymentRepository;
    private final BusinessSubscriptionRepository subscriptionRepository;
    private final com.shimpimilan.service.PlatformSettingsService platformSettingsService;

    @Value("${razorpay.key.id:rzp_test_placeholder}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:placeholder_secret}")
    private String razorpayKeySecret;

    private double getPlanPrice(AdvertisementPlan plan) {
        switch (plan) {
            case BASIC: return platformSettingsService.getSettingAsDouble("BIZ_PLAN_BASIC", 299.0);
            case SILVER: return platformSettingsService.getSettingAsDouble("BIZ_PLAN_SILVER", 599.0);
            case GOLD: return platformSettingsService.getSettingAsDouble("BIZ_PLAN_GOLD", 999.0);
            case PLATINUM: return platformSettingsService.getSettingAsDouble("BIZ_PLAN_PLATINUM", 1999.0);
            default: throw new IllegalArgumentException("Unknown plan type");
        }
    }

    @Override
    @Transactional
    public BusinessPayment createOrder(Long businessId, Long userId, BusinessPaymentRequest request) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        if (!business.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to purchase plan for this business");
        }

        try {
            double amount = getPlanPrice(request.getPlanType());
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int) (amount * 100)); // paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "biz_txn_" + System.currentTimeMillis());

            Order order = razorpay.orders.create(orderRequest);

            BusinessPayment payment = BusinessPayment.builder()
                    .business(business)
                    .razorpayOrderId(order.get("id"))
                    .amount(amount)
                    .planType(request.getPlanType())
                    .status("CREATED")
                    .build();

            return paymentRepository.save(payment);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Razorpay order: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void verifyAndActivateSubscription(Long businessId, Long userId, BusinessPaymentVerifyRequest request) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        if (!business.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        BusinessPayment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Order ID not found"));

        if ("COMPLETED".equals(payment.getStatus())) {
            throw new RuntimeException("Order is already captured.");
        }

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (isValid) {
                payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
                payment.setRazorpaySignature(request.getRazorpaySignature());
                payment.setStatus("COMPLETED");
                payment.setPaymentMethod("RAZORPAY_GATEWAY");

                // Check for existing active subscription
                BusinessSubscription existingSub = subscriptionRepository
                        .findTopByBusinessIdAndIsActiveTrueOrderByExpiryDateDesc(businessId)
                        .orElse(null);

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startDate = now;
                LocalDateTime expiryDate;

                if (existingSub != null && existingSub.getExpiryDate().isAfter(now)) {
                    startDate = existingSub.getExpiryDate();
                    expiryDate = startDate.plusDays(30);
                } else {
                    expiryDate = now.plusDays(30);
                }

                BusinessSubscription subscription = BusinessSubscription.builder()
                        .business(business)
                        .planType(payment.getPlanType())
                        .startDate(startDate)
                        .expiryDate(expiryDate)
                        .isActive(true)
                        .build();

                subscription = subscriptionRepository.save(subscription);
                
                payment.setSubscription(subscription);
                paymentRepository.save(payment);

                // Update Business Status
                business.setPlanType(payment.getPlanType());
                business.setStatus(BusinessStatus.ACTIVE);
                businessRepository.save(business);

                // TODO: Enqueue Notification for Successful Payment and Plan Activation

            } else {
                payment.setStatus("FAILED");
                paymentRepository.save(payment);
                throw new RuntimeException("Signature verification failed.");
            }
        } catch (Exception e) {
            payment.setStatus("FAILED");
            paymentRepository.save(payment);
            throw new RuntimeException("Error verifying payment: " + e.getMessage());
        }
    }
}
