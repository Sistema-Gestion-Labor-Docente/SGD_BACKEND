package co.edu.unicauca.sed.api.service.actividad.Impl;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.EstadoActividad;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.repository.EstadoActividadRepository;
import co.edu.unicauca.sed.api.service.actividad.EstadoActividadService;
import co.edu.unicauca.sed.api.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EstadoActividadServiceImpl implements EstadoActividadService {

    private final EstadoActividadRepository repository;
    private final StringUtils stringUtils;
    private static final Logger LOGGER = LoggerFactory.getLogger(EstadoActividadServiceImpl.class);

    @Override
    public ResponseEntity<ApiResponse<Page<EstadoActividad>>> obtenerTodos(Pageable pageable) {
        try {
            Page<EstadoActividad> estados = repository.findAll(pageable);
            return ResponseEntity.ok(new ApiResponse<>(200, "Estados de actividad obtenidos correctamente", estados));
        } catch (Exception e) {
            LOGGER.error("❌ Error al obtener los estados de actividad", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al obtener los estados de actividad", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<EstadoActividad>> buscarPorOid(Integer oid) {
        try {
            Optional<EstadoActividad> estado = repository.findById(oid);
            return estado.map(value -> ResponseEntity.ok(new ApiResponse<>(200, "Estado de actividad encontrado", value)))
                    .orElseGet(() -> ResponseEntity.status(404).body(new ApiResponse<>(404, "Estado de actividad no encontrado", null)));
        } catch (Exception e) {
            LOGGER.error("❌ Error al buscar el estado de actividad con OID: {}", oid, e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error interno al buscar el estado de actividad", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<EstadoActividad>> guardar(EstadoActividad estadoActividad) {
        try {
            estadoActividad.setNombre(stringUtils.safeToUpperCase(estadoActividad.getNombre()));
            EstadoActividad nuevoEstado = repository.save(estadoActividad);
            return ResponseEntity.ok(new ApiResponse<>(201, "Estado de actividad creado correctamente", nuevoEstado));
        } catch (Exception e) {
            LOGGER.error("❌ Error al guardar el EstadoActividad", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al guardar el estado de actividad", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<EstadoActividad>> actualizar(Integer oid, EstadoActividad estadoActividad) {
        try {
            if (!repository.existsById(oid)) {
                return ResponseEntity.status(404).body(new ApiResponse<>(404, "Estado de actividad no encontrado", null));
            }
            estadoActividad.setOidEstadoActividad(oid);
            estadoActividad.setNombre(stringUtils.safeToUpperCase(estadoActividad.getNombre()));

            EstadoActividad actualizado = repository.save(estadoActividad);
            LOGGER.info("✅ EstadoActividad actualizado correctamente: {}", actualizado);
            return ResponseEntity.ok(new ApiResponse<>(200, "Estado de actividad actualizado correctamente", actualizado));
        } catch (Exception e) {
            LOGGER.error("❌ Error al actualizar el EstadoActividad con OID: {}", oid, e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al actualizar el estado de actividad", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> eliminar(Integer oid) {
        try {
            if (!repository.existsById(oid)) {
                return ResponseEntity.status(404).body(new ApiResponse<>(404, "Estado de actividad no encontrado", null));
            }
            repository.deleteById(oid);
            LOGGER.info("✅ EstadoActividad eliminado con OID: {}", oid);
            return ResponseEntity.ok(new ApiResponse<>(200, "Estado de actividad eliminado correctamente", null));
        } catch (Exception e) {
            LOGGER.error("❌ Error al eliminar el EstadoActividad con OID: {}", oid, e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al eliminar el estado de actividad", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> asignarEstadoActividad(Actividad actividad, Integer oidEstadoActividad) {
        try {
            EstadoActividad estadoExistente = repository.findById(oidEstadoActividad)
                    .orElseThrow(() -> new IllegalArgumentException("Estado de actividad no válido."));

            actividad.setEstadoActividad(estadoExistente);
            return ResponseEntity.ok(new ApiResponse<>(200, "Estado de actividad asignado correctamente", null));
        } catch (Exception e) {
            LOGGER.error("❌ Error al asignar EstadoActividad con OID {}: {}", oidEstadoActividad, e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al asignar el estado de actividad", null));
        }
    }
}
