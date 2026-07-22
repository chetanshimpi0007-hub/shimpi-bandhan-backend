package com.shimpimilan.repository;

import com.shimpimilan.model.ProfileView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileViewRepository extends JpaRepository<ProfileView, Long> {
    Page<ProfileView> findByViewedUserIdOrderByViewedAtDesc(Long viewedUserId, Pageable pageable);
    
    Optional<ProfileView> findByViewerIdAndViewedUserId(Long viewerId, Long viewedUserId);
    
    long countByViewedUserId(Long viewedUserId);
}
