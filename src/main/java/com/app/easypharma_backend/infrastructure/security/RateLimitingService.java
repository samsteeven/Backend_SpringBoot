package com.app.easypharma_backend.infrastructure.security;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@Slf4j
public class RateLimitingService {

    private final Cache<String, Bucket> cache;

    public RateLimitingService() {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    private Bucket createNewBucket() {
        // Configuration: 3 requêtes par heure
        Bandwidth limit = Bandwidth.builder()
                .capacity(3)
                .refillGreedy(3, Duration.ofHours(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public boolean tryConsume(String key) {
        Bucket bucket = cache.getIfPresent(key);

        if (bucket == null) {
            bucket = createNewBucket();
            cache.put(key, bucket);
        }

        return bucket.tryConsume(1);
    }

    public void resetLimit(String key) {
        cache.invalidate(key);
        log.info("Rate limit reset for key: {}", key);
    }

    public long getAvailableTokens(String key) {
        Bucket bucket = cache.getIfPresent(key);
        if (bucket == null) {
            return 3; // Limite initiale
        }
        return bucket.getAvailableTokens();
    }
}
