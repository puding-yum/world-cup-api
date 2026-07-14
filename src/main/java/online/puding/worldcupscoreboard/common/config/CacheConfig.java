package online.puding.worldcupscoreboard.common.config;

import online.puding.worldcupscoreboard.common.cache.CacheNames;
import online.puding.worldcupscoreboard.match.dto.MatchDetailResponse;
import online.puding.worldcupscoreboard.match.dto.MatchSummaryResponse;
import online.puding.worldcupscoreboard.player.dto.PlayerDetailResponse;
import online.puding.worldcupscoreboard.team.dto.TeamDetailResponse;
import online.puding.worldcupscoreboard.team.dto.TeamResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.TypeFactory;

import java.time.Duration;

/**
 * Konfigurasi cache Redis. Tiap cache diberi serializer JSON yang terikat pada
 * tipe konkretnya (termasuk elemen generic untuk List). Pendekatan ini menyimpan
 * JSON bersih di Redis (tanpa metadata tipe) dan menghindari kerapuhan default
 * typing yang gagal round-trip untuk List di posisi root. TTL parameterized dari
 * {@link CacheTtlProperties}; key diberi prefix (versi) agar tidak tabrakan dengan
 * proyek lain yang berbagi Redis. Invalidasi via {@code @CacheEvict} di service.
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheTtlProperties.class)
public class CacheConfig {

    private final String keyPrefix;
    private final TypeFactory typeFactory = JsonMapper.builder().build().getTypeFactory();

    public CacheConfig(@Value("${app.cache.key-prefix:worldcup:v1:}") String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    @Bean
    RedisCacheManagerBuilderCustomizer cacheCustomizer(CacheTtlProperties ttl) {
        return builder -> builder
                .withCacheConfiguration(CacheNames.MATCHES_ONGOING, listCache(MatchSummaryResponse.class, ttl.matchesOngoing()))
                .withCacheConfiguration(CacheNames.MATCHES_UPCOMING, listCache(MatchSummaryResponse.class, ttl.matchesUpcoming()))
                .withCacheConfiguration(CacheNames.MATCHES_ENDED, listCache(MatchSummaryResponse.class, ttl.matchesEnded()))
                .withCacheConfiguration(CacheNames.MATCHES_NEXT, nullableCache(MatchSummaryResponse.class, ttl.matchesNext()))
                .withCacheConfiguration(CacheNames.MATCHES_PREV, nullableCache(MatchSummaryResponse.class, ttl.matchesPrev()))
                .withCacheConfiguration(CacheNames.MATCHES_DETAIL, objectCache(MatchDetailResponse.class, ttl.matchesDetail()))
                .withCacheConfiguration(CacheNames.BRACKET, listCache(MatchSummaryResponse.class, ttl.bracket()))
                .withCacheConfiguration(CacheNames.TEAMS, listCache(TeamResponse.class, ttl.teams()))
                .withCacheConfiguration(CacheNames.TEAMS_DETAIL, listCache(TeamDetailResponse.class, ttl.teamsDetail()))
                .withCacheConfiguration(CacheNames.TEAM_DETAIL, objectCache(TeamDetailResponse.class, ttl.teamDetail()))
                .withCacheConfiguration(CacheNames.PLAYER_DETAIL, objectCache(PlayerDetailResponse.class, ttl.playerDetail()));
    }

    private RedisCacheConfiguration listCache(Class<?> elementType, long ttlSeconds) {
        JavaType type = typeFactory.constructCollectionType(java.util.List.class, elementType);
        return baseConfig(ttlSeconds, type);
    }

    private RedisCacheConfiguration objectCache(Class<?> type, long ttlSeconds) {
        return baseConfig(ttlSeconds, typeFactory.constructType(type));
    }

    /**
     * Untuk cache yang hasilnya boleh null ({@code matches:next} dan
     * {@code matches:prev} mengembalikan null kalau tidak ada match berikutnya /
     * sebelumnya). Tanpa ini Spring melempar IllegalArgumentException saat mau
     * menyimpan null. Tidak bisa dihindari dengan {@code unless} karena atribut
     * itu tidak didukung pada {@code @Cacheable(sync = true)}, sedangkan kedua
     * endpoint ini wajib pakai request coalescing. Null disimpan Redis sebagai
     * penanda khusus, tidak lewat serializer bertipe di bawah.
     */
    private RedisCacheConfiguration nullableCache(Class<?> type, long ttlSeconds) {
        return baseConfig(ttlSeconds, typeFactory.constructType(type), true);
    }

    private RedisCacheConfiguration baseConfig(long ttlSeconds, JavaType valueType) {
        return baseConfig(ttlSeconds, valueType, false);
    }

    private RedisCacheConfiguration baseConfig(long ttlSeconds, JavaType valueType, boolean allowNull) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        if (!allowNull) {
            config = config.disableCachingNullValues();
        }
        return config
                .entryTtl(Duration.ofSeconds(ttlSeconds))
                .computePrefixWith(cacheName -> keyPrefix + cacheName + "::")
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new JacksonJsonRedisSerializer<>(valueType)));
    }
}
