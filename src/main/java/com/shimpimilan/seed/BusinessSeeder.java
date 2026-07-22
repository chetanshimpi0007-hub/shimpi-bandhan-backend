package com.shimpimilan.seed;

import com.shimpimilan.model.User;
import com.shimpimilan.model.business.Business;
import com.shimpimilan.model.business.BusinessCategory;
import com.shimpimilan.model.business.BusinessStatus;
import com.shimpimilan.model.business.AdvertisementPlan;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.repository.business.BusinessCategoryRepository;
import com.shimpimilan.repository.business.BusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
public class BusinessSeeder implements CommandLineRunner {

    @Autowired
    private BusinessRepository businessRepository;
    
    @Autowired
    private BusinessCategoryRepository categoryRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Get any user
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            System.out.println("No users found to assign business.");
            return;
        }
        User admin = users.get(0);

        // Create category if not exists
        String catName = "IT Company / Software Development";
        BusinessCategory category = categoryRepository.findByName(catName).orElseGet(() -> {
            BusinessCategory c = BusinessCategory.builder()
                .name(catName)
                .isActive(true)
                .build();
            return categoryRepository.save(c);
        });

        // Check if business exists
        List<Business> existing = businessRepository.findAll();
        boolean exists = existing.stream().anyMatch(b -> b.getBusinessName().contains("ArnavInfoWeb"));
        if (exists) {
            return;
        }

        Business business = Business.builder()
            .owner(admin)
            .category(category)
            .businessName("ArnavInfoWeb IT Company")
            .ownerName("ArnavInfoWeb Owner")
            .mobileNumber("9158011580")
            .whatsappNumber("8767778028")
            .email("contactarnavinfoweb@gmail.com")
            .website("https://arnavinfoweb.in")
            .description("ArnavInfoWeb IT Company specializes in modern software solutions including:\n\n" +
                         "• Website Development\n" +
                         "• Mobile Application Development\n" +
                         "• Custom Software Development\n" +
                         "• Matrimonial Website Development\n" +
                         "• Business Management Software\n" +
                         "• ERP Solutions\n" +
                         "• CRM Development\n" +
                         "• E-Commerce Solutions\n" +
                         "• UI/UX Design\n" +
                         "• Digital Marketing\n" +
                         "• SEO Services\n" +
                         "• Cloud Solutions\n" +
                         "• API Integration\n" +
                         "• Maintenance & Support\n\n" +
                         "Transforming ideas into digital excellence.")
            .city("Nashik")
            .state("Maharashtra")
            .addressLine("Nashik, Maharashtra, India")
            .logoUrl("/uploads/business/arnav-logo.png")
            .coverUrl("/uploads/business/arnav-logo.png")
            .status(BusinessStatus.ACTIVE)
            .isVerified(true)
            .planType(AdvertisementPlan.GOLD)
            .isAdminFeatured(true)
            .priorityOverride(100)
            .build();

        businessRepository.save(business);
        System.out.println("====== ArnavInfoWeb Business Seeded Successfully ======");
    }
}
