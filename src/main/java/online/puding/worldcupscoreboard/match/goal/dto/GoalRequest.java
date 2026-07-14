package online.puding.worldcupscoreboard.match.goal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** {@code matchTeamId} = tim yang diuntungkan gol ini (own-goal otomatis benar). */
public record GoalRequest(
        @NotNull(message = "match_team_id wajib diisi")
        Long matchTeamId,

        Long playerId,

        @Min(value = 0, message = "minute minimal 0")
        @Max(value = 200, message = "minute maksimal 200")
        Integer minute,

        @Min(value = 0, message = "second minimal 0")
        @Max(value = 59, message = "second maksimal 59")
        Integer second) {
}
