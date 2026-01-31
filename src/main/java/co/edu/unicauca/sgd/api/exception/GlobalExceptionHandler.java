package co.edu.unicauca.sgd.api.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.apache.catalina.connector.ClientAbortException;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.exception.materias.MateriasException;
import co.edu.unicauca.sgd.api.exception.necesidad.NecesidadException;
import co.edu.unicauca.sgd.api.exception.seleccionado.SeleccionadoException;
import co.edu.unicauca.sgd.api.exception.usuarioactividad.UsuarioActividadCalendarioException;
import jakarta.persistence.EntityNotFoundException;

/**
 * Manejo global de excepciones en la aplicacion.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ClassCastException.class)
    public ResponseEntity<ApiResponse<Void>> handleClassCastException(ClassCastException e) {
        logger.error("[ERROR] Error de conversion de tipos: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error de conversion de tipos: " + e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("[WARN] Parametro invalido: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
        logger.warn("[WARN] Estado ilegal: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        StringBuilder mensaje = new StringBuilder("Error de validacion: ");
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            mensaje.append(error.getField())
                   .append(" ")
                   .append(error.getDefaultMessage())
                   .append("; ");
        }
        if (mensaje.length() > 2) {
            mensaje.setLength(mensaje.length() - 2);
        }
        logger.warn("[WARN] Validacion de argumentos: {}", mensaje);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, mensaje.toString());
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(InvalidDataAccessApiUsageException ex) {
        logger.warn("[WARN] Error en consulta de datos: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Error en la consulta de datos: " + ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFoundException(EntityNotFoundException ex) {
        logger.warn("[WARN] Entidad no encontrada: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDatabaseException(DataAccessException ex) {
        logger.error("[ERROR] Error en la base de datos: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Error en la base de datos: " + ex.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiResponse<Void>> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        logger.warn("[WARN] Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(ValidacionNegocioException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidacionNegocio(ValidacionNegocioException ex) {
        logger.warn("[WARN] Validacion de negocio incumplida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(UsuarioDepartamentoException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsuarioDepartamentoException(UsuarioDepartamentoException ex) {
        HttpStatus status = ex.getStatus();
        logger.warn("[WARN] Usuario-Departamento: {}", ex.getMessage());
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(status.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(AsignacionHorasExcedidasException.class)
    public ResponseEntity<ApiResponse<Void>> handleAsignacionHorasExcedidas(AsignacionHorasExcedidasException ex) {
        logger.warn("[WARN] Limite de horas excedido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(HttpStatus.CONFLICT.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(AsignacionException.class)
    public ResponseEntity<ApiResponse<Void>> handleAsignacionException(AsignacionException ex) {
        logger.warn("[WARN] Asignacion: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(new ApiResponse<>(ex.getStatus().value(), ex.getMessage(), null));
    }

    @ExceptionHandler(UsuarioActividadCalendarioException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsuarioActividadCalendarioException(UsuarioActividadCalendarioException ex) {
        HttpStatus status = ex.getStatus();
        logger.error("[ERROR] Usuario-Actividad-Calendario: {}", ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(status.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(NecesidadException.class)
    public ResponseEntity<ApiResponse<Void>> handleNecesidadException(NecesidadException ex) {
        HttpStatus status = ex.getStatus();
        logger.warn("[WARN] Necesidad: {}", ex.getMessage());
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(status.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(SeleccionadoException.class)
    public ResponseEntity<ApiResponse<Void>> handleSeleccionadoException(SeleccionadoException ex) {
        HttpStatus status = ex.getStatus();
        logger.warn("[WARN] Seleccionado: {}", ex.getMessage());
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(status.value(), ex.getMessage(), null));
    }

    @ExceptionHandler({AsyncRequestNotUsableException.class, ClientAbortException.class})
    public void handleClientAbort(Exception ex) {
        logger.debug("[DEBUG] Conexion cerrada por el cliente: {}", ex.getMessage());
    }

    @ExceptionHandler(MateriasException.class)
    public ResponseEntity<ApiResponse<Void>> handleMateriasException(MateriasException ex) {
        HttpStatus status = ex.getStatus();
        logger.warn("[WARN] Materias/Planes: {}", ex.getMessage());
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(status.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception e) {
        logger.error("[ERROR] Excepcion no controlada: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error inesperado.");
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(HttpStatus status, String mensaje) {
        ApiResponse<Void> errorResponse = new ApiResponse<>(status.value(), mensaje, null);
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDatabaseConstraintViolation(DataIntegrityViolationException e) {
        String errorMessage = "Error de integridad de datos.";

        if (e.getCause() != null && e.getCause().getMessage() != null) {
            String detailedMessage = e.getCause().getMessage().toLowerCase();

            if (detailedMessage.contains("ora-00001") || detailedMessage.contains("unique constraint")) {
                errorMessage = "Error: Ya existe un registro con los mismos datos.";
            } else if (detailedMessage.contains("null value in column")) {
                errorMessage = "Uno o mas campos obligatorios estan vacios.";
            }
        }

        logger.warn("[DATABASE ERROR] {}", errorMessage);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(409, errorMessage, null));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        logger.error("[ERROR] Restriccion de clave unica violada: {}", e.getMessage());

        String errorMessage = "Error: Ya existe un registro con los mismos datos.";

        if (e.getSQLException() != null && e.getSQLException().getMessage().contains("ORA-00001")) {
            errorMessage = "Error: Ya existe un proceso con este Evaluador, Evaluado y Periodo Academico.";
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(409, errorMessage, null));
    }
}

