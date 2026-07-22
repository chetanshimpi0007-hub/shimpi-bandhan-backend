package com.shimpimilan.service;

import com.shimpimilan.model.User;
import com.shimpimilan.model.photo.PhotoStatus;
import com.shimpimilan.model.photo.PhotoType;
import com.shimpimilan.model.photo.ProfilePhoto;
import com.shimpimilan.repository.ProfilePhotoRepository;
import com.shimpimilan.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfilePhotoService {

    private final ProfilePhotoRepository profilePhotoRepository;
    private final NotificationService notificationService;

    // We'll use a local directory for simplicity, mimicking StorageService
    private final String UPLOAD_DIR = "uploads/photos/";

    public ProfilePhoto uploadPhoto(User user, MultipartFile file, PhotoType type) throws IOException {
        log.info("[PhotoUpload] START userId={}, type={}, fileSize={}, contentType={}", user.getId(), type, file.getSize(), file.getContentType());
        long count = profilePhotoRepository.countByUserIdAndPhotoType(user.getId(), PhotoType.GALLERY);
        if (type == PhotoType.GALLERY && count >= 10) {
            throw new RuntimeException("Maximum 10 photos allowed.");
        }

        // Validate file size
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File size exceeds 5MB limit.");
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/jpg") && !contentType.equals("image/png") && !contentType.equals("image/webp"))) {
            throw new RuntimeException("Invalid file type. Only JPG, PNG, and WEBP are supported.");
        }

        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        String originalFilename = file.getOriginalFilename();
        String extension = ".jpg"; // Convert all to jpg since webp isn't natively supported by thumbnailator without plugins
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueId = UUID.randomUUID().toString();
        
        String mediumFileName = uniqueId + "_medium.jpg";
        String thumbFileName = uniqueId + "_thumb.jpg";

        Path tempFile = Files.createTempFile("upload_", extension);
        file.transferTo(tempFile.toFile());

        // Generate Medium (e.g. 1080x1080 max)
        File mediumFile = new File(UPLOAD_DIR + mediumFileName);
        Thumbnails.of(tempFile.toFile())
                .size(1080, 1080)
                .outputFormat("jpg")
                .outputQuality(0.8)
                .toFile(mediumFile);

        // Generate Thumbnail (150x150 circular-ready)
        File thumbFile = new File(UPLOAD_DIR + thumbFileName);
        Thumbnails.of(tempFile.toFile())
                .size(150, 150)
                .crop(net.coobird.thumbnailator.geometry.Positions.CENTER)
                .outputFormat("jpg")
                .outputQuality(0.8)
                .toFile(thumbFile);

        Files.deleteIfExists(tempFile);

        ProfilePhoto photo = ProfilePhoto.builder()
                .user(user)
                .photoUrl("/uploads/photos/" + mediumFileName)
                .thumbnailUrl("/uploads/photos/" + thumbFileName)
                .photoType(type)
                .status(PhotoStatus.PENDING)
                .isPrimary(false)
                .build();

        ProfilePhoto saved = profilePhotoRepository.save(photo);
        log.info("[PhotoUpload] SUCCESS userId={}, photoId={}, type={}, url={}", user.getId(), saved.getId(), type, saved.getPhotoUrl());
        
        notificationService.sendInAppNotification(user.getId(), "Photo Uploaded", "Your photo is pending admin approval.");
        
        return saved;
    }

    public List<ProfilePhoto> getPhotos(Long userId) {
        return profilePhotoRepository.findByUserId(userId);
    }

    public List<ProfilePhoto> getApprovedPhotos(Long userId) {
        return profilePhotoRepository.findByUserIdAndStatus(userId, PhotoStatus.APPROVED);
    }

    public void setPrimaryPhoto(Long userId, Long photoId) {
        ProfilePhoto photo = profilePhotoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
        
        if (!photo.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        if (photo.getStatus() != PhotoStatus.APPROVED) {
            throw new RuntimeException("Only approved photos can be set as primary");
        }

        // Unset old primary
        profilePhotoRepository.findByUserIdAndIsPrimaryTrue(userId).ifPresent(oldPrimary -> {
            oldPrimary.setIsPrimary(false);
            profilePhotoRepository.save(oldPrimary);
        });

        photo.setIsPrimary(true);
        photo.setPhotoType(PhotoType.PRIMARY);
        profilePhotoRepository.save(photo);
    }

    public void deletePhoto(Long userId, Long photoId) {
        ProfilePhoto photo = profilePhotoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
        if (!photo.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        // Delete files from disk (optional in this mock, but good practice)
        profilePhotoRepository.delete(photo);
    }

    // Admin Methods
    public ProfilePhoto approvePhoto(Long photoId, User admin) {
        ProfilePhoto photo = profilePhotoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
        
        photo.setStatus(PhotoStatus.APPROVED);
        photo.setApprovedBy(admin);
        photo.setApprovedAt(LocalDateTime.now());
        
        // If it's a primary photo, set it as primary and unset the old one
        if (photo.getPhotoType() == PhotoType.PRIMARY) {
            profilePhotoRepository.findByUserIdAndIsPrimaryTrue(photo.getUser().getId()).ifPresent(oldPrimary -> {
                oldPrimary.setIsPrimary(false);
                profilePhotoRepository.save(oldPrimary);
            });
            photo.setIsPrimary(true);
        }
        
        ProfilePhoto saved = profilePhotoRepository.save(photo);
        notificationService.sendInAppNotification(photo.getUser().getId(), "Photo Approved", "Your profile photo has been approved.");
        return saved;
    }

    public ProfilePhoto rejectPhoto(Long photoId, String reason, User admin) {
        ProfilePhoto photo = profilePhotoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
        
        photo.setStatus(PhotoStatus.REJECTED);
        photo.setRejectionReason(reason);
        photo.setIsPrimary(false); // Can't be primary if rejected
        
        ProfilePhoto saved = profilePhotoRepository.save(photo);
        notificationService.sendInAppNotification(photo.getUser().getId(), "Photo Rejected", "Your photo was rejected: " + reason);
        return saved;
    }

    public void adminDeletePhoto(Long photoId) {
        ProfilePhoto photo = profilePhotoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
        profilePhotoRepository.delete(photo);
    }
}
