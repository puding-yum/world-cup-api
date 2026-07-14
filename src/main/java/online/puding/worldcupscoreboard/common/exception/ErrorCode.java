package online.puding.worldcupscoreboard.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Kode error stabil beserta status HTTP yang sesuai. Dipakai oleh
 * {@link DomainException} dan dipetakan terpusat di
 * {@link GlobalExceptionHandler}.
 */
public enum ErrorCode {

    // 400 - format input tidak valid
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST),

    // 404 - resource tidak ada
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND),
    PLAYER_NOT_FOUND(HttpStatus.NOT_FOUND),
    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND),
    GOAL_NOT_FOUND(HttpStatus.NOT_FOUND),
    CARD_NOT_FOUND(HttpStatus.NOT_FOUND),
    PENALTY_NOT_FOUND(HttpStatus.NOT_FOUND),

    // 409 - bertentangan dengan kondisi data saat ini
    MATCH_NOT_ONGOING(HttpStatus.CONFLICT),
    TEAM_NOT_IN_MATCH(HttpStatus.CONFLICT),
    PLAYER_NOT_IN_TEAM(HttpStatus.CONFLICT),

    // 422 - format benar tapi melanggar aturan bisnis
    PENALTY_ORDER_TAKEN(HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_MATCH_TEAMS(HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_STATUS_TRANSITION(HttpStatus.UNPROCESSABLE_ENTITY),

    // 500
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
