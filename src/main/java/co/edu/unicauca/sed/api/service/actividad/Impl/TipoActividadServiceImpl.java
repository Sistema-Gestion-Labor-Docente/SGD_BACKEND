package co.edu.unicauca.sed.api.service.actividad.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import co.edu.unicauca.sed.api.domain.TipoActividad;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.repository.TipoActividadRepository;
import co.edu.unicauca.sed.api.service.actividad.TipoActividadService;

/**
 * Implementación del servicio de gestión de Tipos de Actividad.
 */
@Service
public class TipoActividadServiceImpl implements TipoActividadService {

    private static final Logger logger = LoggerFactory.getLogger(TipoActividadServiceImpl.class);

    @Autowired
    private TipoActividadRepository tipoActividadRepository;

    @Override
    public ApiResponse<Page<TipoActividad>> listarTodos(Pageable pageable) {
        Page<TipoActividad> pagina = tipoActividadRepository.findAll(pageable);
        return new ApiResponse<>(200, "Consulta realizada correctamente", pagina);
    }

    @Override
    public TipoActividad buscarPorOid(Integer oid) {
        return tipoActividadRepository.findById(oid).orElse(null);
    }

    @Override
    public ApiResponse<TipoActividad> guardar(TipoActividad tipoActividad) {
        try {
            tipoActividad.setNombre(tipoActividad.getNombre().toUpperCase());
            tipoActividad.setDescripcion(tipoActividad.getDescripcion().toUpperCase());

            TipoActividad savedTipoActividad = tipoActividadRepository.save(tipoActividad);
            return new ApiResponse<>(200, "Tipo de actividad guardado con éxito", savedTipoActividad);

        } catch (Exception e) {
            logger.error("❌ [ERROR] Error al guardar el tipo de actividad: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error al guardar el tipo de actividad: " + e.getMessage(), null);
        }
    }

    @Override
    public TipoActividad actualizar(Integer oid, TipoActividad tipoActividad) {
        return tipoActividadRepository.findById(oid).map(existingTipoActividad -> {
            if (tipoActividad.getDescripcion() != null) {
                tipoActividad.setDescripcion(tipoActividad.getDescripcion().toUpperCase());
            }
            if (tipoActividad.getNombre() != null) {
                tipoActividad.setNombre(tipoActividad.getNombre().toUpperCase());
            }
            tipoActividad.setOidTipoActividad(oid);
            return tipoActividadRepository.save(tipoActividad);
        }).orElseThrow(() -> new RuntimeException("TipoActividad con ID " + oid + " no encontrado."));
    }

    @Override
    public void eliminar(Integer oid) {
        tipoActividadRepository.deleteById(oid);
    }
}
