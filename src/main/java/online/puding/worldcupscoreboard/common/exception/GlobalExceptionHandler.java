package online.puding.worldcupscoreboard.common.exception;

import online.puding.worldcupscoreboard.common.response.ApiError;
import online.puding.worldcupscoreboard.common.response.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * Menangkap semua exception secara terpusat dan memetakannya ke
 * {@link BaseResponse} dengan status HTTP yang benar. Status HTTP selalu
 * mencerminkan hasil sebenarnya — tidak pernah 200 untuk kegagalan.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<BaseResponse<Void>> handleDomain(DomainException ex) {
        ErrorCode code = ex.getErrorCode();
        // 5xx = kegagalan tak terduga, 4xx = kesalahan klien/kondisi data
        if (code.status().is5xxServerError()) {
            log.error("Domain error {}: {}", code, ex.getMessage(), ex);
        } else {
            log.warn("Domain error {}: {}", code, ex.getMessage());
        }
        return build(code.status(), code.name(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", message);
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR.name(), message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ErrorCode.MALFORMED_REQUEST.name(),
                "Request body tidak bisa dibaca atau formatnya salah");
    }

    /**
     * Path yang tidak ter-map ke handler mana pun / static resource yang tidak
     * ada (mis. request browser ke {@code /favicon.ico}). Ini kondisi normal,
     * bukan kegagalan server: balas 404, cukup log level debug — jangan sampai
     * catch-all di bawah menjadikannya ERROR + 500.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleNoResource(NoResourceFoundException ex) {
        log.debug("No resource for {}", ex.getResourcePath());
        return build(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND.name(),
                "Resource tidak ditemukan");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR.name(),
                "Terjadi kesalahan tak terduga di server");
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private ResponseEntity<BaseResponse<Void>> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(BaseResponse.failure(new ApiError(code, message)));
    }
}
