package co.edu.unicauca.sgd.api.service.calendario.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Fecha;
import co.edu.unicauca.sgd.api.domain.NombreFecha;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTOResponse;
import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;
import co.edu.unicauca.sgd.api.mapper.FechaMapper;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.FechaRepository;
import co.edu.unicauca.sgd.api.repository.NombreFechaRepository;
import co.edu.unicauca.sgd.api.service.calendario.FechaService;
import co.edu.unicauca.sgd.api.utils.StringUtils;

@Service
public class FechaServiceImpl implements FechaService {

    private final FechaRepository fechaRepository;

    private final CalendarioRepository calendarioRepository;

    private final NombreFechaRepository nombreFechaRepository;

    private final FechaMapper fechaMapper;

    public FechaServiceImpl(FechaRepository fechaRepository, CalendarioRepository calendarioRepository,
            NombreFechaRepository nombreFechaRepository, FechaMapper fechaMapper) {
        this.fechaRepository = fechaRepository;
        this.calendarioRepository = calendarioRepository;
        this.nombreFechaRepository = nombreFechaRepository;
        this.fechaMapper = fechaMapper;
    }

    
    @Override
    public ApiResponse<Page<FechaDTOResponse>> obtenerTodas(String nombre, TipoFechaEnum tipo, Pageable pageable) {
        try {
            Specification<Fecha> spec = Specification.where(null);

            if (StringUtils.hasText(nombre)) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.upper(root.join("nombreFecha").get("nombre")), "%" + nombre.toUpperCase() + "%"));
            }

            if (tipo != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("tipo"), tipo));
            }

            Page<Fecha> fechas = fechaRepository.findAll(spec, pageable);
            Page<FechaDTOResponse> responsePage = fechas.map(fechaMapper::toResponse);
            return new ApiResponse<>(200, "Fechas obtenidas correctamente", responsePage);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al obtener fechas: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<FechaDTOResponse> buscarPorId(Integer oid) {
        try {
            Fecha fecha = fechaRepository.findById(oid)
                    .orElseThrow(() -> new RuntimeException("Fecha no encontrada con ID: " + oid));
            return new ApiResponse<>(200, "Fecha encontrada correctamente", fechaMapper.toResponse(fecha));
        } catch (RuntimeException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<FechaDTOResponse> guardar(FechaDTORequest dto) {
        try {
            validarRango(dto.getFechaInicial(), dto.getFechaFin());

            Calendario calendario = calendarioRepository.findById(dto.getOidCalendario())
                    .orElseThrow(() -> new RuntimeException("Calendario no encontrado con ID: " + dto.getOidCalendario()));

            NombreFecha nombreFecha = nombreFechaRepository.findById(dto.getOidNombreFecha())
                    .orElseThrow(() -> new RuntimeException("NombreFecha no encontrado con ID: " + dto.getOidNombreFecha()));

            Fecha entidad = fechaMapper.convertToEntity(dto, calendario, nombreFecha);
            Fecha guardada = fechaRepository.save(entidad);
            return new ApiResponse<>(200, "Fecha guardada correctamente", fechaMapper.toResponse(guardada));
        } catch (RuntimeException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al guardar la fecha: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<FechaDTOResponse> actualizar(Integer id, FechaDTORequest dto) {
        try {
            validarRango(dto.getFechaInicial(), dto.getFechaFin());

            Fecha existente = fechaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Fecha no encontrada con ID: " + id));

            Calendario calendario = calendarioRepository.findById(dto.getOidCalendario())
                    .orElseThrow(() -> new RuntimeException("Calendario no encontrado con ID: " + dto.getOidCalendario()));

            NombreFecha nombreFecha = nombreFechaRepository.findById(dto.getOidNombreFecha())
                    .orElseThrow(() -> new RuntimeException("NombreFecha no encontrado con ID: " + dto.getOidNombreFecha()));

            fechaMapper.actualizarCamposBasicos(existente, dto, calendario, nombreFecha);
            Fecha actualizada = fechaRepository.save(existente);
            return new ApiResponse<>(200, "Fecha actualizada correctamente", fechaMapper.toResponse(actualizada));
        } catch (RuntimeException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al actualizar la fecha: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            if (!fechaRepository.existsById(oid)) {
                return new ApiResponse<>(404, "Fecha no encontrada con ID: " + oid, null);
            }
            fechaRepository.deleteById(oid);
            return new ApiResponse<>(200, "Fecha eliminada correctamente", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar la fecha: " + e.getMessage(), null);
        }
    }

    /* ------------ Helpers ------------ */
    private void validarRango(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio.isAfter(fin)) {
            throw new RuntimeException("La fecha inicial no puede ser mayor que la fecha fin.");
        }
    }
}
