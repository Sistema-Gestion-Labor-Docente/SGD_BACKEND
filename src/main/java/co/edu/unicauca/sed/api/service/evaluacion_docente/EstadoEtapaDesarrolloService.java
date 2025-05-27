package co.edu.unicauca.sed.api.service.evaluacion_docente;

import co.edu.unicauca.sed.api.domain.EstadoEtapaDesarrollo;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.repository.EstadoEtapaDesarrolloRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio para gestionar EstadoEtapaDesarrollo con soporte para CRUD y
 * paginación.
 */
@Service
@RequiredArgsConstructor
public class EstadoEtapaDesarrolloService {

    private final EstadoEtapaDesarrolloRepository estadoEtapaDesarrolloRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(EstadoEtapaDesarrolloService.class);

    public ResponseEntity<ApiResponse<Page<EstadoEtapaDesarrollo>>> obtenerTodos(int page, int size) {
        try {
            Page<EstadoEtapaDesarrollo> estados = estadoEtapaDesarrolloRepository.findAll(PageRequest.of(page, size));
            return ResponseEntity.ok(new ApiResponse<>(200, "Registros obtenidos correctamente", estados));
        } catch (Exception e) {
            LOGGER.error("❌ Error al obtener registros ESTADOETAPADESARROLLO", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al obtener registros", null));
        }
    }

    public ResponseEntity<ApiResponse<EstadoEtapaDesarrollo>> buscarPorId(Integer id) {
        try {
            Optional<EstadoEtapaDesarrollo> estado = estadoEtapaDesarrolloRepository.findById(id);
            return estado.map(value -> ResponseEntity.ok(new ApiResponse<>(200, "Registro encontrado", value)))
                    .orElseGet(() -> ResponseEntity.status(404)
                            .body(new ApiResponse<>(404, "Registro no encontrado", null)));
        } catch (Exception e) {
            LOGGER.error("❌ Error al obtener el registro ESTADOETAPADESARROLLO", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al obtener el registro", null));
        }
    }

    public ResponseEntity<ApiResponse<EstadoEtapaDesarrollo>> crear(EstadoEtapaDesarrollo estado) {
        try {
            EstadoEtapaDesarrollo nuevoEstado = estadoEtapaDesarrolloRepository.save(estado);
            return ResponseEntity.ok(new ApiResponse<>(201, "Registro creado exitosamente", nuevoEstado));
        } catch (Exception e) {
            LOGGER.error("❌ Error al crear el registro ESTADOETAPADESARROLLO", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al crear el registro", null));
        }
    }

    public ResponseEntity<ApiResponse<Void>> eliminar(Integer id) {
        try {
            if (!estadoEtapaDesarrolloRepository.existsById(id)) {
                return ResponseEntity.status(404).body(new ApiResponse<>(404, "Registro no encontrado", null));
            }
            estadoEtapaDesarrolloRepository.deleteById(id);
            LOGGER.info("✅ Registro ESTADOETAPADESARROLLO eliminado con ID: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Registro eliminado correctamente", null));
        } catch (Exception e) {
            LOGGER.error("❌ Error al eliminar el registro ESTADOETAPADESARROLLO", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al eliminar el registro", null));
        }
    }
}
