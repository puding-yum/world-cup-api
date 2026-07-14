package online.puding.worldcupscoreboard.match.card.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CardRequest(
        @NotNull(message = "match_team_id wajib diisi")
        Long matchTeamId,

        @NotNull(message = "player_id wajib diisi")
        Long playerId,

        @NotNull(message = "card_type wajib diisi")
        @Pattern(regexp = "yellow|red|second_yellow_red",
                message = "card_type harus yellow, red, atau second_yellow_red")
        String cardType,

        @Min(value = 0, message = "minute minimal 0")
        @Max(value = 200, message = "minute maksimal 200")
        Integer minute,

        @Min(value = 0, message = "second minimal 0")
        @Max(value = 59, message = "second maksimal 59")
        Integer second) {
}
