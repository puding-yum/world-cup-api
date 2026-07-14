package online.puding.worldcupscoreboard.player.dto;

/** Ringkasan pemain (dipakai di dalam team detail). */
public record PlayerResponse(
        Long id,
        String name,
        Long teamId,
        Integer jerseyNumber,
        String position,
        String avatarUrl) {
}
