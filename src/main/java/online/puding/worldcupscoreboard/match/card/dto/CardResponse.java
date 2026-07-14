package online.puding.worldcupscoreboard.match.card.dto;

public record CardResponse(
        Long id,
        Long matchTeamId,
        Long playerId,
        String cardType,
        Integer minute,
        Integer second) {
}
