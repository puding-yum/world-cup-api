package online.puding.worldcupscoreboard.match.dto;

import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

/**
 * Body untuk POST/PUT match. {@code teamIds} opsional (match bracket bisa dibuat
 * sebelum kedua tim diketahui); kalau diisi harus tepat 2 tim berbeda —
 * divalidasi di service. {@code status} opsional, default "upcoming".
 */
public record MatchRequest(
        String matchCode,
        String bracketPosition,
        Instant matchDate,
        String status,

        @Size(max = 2, message = "sebuah match maksimal punya 2 tim")
        List<Long> teamIds) {
}
