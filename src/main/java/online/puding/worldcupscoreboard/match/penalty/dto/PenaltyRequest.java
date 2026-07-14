package online.puding.worldcupscoreboard.match.penalty.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PenaltyRequest(
        @NotNull(message = "match_team_id wajib diisi")
        Long matchTeamId,

        Long playerId,

        @NotNull(message = "kick_order wajib diisi")
        @Min(value = 1, message = "kick_order minimal 1")
        Integer kickOrder,

        @NotNull(message = "is_scored wajib diisi")
        Boolean scored) {
}
