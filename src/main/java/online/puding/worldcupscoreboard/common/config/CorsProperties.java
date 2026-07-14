package online.puding.worldcupscoreboard.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Origin frontend yang diizinkan (di-bind dari {@code app.cors.allowed-origins},
 * comma-separated). Sengaja tidak pakai wildcard — endpoint admin tanpa auth
 * tidak boleh bisa diakses domain sembarangan.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
