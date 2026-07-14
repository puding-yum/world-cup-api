package online.puding.worldcupscoreboard.match.dto;

import java.time.Instant;
import java.util.List;

/** Ringkasan match untuk list (ongoing/upcoming/ended/next/prev/bracket). */
public record MatchSummaryResponse(
        Long id,
        String matchCode,
        String bracketPosition,
        Instant matchDate,
        String status,
        List<MatchTeamScoreResponse> teams) {
}
