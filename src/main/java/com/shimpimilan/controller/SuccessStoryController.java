package com.shimpimilan.controller;

import com.shimpimilan.model.SuccessStory;
import com.shimpimilan.repository.SuccessStoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;

@RestController
@RequestMapping("/api/v1/success-stories")
@RequiredArgsConstructor
public class SuccessStoryController {

    private final SuccessStoryRepository successStoryRepository;

    // Public Endpoint: Fetch all published stories
    @GetMapping
    @Cacheable("successStories")
    public ResponseEntity<List<SuccessStory>> getAllPublishedStories() {
        List<SuccessStory> stories = successStoryRepository.findByIsPublishedTrueOrderByDisplayOrderAscWeddingDateDesc();
        if (stories.isEmpty()) {
            stories = successStoryRepository.findAllByOrderByWeddingDateDesc();
        }
        return ResponseEntity.ok(stories);
    }

    // Public Endpoint: Fetch single story by ID
    @GetMapping("/{id}")
    public ResponseEntity<SuccessStory> getStoryById(@PathVariable Long id) {
        return successStoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Admin Endpoint: Fetch all stories (published & unpublished)
    @GetMapping("/all")
    public ResponseEntity<List<SuccessStory>> getAllStoriesForAdmin() {
        return ResponseEntity.ok(successStoryRepository.findAllByOrderByWeddingDateDesc());
    }

    // Admin Endpoint: Create story
    @PostMapping
    @CacheEvict(value = "successStories", allEntries = true)
    public ResponseEntity<SuccessStory> createStory(@RequestBody SuccessStory story) {
        if (story.getIsPublished() == null) story.setIsPublished(true);
        if (story.getIsFeatured() == null) story.setIsFeatured(true);
        if (story.getDisplayOrder() == null) story.setDisplayOrder(0);
        return ResponseEntity.ok(successStoryRepository.save(story));
    }

    // Admin Endpoint: Update story
    @PutMapping("/{id}")
    @CacheEvict(value = "successStories", allEntries = true)
    public ResponseEntity<SuccessStory> updateStory(@PathVariable Long id, @RequestBody SuccessStory updatedStory) {
        return successStoryRepository.findById(id).map(existing -> {
            existing.setGroomName(updatedStory.getGroomName());
            existing.setBrideName(updatedStory.getBrideName());
            existing.setWeddingDate(updatedStory.getWeddingDate());
            existing.setCity(updatedStory.getCity());
            existing.setShortStory(updatedStory.getShortStory());
            existing.setStory(updatedStory.getStory());
            existing.setPhotoUrl(updatedStory.getPhotoUrl());
            existing.setVideoUrl(updatedStory.getVideoUrl());
            existing.setGalleryImages(updatedStory.getGalleryImages());
            if (updatedStory.getIsFeatured() != null) existing.setIsFeatured(updatedStory.getIsFeatured());
            if (updatedStory.getDisplayOrder() != null) existing.setDisplayOrder(updatedStory.getDisplayOrder());
            if (updatedStory.getIsPublished() != null) existing.setIsPublished(updatedStory.getIsPublished());
            return ResponseEntity.ok(successStoryRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Admin Endpoint: Toggle Featured
    @PatchMapping("/{id}/toggle-featured")
    @CacheEvict(value = "successStories", allEntries = true)
    public ResponseEntity<SuccessStory> toggleFeatured(@PathVariable Long id) {
        return successStoryRepository.findById(id).map(story -> {
            story.setIsFeatured(!Boolean.TRUE.equals(story.getIsFeatured()));
            return ResponseEntity.ok(successStoryRepository.save(story));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Admin Endpoint: Toggle Published
    @PatchMapping("/{id}/toggle-published")
    @CacheEvict(value = "successStories", allEntries = true)
    public ResponseEntity<SuccessStory> togglePublished(@PathVariable Long id) {
        return successStoryRepository.findById(id).map(story -> {
            story.setIsPublished(!Boolean.TRUE.equals(story.getIsPublished()));
            return ResponseEntity.ok(successStoryRepository.save(story));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Admin Endpoint: Delete story
    @DeleteMapping("/{id}")
    @CacheEvict(value = "successStories", allEntries = true)
    public ResponseEntity<Void> deleteStory(@PathVariable Long id) {
        successStoryRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
