package online.puding.worldcupscoreboard.team.dto;

import online.puding.worldcupscoreboard.player.dto.PlayerResponse;

import java.util.List;

/** Tim beserta daftar pemainnya, untuk GET /teams/detail dan GET /teams/{id}. */
public record TeamDetailResponse(
        Long id,
        String name,
        String regionMatchCode,
        String regionFlagCode,
        String flagUrl,
        List<PlayerResponse> players) {
}
