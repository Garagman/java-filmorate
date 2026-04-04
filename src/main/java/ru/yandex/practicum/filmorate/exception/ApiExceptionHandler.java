package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(ValidationException ex) {
        log.warn("Ошибка валидации: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation error", ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException ex) {
        log.warn("Объект не найден: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Not found", ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleBadJson(HttpMessageNotReadableException ex) {
        log.warn("Пустой или некорректный JSON");
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad request", "Пустой или некорректный JSON");
    }

    private ResponseEntity<Map<String, String>> buildResponse(
            HttpStatus status,
            String error,
            String message
    ) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", error);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}