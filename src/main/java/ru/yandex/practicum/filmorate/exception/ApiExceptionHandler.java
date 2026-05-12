package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(404)
                .body(new ErrorResponse("Not found", ex.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        // Если сообщение содержит "не найден", то это ошибка ссылки на несуществующий ресурс → 404
        if (ex.getMessage().contains("не найден")) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("Not found", ex.getMessage()));
        }
        // Иначе это ошибка валидации → 400
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Validation error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex) {
        return ResponseEntity.status(500)
                .body(new ErrorResponse("Internal error", ex.getMessage()));
    }

    private record ErrorResponse(String error, String message) {}
}