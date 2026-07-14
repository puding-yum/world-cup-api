package online.puding.worldcupscoreboard.common.util;

import online.puding.worldcupscoreboard.common.config.AssetProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Membangun URL aset eksternal dari data yang dimiliki (bukan menyimpan file),
 * memakai template yang dikonfigurasi di {@link AssetProperties}. Bendera dari
 * flagcdn.com (kode ISO 3166-1 alpha-2), avatar dari ui-avatars.com.
 */
@Component
@EnableConfigurationProperties(AssetProperties.class)
public class AssetUrlBuilder {

    private final AssetProperties properties;

    public AssetUrlBuilder(AssetProperties properties) {
        this.properties = properties;
    }

    /** URL bendera, mis. code "AR" -> https://flagcdn.com/w320/ar.png */
    public String flag(String regionFlagCode) {
        if (regionFlagCode == null || regionFlagCode.isBlank()) {
            return null;
        }
        return properties.flagUrlTemplate().replace("{code}", regionFlagCode.trim().toLowerCase());
    }

    /** URL avatar dari nama pemain, mis. "Lionel Messi" -> ...?name=Lionel+Messi */
    public String avatar(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String encoded = URLEncoder.encode(name.trim(), StandardCharsets.UTF_8);
        return properties.avatarUrlTemplate().replace("{name}", encoded);
    }
}
