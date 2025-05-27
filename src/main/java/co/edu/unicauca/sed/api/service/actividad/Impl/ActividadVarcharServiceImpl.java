package co.edu.unicauca.sed.api.service.actividad.Impl;

import co.edu.unicauca.sed.api.domain.ActividadVarchar;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.repository.ActividadVarcharRepository;
import co.edu.unicauca.sed.api.service.actividad.ActividadVarcharService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementación del servicio para gestionar ActividadVarchar con soporte para CRUD y paginación.
 */
@Service
@RequiredArgsConstructor
public class ActividadVarcharServiceImpl implements ActividadVarcharService {

    private final ActividadVarcharRepository actividadVarcharRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ActividadVarcharServiceImpl.class);

    @Override
    public ResponseEntity<ApiResponse<Page<ActividadVarchar>>> obtenerTodos(int page, int size) {
        try {
            Page<ActividadVarchar> actividadVarchars = actividadVarcharRepository.findAll(PageRequest.of(page, size));
            return ResponseEntity.ok(new ApiResponse<>(200, "Registros obtenidos correctamente", actividadVarchars));
        } catch (Exception e) {
            LOGGER.error("❌ Error al obtener registros ACTIVIDADVARCHAR", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al obtener registros", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<ActividadVarchar>> buscarPorId(Integer id) {
        try {
            Optional<ActividadVarchar> actividadVarchar = actividadVarcharRepository.findById(id);
            return actividadVarchar
                    .map(value -> ResponseEntity.ok(new ApiResponse<>(200, "Registro encontrado", value)))
                    .orElseGet(() -> ResponseEntity.status(404)
                            .body(new ApiResponse<>(404, "Registro no encontrado", null)));
        } catch (Exception e) {
            LOGGER.error("❌ Error al obtener el registro ACTIVIDADVARCHAR", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al obtener el registro", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<ActividadVarchar>> crear(ActividadVarchar actividadVarchar) {
        try {
            if (actividadVarchar.getValor() != null) {
                actividadVarchar.setValor(actividadVarchar.getValor().toUpperCase());
            }

            ActividadVarchar nuevoRegistro = actividadVarcharRepository.save(actividadVarchar);
            return ResponseEntity.ok(new ApiResponse<>(201, "Registro creado exitosamente", nuevoRegistro));
        } catch (Exception e) {
            LOGGER.error("❌ Error al crear el registro ACTIVIDADVARCHAR", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al crear el registro", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<ActividadVarchar>> actualizar(Integer id, ActividadVarchar actividadVarchar) {
        try {
            if (!actividadVarcharRepository.existsById(id)) {
                return ResponseEntity.status(404).body(new ApiResponse<>(404, "Registro no encontrado", null));
            }
            actividadVarchar.setOidActividadVarchar(id);

            if (actividadVarchar.getValor() != null) {
                actividadVarchar.setValor(actividadVarchar.getValor().toUpperCase());
            }

            ActividadVarchar actualizado = actividadVarcharRepository.save(actividadVarchar);
            LOGGER.info("✅ Registro ACTIVIDADVARCHAR actualizado correctamente: {}", actualizado);
            return ResponseEntity.ok(new ApiResponse<>(200, "Registro actualizado correctamente", actualizado));
        } catch (Exception e) {
            LOGGER.error("❌ Error al actualizar el registro ACTIVIDADVARCHAR", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al actualizar el registro", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> eliminar(Integer id) {
        try {
            if (!actividadVarcharRepository.existsById(id)) {
                return ResponseEntity.status(404).body(new ApiResponse<>(404, "Registro no encontrado", null));
            }
            actividadVarcharRepository.deleteById(id);
            LOGGER.info("✅ Registro ACTIVIDADVARCHAR eliminado con ID: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Registro eliminado correctamente", null));
        } catch (Exception e) {
            LOGGER.error("❌ Error al eliminar el registro ACTIVIDADVARCHAR", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al eliminar el registro", null));
        }
    }
}
