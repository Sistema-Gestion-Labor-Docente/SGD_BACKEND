package co.edu.unicauca.sed.api.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manejo global de excepciones en la aplicación.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones de conversión de tipos (ClassCastException).
     */
    @ExceptionHandler(ClassCastException.class)
    public ResponseEntity<ApiResponse<Void>> handleClassCastException(ClassCastException e) {
        logger.error("❌ [ERROR] Error de conversión de tipos: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error de conversión de tipos: " + e.getMessage());
    }

    /**
     * Maneja excepciones de tipo IllegalArgumentException.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("⚠️ [WARN] Parámetro inválido: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /**
     * Maneja excepciones de estado ilegal (IllegalStateException).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
        logger.warn("⚠️ [WARN] Estado ilegal: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Maneja errores de acceso a datos inválidos en consultas.
     */
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(InvalidDataAccessApiUsageException ex) {
        logger.warn("⚠️ [WARN] Error en consulta de datos: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Error en la consulta de datos: " + ex.getMessage());
    }

    /**
     * Maneja excepciones cuando no se encuentra una entidad en la base de datos.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFoundException(EntityNotFoundException ex) {
        logger.warn("⚠️ [WARN] Entidad no encontrada: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Maneja errores generales de acceso a la base de datos.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDatabaseException(DataAccessException ex) {
        logger.error("❌ [ERROR] Error en la base de datos: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Error en la base de datos: " + ex.getMessage());
    }

    /**
     * Maneja cualquier otra excepción no capturada específicamente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception e) {
        logger.error("❌ [ERROR] Excepción no controlada: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error inesperado.");
    }

    /**
     * Método utilitario para construir la respuesta de error estándar.
     */
    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(HttpStatus status, String mensaje) {
        ApiResponse<Void> errorResponse = new ApiResponse<>(status.value(), mensaje, null);
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDatabaseConstraintViolation(DataIntegrityViolationException e) {
        String errorMessage = "Error de integridad de datos.";

        // Extraer el mensaje de error para detectar restricciones únicas
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            String detailedMessage = e.getCause().getMessage().toLowerCase();

            if (detailedMessage.contains("ora-00001") || detailedMessage.contains("unique constraint")) {
                errorMessage = "Error: Ya existe un registro con los mismos datos.";
            } else if (detailedMessage.contains("null value in column")) {
                errorMessage = "Uno o más campos obligatorios están vacíos.";
            }
        }

        logger.warn("⚠️ [DATABASE ERROR] {}", errorMessage);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(409, errorMessage, null));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        logger.error("❌ [ERROR] Restricción de clave única violada: {}", e.getMessage());

        String errorMessage = "Error: Ya existe un registro con los mismos datos.";

        if (e.getSQLException() != null && e.getSQLException().getMessage().contains("ORA-00001")) {
            errorMessage = "Error: Ya existe un proceso con este Evaluador, Evaluado y Período Académico.";
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(409, errorMessage, null));
    }

}
