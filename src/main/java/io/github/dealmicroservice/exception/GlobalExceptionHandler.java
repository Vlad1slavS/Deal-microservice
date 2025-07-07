package io.github.dealmicroservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Глобальный обработчик исключений
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Обработка исключения EntityNotFoundException
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> entityNotFoundException(
            EntityNotFoundException ex, WebRequest request) {

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("error", "Сущность не найдена");
        errorBody.put("message", ex.getMessage());
        errorBody.put("status", HttpStatus.NOT_FOUND.value());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    /**
     * Обработка исключения EntityIsEmptyException
     */
    @ExceptionHandler(EntityIsEmptyException.class)
    public ResponseEntity<Map<String, Object>> entityIsEmptyException(
            EntityNotFoundException ex, WebRequest request) {

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("error", "Сущность пуста");
        errorBody.put("message", ex.getMessage());
        errorBody.put("status", HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    /**
     * Обработка ошибок валидации
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> validationErrors = new HashMap<>();

        List<FieldError> allErrors = ex.getBindingResult().getFieldErrors();
        for (FieldError error : allErrors) {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        }

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("error", "Ошибка валидации");
        errorBody.put("message", "Переданные данные не прошли валидацию");
        errorBody.put("validationErrors", validationErrors);
        errorBody.put("status", HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    /**
     * Обработка всех остальных исключений
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> otherExceptions(
            Exception ex, WebRequest request) {

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("error", "Внутренняя ошибка сервера");
        errorBody.put("typeError", ex.getClass().getSimpleName());
        errorBody.put("message", ex.getMessage());
        errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }

}
