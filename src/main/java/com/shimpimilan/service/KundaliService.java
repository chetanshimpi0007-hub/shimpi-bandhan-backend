package com.shimpimilan.service;

import com.shimpimilan.model.Kundali;
import com.shimpimilan.service.kundali.KundaliProvider;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class KundaliService {

    private final KundaliProvider kundaliProvider;
    private final Map<Long, Bucket> cache = new ConcurrentHashMap<>();

    public Map<String, Object> calculateGunMilan(Long requestingUserId, Kundali k1, Kundali k2) {
        Bucket bucket = resolveBucket(requestingUserId);
        if (!bucket.tryConsume(1)) {
            throw new RuntimeException("Rate limit exceeded. You can only perform 5 Kundali matches per hour.");
        }
        return kundaliProvider.calculateMatch(k1, k2);
    }

    private Bucket resolveBucket(Long userId) {
        return cache.computeIfAbsent(userId, this::newBucket);
    }

    private Bucket newBucket(Long userId) {
        // Limit: 5 requests per hour
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofHours(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
