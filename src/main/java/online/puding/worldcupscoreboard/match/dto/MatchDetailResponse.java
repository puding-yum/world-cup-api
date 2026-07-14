package online.puding.worldcupscoreboard.match.dto;

import java.time.Instant;
import java.util.List;

/**
 * Detail lengkap satu match untuk halaman detail: kedua tim + skor, timeline
 * kronologis (gol+kartu), dan adu penalti (kondisional). {@code winnerTeamId}
 * hanya terisi kalau status ended dan ada pemenang.
 */
public record MatchDetailResponse(
        Long id,
        String matchCode,
        String bracketPosition,
        Instant matchDate,
        String status,
        List<MatchTeamScoreResponse> teams,
        List<TimelineEventResponse> timeline,
        boolean hasShootout,
        List<PenaltyKickResponse> penalties,
        Long winnerTeamId) {
}
