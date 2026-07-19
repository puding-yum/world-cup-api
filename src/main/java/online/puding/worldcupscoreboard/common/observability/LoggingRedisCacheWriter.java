package online.puding.worldcupscoreboard.common.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnection;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Membungkus {@link RedisCacheWriter} untuk mencatat tiap operasi tulis ke Redis
 * (put/store/putIfAbsent) dan tiap invalidasi (evict/remove/clear/clean), plus
 * tiap cache-miss yang memicu repopulasi pada jalur {@code @Cacheable(sync=true)}.
 *
 * <p>Setiap method didelegasikan apa adanya ke writer asli — termasuk jalur
 * single-flight untuk {@code sync=true} — sehingga perilaku cache maupun request
 * coalescing TIDAK berubah; kelas ini hanya menambah log. Tiap panggilan tingkat
 * atas di-log tepat sekali karena delegasi selalu ke method yang sama pada
 * delegate (tidak masuk balik ke logger lain di kelas ini). Logger dipisah agar
 * gampang dimatikan lewat konfigurasi level log.
 */
public class LoggingRedisCacheWriter implements RedisCacheWriter {

    private static final Logger log = LoggerFactory.getLogger(LoggingRedisCacheWriter.class);

    private final RedisCacheWriter delegate;

    public LoggingRedisCacheWriter(RedisCacheWriter delegate) {
        this.delegate = delegate;
    }

    private static String k(byte[] key) {
        return key == null ? "null" : new String(key, StandardCharsets.UTF_8);
    }

    // --- tulis: log tepat sekali, lalu delegasikan ke method yang sama ---

    @Override
    public void put(String name, byte[] key, byte[] value, Duration ttl) {
        log.info("REDIS put cache={} key={}", name, k(key));
        delegate.put(name, key, value, ttl);
    }

    @Override
    public CompletableFuture<Void> store(String name, byte[] key, byte[] value, Duration ttl) {
        log.info("REDIS store cache={} key={}", name, k(key));
        return delegate.store(name, key, value, ttl);
    }

    @Override
    public byte[] putIfAbsent(String name, byte[] key, byte[] value, Duration ttl) {
        log.info("REDIS putIfAbsent cache={} key={}", name, k(key));
        return delegate.putIfAbsent(name, key, value, ttl);
    }

    @Override
    public void evict(String name, byte[] key) {
        log.info("REDIS evict cache={} key={}", name, k(key));
        delegate.evict(name, key);
    }

    @Override
    public void remove(String name, byte[] key) {
        log.info("REDIS remove cache={} key={}", name, k(key));
        delegate.remove(name, key);
    }

    @Override
    public boolean evictIfPresent(String name, byte[] key) {
        log.info("REDIS evictIfPresent cache={} key={}", name, k(key));
        return delegate.evictIfPresent(name, key);
    }

    @Override
    public boolean invalidate(String name, byte[] key) {
        log.info("REDIS invalidate cache={} key={}", name, k(key));
        return delegate.invalidate(name, key);
    }

    @Override
    public void clear(String name, byte[] pattern) {
        log.info("REDIS clear cache={}", name);
        delegate.clear(name, pattern);
    }

    @Override
    public void clean(String name, byte[] pattern) {
        log.info("REDIS clean cache={}", name);
        delegate.clean(name, pattern);
    }

    // --- sync=true: supplier hanya terpanggil saat miss (menandai repopulasi) ---

    @Override
    public byte[] get(String name, byte[] key, Supplier<byte[]> valueLoader, Duration ttl, boolean timeToIdleEnabled) {
        Supplier<byte[]> logged = () -> {
            log.info("REDIS miss->load cache={} key={}", name, k(key));
            return valueLoader.get();
        };
        return delegate.get(name, key, logged, ttl, timeToIdleEnabled);
    }

    // --- baca & lainnya: delegasi murni, tanpa log ---

    @Override
    public byte[] get(String name, byte[] key) {
        return delegate.get(name, key);
    }

    @Override
    public byte[] get(String name, byte[] key, Duration ttl) {
        return delegate.get(name, key, ttl);
    }

    @Override
    public boolean supportsAsyncRetrieve() {
        return delegate.supportsAsyncRetrieve();
    }

    @Override
    public CompletableFuture<byte[]> retrieve(String name, byte[] key) {
        return delegate.retrieve(name, key);
    }

    @Override
    public CompletableFuture<byte[]> retrieve(String name, byte[] key, Duration ttl) {
        return delegate.retrieve(name, key, ttl);
    }

    @Override
    public <T> T execute(Function<RedisConnection, T> callback) {
        return delegate.execute(callback);
    }

    @Override
    public void clearStatistics(String name) {
        delegate.clearStatistics(name);
    }

    @Override
    public RedisCacheWriter withStatisticsCollector(CacheStatisticsCollector cacheStatisticsCollector) {
        return new LoggingRedisCacheWriter(delegate.withStatisticsCollector(cacheStatisticsCollector));
    }

    @Override
    public CacheStatistics getCacheStatistics(String cacheName) {
        return delegate.getCacheStatistics(cacheName);
    }
}
