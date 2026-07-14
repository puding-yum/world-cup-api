package online.puding.worldcupscoreboard.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Template URL aset eksternal (di-bind dari {@code app.assets.*}). Placeholder:
 * {@code {code}} untuk bendera, {@code {name}} untuk avatar. Tidak di-hardcode
 * supaya provider/ukuran gambar gampang diganti tanpa recompile.
 */
@ConfigurationProperties(prefix = "app.assets")
public record AssetProperties(
        String flagUrlTemplate,
        String avatarUrlTemplate) {
}
