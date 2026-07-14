package online.puding.worldcupscoreboard.team.dto;

/** Ringkasan tim untuk GET /teams (dengan URL bendera siap pakai). */
public record TeamResponse(
        Long id,
        String name,
        String regionMatchCode,
        String regionFlagCode,
        String flagUrl) {
}
