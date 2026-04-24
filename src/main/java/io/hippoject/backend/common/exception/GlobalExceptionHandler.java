package io.hippoject.backend.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiErrorResponse> handleConflict(ConflictException exception, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        String message = exception instanceof MethodArgumentNotValidException manve
                ? manve.getBindingResult().getFieldErrors().stream()
                        .map(FieldError::getDefaultMessage)
                        .findFirst()
                        .orElse("Die Anfrage ist ungültig")
                : exception instanceof MethodArgumentTypeMismatchException matme
                        ? "Ungültiger Wert für \"" + matme.getName() + "\""
                : exception instanceof HttpMessageNotReadableException
                        ? "Die Anfrage konnte nicht verarbeitet werden"
                : exception.getMessage();

        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiErrorResponse> handleForbidden(AccessDeniedException exception, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleGeneric(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unerwarteter Fehler", request);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()));
    }
}
