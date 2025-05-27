package co.edu.unicauca.sed.api.service.actividad.Impl;

import co.edu.unicauca.sed.api.domain.ActividadInt;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.repository.ActividadIntRepository;
import co.edu.unicauca.sed.api.service.actividad.ActividadIntService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementación del servicio para gestionar ActividadInt con soporte para CRUD y paginación.
 */
@Service
@RequiredArgsConstructor
public class ActividadIntServiceImpl implements ActividadIntService {

    private final ActividadIntRepository actividadIntRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ActividadIntServiceImpl.class);

    @Override
    public ResponseEntity<ApiResponse<Page<ActividadInt>>> obtenerTodos(int page, int size) {
        try {
            Page<ActividadInt> actividadInts = actividadIntRepository.findAll(PageRequest.of(page, size));
            return ResponseEntity.ok(new ApiResponse<>(200, "Registros obtenidos correctamente", actividadInts));
        } catch (Exception e) {
            LOGGER.error("❌ Error al obtener registros ACTIVIDADINT", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al obtener registros", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<ActividadInt>> buscarPorId(Integer id) {
        try {
            Optional<ActividadInt> actividadInt = actividadIntRepository.findById(id);
            return actividadInt.map(value -> ResponseEntity.ok(new ApiResponse<>(200, "Registro encontrado", value)))
                    .orElseGet(() -> ResponseEntity.status(404)
                            .body(new ApiResponse<>(404, "Registro no encontrado", null)));
        } catch (Exception e) {
            LOGGER.error("❌ Error al obtener el registro ACTIVIDADINT", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al obtener el registro", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<ActividadInt>> crear(ActividadInt actividadInt) {
        try {
            ActividadInt nuevoRegistro = actividadIntRepository.save(actividadInt);
            return ResponseEntity.ok(new ApiResponse<>(201, "Registro creado exitosamente", nuevoRegistro));
        } catch (Exception e) {
            LOGGER.error("❌ Error al crear el registro ACTIVIDADINT", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al crear el registro", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<ActividadInt>> actualizar(Integer id, ActividadInt actividadInt) {
        try {
            if (!actividadIntRepository.existsById(id)) {
                return ResponseEntity.status(404).body(new ApiResponse<>(404, "Registro no encontrado", null));
            }
            actividadInt.setOidActividadInt(id);
            ActividadInt actualizado = actividadIntRepository.save(actividadInt);
            LOGGER.info("✅ Registro ACTIVIDADINT actualizado correctamente: {}", actualizado);
            return ResponseEntity.ok(new ApiResponse<>(200, "Registro actualizado correctamente", actualizado));
        } catch (Exception e) {
            LOGGER.error("❌ Error al actualizar el registro ACTIVIDADINT", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al actualizar el registro", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> eliminar(Integer id) {
        try {
            if (!actividadIntRepository.existsById(id)) {
                return ResponseEntity.status(404).body(new ApiResponse<>(404, "Registro no encontrado", null));
            }
            actividadIntRepository.deleteById(id);
            LOGGER.info("✅ Registro ACTIVIDADINT eliminado con ID: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Registro eliminado correctamente", null));
        } catch (Exception e) {
            LOGGER.error("❌ Error al eliminar el registro ACTIVIDADINT", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al eliminar el registro", null));
        }
    }
}
