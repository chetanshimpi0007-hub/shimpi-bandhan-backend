package com.shimpimilan.service.business;

import com.shimpimilan.dto.business.BusinessLeadRequest;
import com.shimpimilan.dto.business.BusinessReviewRequest;
import com.shimpimilan.exception.ResourceNotFoundException;
import com.shimpimilan.model.User;
import com.shimpimilan.model.business.Business;
import com.shimpimilan.model.business.BusinessLead;
import com.shimpimilan.model.business.BusinessReview;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.repository.business.BusinessLeadRepository;
import com.shimpimilan.repository.business.BusinessRepository;
import com.shimpimilan.repository.business.BusinessReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessInteractionServiceImpl implements BusinessInteractionService {

    private final BusinessRepository businessRepository;
    private final BusinessReviewRepository reviewRepository;
    private final BusinessLeadRepository leadRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void addReview(Long businessId, Long userId, BusinessReviewRequest request) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user already reviewed
        java.util.List<BusinessReview> existingReviews = reviewRepository.findAll();
        boolean alreadyReviewed = existingReviews.stream()
                .anyMatch(r -> r.getBusiness().getId().equals(businessId) && r.getUser().getId().equals(userId));
        
        if (alreadyReviewed) {
            throw new RuntimeException("You have already reviewed this business.");
        }

        BusinessReview review = BusinessReview.builder()
                .business(business)
                .user(user)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .isApproved(true) // Auto-approve for now; admin can moderate later
                .build();

        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void trackLead(Long businessId, Long userId, BusinessLeadRequest request) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        BusinessLead lead = BusinessLead.builder()
                .business(business)
                .user(user)
                .leadType(request.getLeadType())
                .build();

        leadRepository.save(lead);
    }
}
