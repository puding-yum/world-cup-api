package online.puding.worldcupscoreboard.player.dto;

/** Detail pemain untuk GET /players/{id}, termasuk info tim (nama + bendera). */
public record PlayerDetailResponse(
        Long id,
        String name,
        Integer jerseyNumber,
        String position,
        String avatarUrl,
        Long teamId,
        String teamName,
        String teamFlagUrl) {
}
