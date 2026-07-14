package online.puding.worldcupscoreboard.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Bagian error dari {@link BaseResponse}. {@code code} adalah kode error stabil
 * (mis. MATCH_NOT_ONGOING) yang bisa dibaca frontend, {@code message} pesan
 * yang bisa ditampilkan ke user.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String code, String message) {
}
