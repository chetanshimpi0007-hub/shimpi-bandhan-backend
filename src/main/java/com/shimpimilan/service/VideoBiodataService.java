package com.shimpimilan.service;

import com.shimpimilan.model.VideoBiodata;
import com.shimpimilan.repository.VideoBiodataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VideoBiodataService {

    private final VideoBiodataRepository videoBiodataRepository;

    public VideoBiodata saveVideoBiodata(VideoBiodata videoBiodata) {
        // Validate duration (max 60 seconds)
        if (videoBiodata.getDurationSeconds() != null && videoBiodata.getDurationSeconds() > 60) {
            throw new IllegalArgumentException("Video duration cannot exceed 60 seconds.");
        }
        
        // We assume file size validation happens at the controller/filter level or via Cloudinary directly,
        // but it's good to enforce the rules here conceptually.
        
        return videoBiodataRepository.save(videoBiodata);
    }
    
    public Optional<VideoBiodata> getVideoByUserId(Long userId) {
        return videoBiodataRepository.findByUserId(userId);
    }
    
    public VideoBiodata approveVideo(Long videoId) {
        VideoBiodata video = videoBiodataRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        video.setStatus(VideoBiodata.ApprovalStatus.APPROVED);
        return videoBiodataRepository.save(video);
    }

    public VideoBiodata rejectVideo(Long videoId) {
        VideoBiodata video = videoBiodataRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        video.setStatus(VideoBiodata.ApprovalStatus.REJECTED);
        return videoBiodataRepository.save(video);
    }
}
