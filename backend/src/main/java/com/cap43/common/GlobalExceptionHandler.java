package com.cap43.common;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleStatus(ResponseStatusException ex, HttpServletRequest req) {
        int code = ex.getStatusCode().value();
        String reason = (ex.getStatusCode() instanceof HttpStatus hs) ? hs.getReasonPhrase() : "Error";
        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiError.of(code, reason, ex.getReason(), req.getRequestURI(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest req) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "Bad Request", "Validation failed", req.getRequestURI(), fields));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpServletRequest req) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "Bad Request", "Malformed request body", req.getRequestURI(), null));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleTooLarge(HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                .body(ApiError.of(413, "Content Too Large", "File exceeds the 10 MB limit", req.getRequestURI(), null));
    }

    
    @ExceptionHandler(ServletException.class)
    public ResponseEntity<ApiError> handleServlet(ServletException ex, HttpServletRequest req) {
        HttpStatusCode status = (ex instanceof ErrorResponse er) ? er.getStatusCode() : HttpStatus.BAD_REQUEST;
        String reason = (status instanceof HttpStatus hs) ? hs.getReasonPhrase() : "Error";
        String message = status.value() == 400 ? "Invalid request" : reason;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), reason, message, req.getRequestURI(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex, HttpServletRequest req) {
        log.error("Unhandled error on {}", req.getRequestURI(), ex);
        return ResponseEntity.status(500)
                .body(ApiError.of(500, "Internal Server Error", "Unexpected error", req.getRequestURI(), null));
    }
}
