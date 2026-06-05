package com.auth.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {

        String mensaje = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(" | "));

        return ResponseEntity.badRequest()
                .body(Map.of("error", mensaje));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(
            ConstraintViolationException ex) {

        String mensaje = ex.getConstraintViolations().stream()
                .map(cv -> cv.getMessage())
                .collect(Collectors.joining(" | "));

        return ResponseEntity.badRequest()
                .body(Map.of("error", mensaje));
    }

    // Username, email o teléfono duplicado
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDuplicate(
            DataIntegrityViolationException ex) {

        String mensaje = "Ya existe un usuario con ese username, email o teléfono";
        String cause = ex.getMostSpecificCause().getMessage().toLowerCase();

        if (cause.contains("username")) mensaje = "El username ya está registrado";
        else if (cause.contains("email")) mensaje = "El email ya está registrado";
        else if (cause.contains("phoneNumber")) mensaje = "El teléfono ya está registrado";

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", mensaje));
    }

    // Login incorrecto
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(
            BadCredentialsException ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Username o contraseña incorrectos"));
    }

    // Cualquier otro error no controlado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error interno: " + ex.getMessage()));
    }
}