package online.puding.worldcupscoreboard.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamRequest(
        @NotBlank(message = "nama tim wajib diisi")
        String name,

        @NotBlank(message = "region_match_code wajib diisi")
        @Size(max = 10, message = "region_match_code maksimal 10 karakter")
        String regionMatchCode,

        @NotBlank(message = "region_flag_code wajib diisi")
        @Size(min = 2, max = 10, message = "region_flag_code harus 2-10 karakter")
        String regionFlagCode) {
}
