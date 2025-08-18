package co.edu.unicauca.sgd.api.service.calendario.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import co.edu.unicauca.sgd.api.specification.CalendarioSpecs;
import co.edu.unicauca.sgd.api.utils.StringUtils;

@Service
public class CalendarioServiceImpl implements CalendarioService {

    private static final Logger logger = LoggerFactory.getLogger(CalendarioServiceImpl.class);

    private CalendarioRepository calendarioRepository;

    private CalendarioMapper calendarioMapper;

    public CalendarioServiceImpl(CalendarioRepository calendarioRepository, CalendarioMapper calendarioMapper) {
        this.calendarioRepository = calendarioRepository;
        this.calendarioMapper = calendarioMapper;
    }

    @Override
    public ApiResponse<Page<CalendarioDTOResponse>> obtenerTodos(String anioCalendario, Integer numeroCalendario, String estado, Pageable pageable) {
        try {
            Specification<Calendario> spec = Specification
                    .where(CalendarioSpecs.anioEq(anioCalendario))
                    .and(CalendarioSpecs.numeroEq(numeroCalendario))
                    .and(CalendarioSpecs.estadoEq(estado));

            Page<Calendario> calendarios = calendarioRepository.findAll(spec, pageable);
            Page<CalendarioDTOResponse> responsePage = calendarios.map(calendarioMapper::toResponse);

            logger.info("Calendarios encontrados: {}", responsePage.getTotalElements());
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
            logger.info("Calendario encontrado con ID: {}", oid);

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
            calendario.setUsuarioCreacion(
                StringUtils.hasText(calendario.getUsuarioCreacion()) ? calendario.getUsuarioCreacion() : "Usuario"
            );
            calendario.setEstado(
                StringUtils.hasText(calendario.getEstado()) ? calendario.getEstado() : "PENDIENTE"
            );
            Calendario guardado = calendarioRepository.save(calendario);
            CalendarioDTOResponse dto = calendarioMapper.toResponse(guardado);

            logger.info("Calendario guardado con ID: {}", guardado.getOidcalendario());
            return new ApiResponse<>(201, "Calendario guardado correctamente.", dto);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al guardar el calendario: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<CalendarioDTOResponse> actualizar(Integer oid, CalendarioDTORequest request) {
        try {
            Calendario existente = calendarioRepository.findById(oid)
                    .orElseThrow(() -> new RuntimeException("Calendario no encontrado con ID: " + oid));

            calendarioMapper.actualizarCamposBasicos(existente, request);
            existente.setUsuarioActualizacion("UsuarioActualizacion");

            Calendario actualizado = calendarioRepository.save(existente);
            CalendarioDTOResponse dto = calendarioMapper.toResponse(actualizado);

            logger.info("Calendario actualizado con ID: {}", oid);
            return new ApiResponse<>(200, "Calendario actualizado correctamente.", dto);
        } catch (RuntimeException e) {
            return new ApiResponse<>(400, "Error en la actualización: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno al actualizar el calendario: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            if (!calendarioRepository.existsById(oid)) {
                return new ApiResponse<>(404, "Calendario no encontrado con ID: " + oid, null);
            }
            calendarioRepository.deleteById(oid);
            logger.info("Calendario eliminado con ID: {}", oid);
            return new ApiResponse<>(204, "Calendario eliminado correctamente.", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar el calendario: " + e.getMessage(), null);
        }
    }
}

