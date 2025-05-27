package co.edu.unicauca.sed.api.service.actividad.Impl;

import co.edu.unicauca.sed.api.domain.ActividadDate;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.repository.ActividadDateRepository;
import co.edu.unicauca.sed.api.service.actividad.ActividadDateService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementación del servicio para gestionar ActividadDate con soporte para
 * CRUD y paginación.
 */
@Service
@RequiredArgsConstructor
public class ActividadDateServiceImpl implements ActividadDateService {

    private final ActividadDateRepository actividadDateRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ActividadDateServiceImpl.class);

    @Override
    public ResponseEntity<ApiResponse<Page<ActividadDate>>> obtenerTodos(int page, int size) {
        try {
            Page<ActividadDate> actividadDates = actividadDateRepository.findAll(PageRequest.of(page, size));
            return ResponseEntity.ok(new ApiResponse<>(200, "Registros obtenidos correctamente", actividadDates));
        } catch (Exception e) {
            LOGGER.error("❌ Error al obtener registros ACTIVIDADDATE", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al obtener registros", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<ActividadDate>> buscarPorId(Integer id) {
        try {
            Optional<ActividadDate> actividadDate = actividadDateRepository.findById(id);
            return actividadDate.map(value -> ResponseEntity.ok(new ApiResponse<>(200, "Registro encontrado", value)))
                    .orElseGet(() -> ResponseEntity.status(404)
                            .body(new ApiResponse<>(404, "Registro no encontrado", null)));
        } catch (Exception e) {
            LOGGER.error("❌ Error al obtener el registro ACTIVIDADDATE", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al obtener el registro", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<ActividadDate>> crear(ActividadDate actividadDate) {
        try {
            ActividadDate nuevoRegistro = actividadDateRepository.save(actividadDate);
            return ResponseEntity.ok(new ApiResponse<>(201, "Registro creado exitosamente", nuevoRegistro));
        } catch (Exception e) {
            LOGGER.error("❌ Error al crear el registro ACTIVIDADDATE", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al crear el registro", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<ActividadDate>> actualizar(Integer id, ActividadDate actividadDate) {
        try {
            if (!actividadDateRepository.existsById(id)) {
                return ResponseEntity.status(404).body(new ApiResponse<>(404, "Registro no encontrado", null));
            }
            actividadDate.setOidActividadDate(id);
            ActividadDate actualizado = actividadDateRepository.save(actividadDate);
            LOGGER.info("✅ Registro ACTIVIDADDATE actualizado correctamente: {}", actualizado);
            return ResponseEntity.ok(new ApiResponse<>(200, "Registro actualizado correctamente", actualizado));
        } catch (Exception e) {
            LOGGER.error("❌ Error al actualizar el registro ACTIVIDADDATE", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al actualizar el registro", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> eliminar(Integer id) {
        try {
            if (!actividadDateRepository.existsById(id)) {
                return ResponseEntity.status(404).body(new ApiResponse<>(404, "Registro no encontrado", null));
            }
            actividadDateRepository.deleteById(id);
            LOGGER.info("✅ Registro ACTIVIDADDATE eliminado con ID: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Registro eliminado correctamente", null));
        } catch (Exception e) {
            LOGGER.error("❌ Error al eliminar el registro ACTIVIDADDATE", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al eliminar el registro", null));
        }
    }
}
