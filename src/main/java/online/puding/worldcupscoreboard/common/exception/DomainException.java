package online.puding.worldcupscoreboard.common.exception;

/**
 * Basis semua exception domain. Membawa {@link ErrorCode} sehingga
 * {@link GlobalExceptionHandler} bisa memetakan ke status HTTP dan kode error
 * yang sesuai tanpa perlu tahu tipe konkretnya.
 */
public class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    public DomainException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
