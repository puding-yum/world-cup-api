package online.puding.worldcupscoreboard.match.dto;

/** Satu tendangan adu penalti, ditampilkan urut kick_order per tim. */
public record PenaltyKickResponse(
        Long id,
        Long matchTeamId,
        Long teamId,
        Integer kickOrder,
        boolean scored,
        String playerName) {
}
