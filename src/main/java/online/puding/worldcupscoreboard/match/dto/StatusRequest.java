package online.puding.worldcupscoreboard.match.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StatusRequest(
        @NotBlank(message = "status wajib diisi")
        @Pattern(regexp = "upcoming|ongoing|ended",
                message = "status harus upcoming, ongoing, atau ended")
        String status) {
}
