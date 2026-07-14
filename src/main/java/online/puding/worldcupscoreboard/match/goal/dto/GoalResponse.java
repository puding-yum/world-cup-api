package online.puding.worldcupscoreboard.match.goal.dto;

public record GoalResponse(
        Long id,
        Long matchTeamId,
        Long playerId,
        Integer minute,
        Integer second) {
}
