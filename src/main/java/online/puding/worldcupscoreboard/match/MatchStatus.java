package online.puding.worldcupscoreboard.match;

import java.util.Arrays;

/**
 * Status pertandingan. Nilai di database disimpan lowercase (upcoming/ongoing/ended)
 * sesuai CHECK constraint di skema.
 */
public enum MatchStatus {

    UPCOMING,
    ONGOING,
    ENDED;

    public String value() {
        return name().toLowerCase();
    }

    public static MatchStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(s -> s.value().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Status tidak valid: " + value));
    }
}
