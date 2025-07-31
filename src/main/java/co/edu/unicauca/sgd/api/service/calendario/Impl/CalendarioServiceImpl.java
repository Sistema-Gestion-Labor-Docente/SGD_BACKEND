package co.edu.unicauca.sgd.api.service.calendario.Impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.service.calendario.CalendarioService;
import co.edu.unicauca.sgd.api.utils.StringUtils;

@Service
public class CalendarioServiceImpl implements CalendarioService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarioServiceImpl.class);

    @Autowired
    private CalendarioRepository calendarioRepository;

    @Override
    public ApiResponse<Page<Calendario>> obtenerTodos(String nombreCalendario, String estado, Pageable pageable) {
        try {
            Specification<Calendario> spec = Specification.where(null);

            if (StringUtils.hasText(nombreCalendario)) {
                spec = spec.and((root, query, cb) -> cb.like(cb.upper(root.get("nombreCalendario")), "%" + nombreCalendario.toUpperCase() + "%"));
            }

            if (StringUtils.hasText(estado)) {
                spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("estado")), estado.toUpperCase()));
            }

            Page<Calendario> calendarios = calendarioRepository.findAll(spec, pageable);
            return new ApiResponse<>(200, "Calendarios encontrados correctamente.", calendarios);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al recuperar los calendarios: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Calendario> buscarPorId(Integer oid) {
        try {
            Calendario calendario = calendarioRepository.findById(oid)
                    .orElseThrow(() -> new RuntimeException("Calendario no encontrado con ID: " + oid));
            return new ApiResponse<>(200, "Calendario encontrado correctamente.", calendario);
        } catch (RuntimeException e) {
            return new ApiResponse<>(404, "Calendario no encontrado: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno al recuperar el calendario: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Calendario> guardar(Calendario calendario) {
        try {
            Calendario guardado = calendarioRepository.save(calendario);
            return new ApiResponse<>(200, "Calendario guardado correctamente.", guardado);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al guardar el calendario: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Calendario> actualizar(Integer id, Calendario calendarioActualizado) {
        try {
            Calendario existente = calendarioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Calendario no encontrado con ID: " + id));

            existente.setNombreCalendario(calendarioActualizado.getNombreCalendario());
            existente.setSemanasClase(calendarioActualizado.getSemanasClase());
            existente.setSemanasPreparacion(calendarioActualizado.getSemanasPreparacion());
            existente.setHorasTotales(calendarioActualizado.getHorasTotales());
            existente.setUsuarioActualizacion(calendarioActualizado.getUsuarioActualizacion());
            existente.setEstado(calendarioActualizado.getEstado());
            existente.setObservacion(calendarioActualizado.getObservacion());

            Calendario guardado = calendarioRepository.save(existente);
            return new ApiResponse<>(200, "Calendario actualizado correctamente.", guardado);
        } catch (RuntimeException e) {
            return new ApiResponse<>(400, "Error en la actualización: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno al actualizar el calendario: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            if (!calendarioRepository.existsById(oid)) {
                return new ApiResponse<>(404, "Calendario no encontrado con ID: " + oid, null);
            }
            calendarioRepository.deleteById(oid);
            return new ApiResponse<>(200, "Calendario eliminado correctamente.", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar el calendario: " + e.getMessage(), null);
        }
    }
}

