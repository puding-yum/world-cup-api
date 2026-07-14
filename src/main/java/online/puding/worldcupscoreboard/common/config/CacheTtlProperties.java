package online.puding.worldcupscoreboard.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TTL cache (dalam detik) per cache key. Di-bind dari {@code app.cache.ttl.*}
 * di application.yaml supaya bisa di-tuning tanpa recompile (lihat konvensi
 * "TTL parameterized").
 */
@ConfigurationProperties(prefix = "app.cache.ttl")
public record CacheTtlProperties(
        long matchesOngoing,
        long matchesUpcoming,
        long matchesEnded,
        long matchesNext,
        long matchesPrev,
        long matchesDetail,
        long bracket,
        long teams,
        long teamsDetail,
        long teamDetail,
        long playerDetail) {
}
