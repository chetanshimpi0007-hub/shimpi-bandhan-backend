package com.shimpimilan.repository;

import com.shimpimilan.model.photo.PhotoStatus;
import com.shimpimilan.model.photo.PhotoType;
import com.shimpimilan.model.photo.ProfilePhoto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfilePhotoRepository extends JpaRepository<ProfilePhoto, Long> {
    
    List<ProfilePhoto> findByUserId(Long userId);
    
    List<ProfilePhoto> findByUserIdAndStatus(Long userId, PhotoStatus status);
    
    Optional<ProfilePhoto> findByUserIdAndIsPrimaryTrue(Long userId);
    
    Page<ProfilePhoto> findByStatus(PhotoStatus status, Pageable pageable);
    
    long countByUserId(Long userId);
    
    long countByUserIdAndPhotoType(Long userId, PhotoType photoType);
    
    long countByStatus(PhotoStatus status);
}
