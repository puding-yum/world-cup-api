package online.puding.worldcupscoreboard.player.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlayerRequest(
        @NotBlank(message = "nama pemain wajib diisi")
        String name,

        @NotNull(message = "team_id wajib diisi")
        Long teamId,

        @Min(value = 0, message = "jersey_number minimal 0")
        @Max(value = 999, message = "jersey_number maksimal 999")
        Integer jerseyNumber,

        String position) {
}
