package com.andres.casa_dragones.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Manejador global de excepciones (@RestControllerAdvice).
 * Centraliza la captura de excepciones y mapea a respuestas JSON con estructura uniforme.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> handleNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        ErrorRespuesta error = new ErrorRespuesta(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "No encontrado",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorRespuesta> handleConflicto(ConflictoException ex, HttpServletRequest request) {
        ErrorRespuesta error = new ErrorRespuesta(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflicto",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<ErrorRespuesta> handleValidacion(ValidacionException ex, HttpServletRequest request) {
        ErrorRespuesta error = new ErrorRespuesta(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Solicitud inválida",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorRespuesta> handleDuplicateKey(DuplicateKeyException ex, HttpServletRequest request) {
        ErrorRespuesta error = new ErrorRespuesta(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflicto",
                "El registro ya existe o viola una restricción de unicidad.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String msg = "Violación de integridad de datos.";
        if (ex.getMessage() != null && ex.getMessage().contains("1451")) {
            msg = "No se puede eliminar el registro porque tiene relaciones asociadas.";
        }
        ErrorRespuesta error = new ErrorRespuesta(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflicto",
                msg,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorRespuesta error = new ErrorRespuesta(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Solicitud inválida",
                "El cuerpo de la petición contiene un JSON inválido o malformado.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((m1, m2) -> m1 + "; " + m2)
                .orElse("Datos inválidos");

        ErrorRespuesta error = new ErrorRespuesta(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Solicitud inválida",
                mensaje,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {}: ", request.getRequestURI(), ex);
        ErrorRespuesta error = new ErrorRespuesta(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                "Ocurrió un error inesperado al procesar la solicitud.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
