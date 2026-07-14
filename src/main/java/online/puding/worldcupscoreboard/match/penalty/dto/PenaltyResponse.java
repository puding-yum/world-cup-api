package online.puding.worldcupscoreboard.match.penalty.dto;

public record PenaltyResponse(
        Long id,
        Long matchTeamId,
        Long playerId,
        Integer kickOrder,
        boolean scored) {
}
