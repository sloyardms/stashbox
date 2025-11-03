package com.sloyardms.stashbox.metrics;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/actuator/cache")
public class CacheMetricsController {

    @Autowired
    private CacheManager cacheManager;

    @GetMapping("/stats")
    public Map<String, Object> getCacheStats() {
        CaffeineCache cache = (CaffeineCache) cacheManager.getCache("userIdByExternalId");
        Cache<Object, Object> nativeCache = cache.getNativeCache();

        return Map.of(
                "size", nativeCache.estimatedSize(),
                "hitRate", nativeCache.stats().hitRate(),
                "missRate", nativeCache.stats().missRate(),
                "evictionCount", nativeCache.stats().evictionCount()
        );
    }

}
