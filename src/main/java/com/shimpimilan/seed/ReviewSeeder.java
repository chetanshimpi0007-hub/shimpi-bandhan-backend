package com.shimpimilan.seed;

import com.shimpimilan.model.User;
import com.shimpimilan.model.business.Business;
import com.shimpimilan.model.business.BusinessReview;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.repository.business.BusinessRepository;
import com.shimpimilan.repository.business.BusinessReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ReviewSeeder implements CommandLineRunner {

    @Autowired
    private BusinessRepository businessRepository;
    
    @Autowired
    private BusinessReviewRepository reviewRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Get any user
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return;
        }
        User user = users.get(0);

        // Find the business
        List<Business> businesses = businessRepository.findAll();
        Business arnavBusiness = businesses.stream()
                .filter(b -> b.getBusinessName().contains("ArnavInfoWeb"))
                .findFirst()
                .orElse(null);

        if (arnavBusiness == null) {
            return;
        }

        // Check if reviews already exist
        long count = reviewRepository.countApprovedReviewsByBusinessId(arnavBusiness.getId());
        if (count > 0) {
            return; // already seeded
        }

        // Add 4-star review
        BusinessReview review1 = BusinessReview.builder()
                .business(arnavBusiness)
                .user(user)
                .rating(4)
                .title("Great Service")
                .comment("Very good IT services, responsive team.")
                .isApproved(true)
                .build();
        
        // Add 5-star review
        BusinessReview review2 = BusinessReview.builder()
                .business(arnavBusiness)
                .user(user)
                .rating(5)
                .title("Excellent Experience")
                .comment("Highly recommended for software development!")
                .isApproved(true)
                .build();

        reviewRepository.save(review1);
        reviewRepository.save(review2);
        
        System.out.println("====== ArnavInfoWeb Reviews Seeded (4.5 Avg) ======");
    }
}
