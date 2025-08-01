package co.edu.unicauca.sgd.api.service.calendario.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Fecha;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTOResponse;
import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;
import co.edu.unicauca.sgd.api.mapper.FechaMapper;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.FechaRepository;
import co.edu.unicauca.sgd.api.service.calendario.FechaService;
import co.edu.unicauca.sgd.api.utils.StringUtils;

@Service
public class FechaServiceImpl implements FechaService {

    @Autowired
    private FechaRepository fechaRepository;

    @Autowired
    private CalendarioRepository calendarioRepository;

    @Autowired
    private FechaMapper fechaMapper;

    @Override
    public ApiResponse<Page<FechaDTOResponse>> obtenerTodas(String nombre, TipoFechaEnum tipo, Pageable pageable) {
        try {
            Specification<Fecha> spec = Specification.where(null);

            if (StringUtils.hasText(nombre)) {
                spec = spec.and(
                        (root, query, cb) -> cb.like(cb.upper(root.get("nombre")), "%" + nombre.toUpperCase() + "%"));
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
            FechaDTOResponse response = fechaMapper.toResponse(fecha);
            return new ApiResponse<>(200, "Fecha encontrada correctamente", response);
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
            Calendario calendario = calendarioRepository.findById(dto.getOidCalendario())
                    .orElseThrow(
                            () -> new RuntimeException("Calendario no encontrado con ID: " + dto.getOidCalendario()));

            Fecha fecha = fechaMapper.convertToEntity(dto, calendario);
            Fecha guardada = fechaRepository.save(fecha);
            FechaDTOResponse response = fechaMapper.toResponse(guardada);
            return new ApiResponse<>(200, "Fecha guardada correctamente", response);
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
            Fecha existente = fechaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Fecha no encontrada con ID: " + id));

            Calendario calendario = calendarioRepository.findById(dto.getOidCalendario())
                    .orElseThrow(
                            () -> new RuntimeException("Calendario no encontrado con ID: " + dto.getOidCalendario()));

            fechaMapper.actualizarCamposBasicos(existente, dto, calendario);
            Fecha actualizada = fechaRepository.save(existente);
            FechaDTOResponse response = fechaMapper.toResponse(actualizada);
            return new ApiResponse<>(200, "Fecha actualizada correctamente", response);
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
}
