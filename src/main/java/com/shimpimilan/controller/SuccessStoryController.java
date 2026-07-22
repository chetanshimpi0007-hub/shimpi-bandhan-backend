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

    @GetMapping
    @Cacheable("successStories")
    public ResponseEntity<List<SuccessStory>> getAllStories() {
        return ResponseEntity.ok(successStoryRepository.findAllByOrderByWeddingDateDesc());
    }

    // Admin endpoint
    @PostMapping
    @CacheEvict(value = "successStories", allEntries = true)
    public ResponseEntity<SuccessStory> createStory(@RequestBody SuccessStory story) {
        return ResponseEntity.ok(successStoryRepository.save(story));
    }

    // Admin endpoint
    @DeleteMapping("/{id}")
    @CacheEvict(value = "successStories", allEntries = true)
    public ResponseEntity<Void> deleteStory(@PathVariable Long id) {
        successStoryRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
