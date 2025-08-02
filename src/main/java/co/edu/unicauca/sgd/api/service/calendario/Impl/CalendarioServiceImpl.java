package co.edu.unicauca.sgd.api.service.calendario.Impl;

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
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTOResponse;
import co.edu.unicauca.sgd.api.mapper.CalendarioMapper;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.service.calendario.CalendarioService;
import co.edu.unicauca.sgd.api.utils.StringUtils;

@Service
public class CalendarioServiceImpl implements CalendarioService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarioServiceImpl.class);

    @Autowired
    private CalendarioRepository calendarioRepository;

    @Autowired
    private CalendarioMapper calendarioMapper;

    @Override
    public ApiResponse<Page<CalendarioDTOResponse>> obtenerTodos(String nombreCalendario, String estado, Pageable pageable) {
        try {
            Specification<Calendario> spec = Specification.where(null);

            if (StringUtils.hasText(nombreCalendario)) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.upper(root.get("nombreCalendario")), "%" + nombreCalendario.toUpperCase() + "%"));
            }

            if (StringUtils.hasText(estado)) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(cb.upper(root.get("estado")), estado.toUpperCase()));
            }

            Page<Calendario> calendarios = calendarioRepository.findAll(spec, pageable);
            Page<CalendarioDTOResponse> responsePage = calendarios.map(calendarioMapper::toResponse);

            return new ApiResponse<>(200, "Calendarios encontrados correctamente.", responsePage);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al recuperar los calendarios: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<CalendarioDTOResponse> buscarPorId(Integer oid) {
        try {
            Calendario calendario = calendarioRepository.findById(oid)
                    .orElseThrow(() -> new RuntimeException("Calendario no encontrado con ID: " + oid));

            CalendarioDTOResponse dto = calendarioMapper.toResponse(calendario);
            return new ApiResponse<>(200, "Calendario encontrado correctamente.", dto);
        } catch (RuntimeException e) {
            return new ApiResponse<>(404, "Calendario no encontrado: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno al recuperar el calendario: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<CalendarioDTOResponse> guardar(CalendarioDTORequest request) {
        try {
            Calendario calendario = calendarioMapper.convertToEntity(request);
            Calendario guardado = calendarioRepository.save(calendario);
            CalendarioDTOResponse dto = calendarioMapper.toResponse(guardado);
            return new ApiResponse<>(200, "Calendario guardado correctamente.", dto);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al guardar el calendario: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<CalendarioDTOResponse> actualizar(Integer id, CalendarioDTORequest request) {
        try {
            Calendario existente = calendarioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Calendario no encontrado con ID: " + id));

            calendarioMapper.actualizarCamposBasicos(existente, request);
            Calendario actualizado = calendarioRepository.save(existente);
            CalendarioDTOResponse dto = calendarioMapper.toResponse(actualizado);
            return new ApiResponse<>(200, "Calendario actualizado correctamente.", dto);
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

