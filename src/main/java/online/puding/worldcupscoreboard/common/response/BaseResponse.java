package online.puding.worldcupscoreboard.common.response;

import java.time.Instant;

/**
 * Struktur response seragam untuk semua endpoint request-response biasa
 * (bukan SSE). Lihat konvensi "Base response" di spesifikasi.
 */
public record BaseResponse<T>(
        boolean success,
        T data,
        ApiError error,
        Instant timestamp) {

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, data, null, Instant.now());
    }

    public static <T> BaseResponse<T> failure(ApiError error) {
        return new BaseResponse<>(false, null, error, Instant.now());
    }
}
