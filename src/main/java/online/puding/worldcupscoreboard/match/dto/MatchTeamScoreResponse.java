package online.puding.worldcupscoreboard.match.dto;

/**
 * Satu tim dalam sebuah match beserta skornya (diturunkan dari jumlah gol).
 * {@code penaltyScore} hanya terisi kalau match punya adu penalti.
 */
public record MatchTeamScoreResponse(
        Long matchTeamId,
        Long teamId,
        String name,
        String regionMatchCode,
        String flagUrl,
        int score,
        Integer penaltyScore) {
}
