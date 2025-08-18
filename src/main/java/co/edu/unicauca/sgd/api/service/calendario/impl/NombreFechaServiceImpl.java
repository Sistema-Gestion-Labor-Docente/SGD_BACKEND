package co.edu.unicauca.sgd.api.service.calendario.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import co.edu.unicauca.sgd.api.domain.NombreFecha;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.NombreFechaDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.NombreFechaDTOResponse;
import co.edu.unicauca.sgd.api.mapper.NombreFechaMapper;
import co.edu.unicauca.sgd.api.repository.NombreFechaRepository;
import co.edu.unicauca.sgd.api.service.calendario.NombreFechaService;
import co.edu.unicauca.sgd.api.utils.StringUtils;
import jakarta.transaction.Transactional;

@Service
public class NombreFechaServiceImpl implements NombreFechaService {

    private final NombreFechaRepository nombreFechaRepository;
    
    private final NombreFechaMapper nombreFechaMapper;

    public NombreFechaServiceImpl(NombreFechaRepository nombreFechaRepository,
                                  NombreFechaMapper nombreFechaMapper) {
        this.nombreFechaRepository = nombreFechaRepository;
        this.nombreFechaMapper = nombreFechaMapper;
    }

    @Override
    public ApiResponse<Page<NombreFechaDTOResponse>> obtenerTodas(String nombre, Pageable pageable) {
        try {
            Page<NombreFecha> page;
            if (StringUtils.hasText(nombre)) {
                page = nombreFechaRepository.findByNombreContainingIgnoreCase(nombre, pageable);
            } else {
                page = nombreFechaRepository.findAll(pageable);
            }
            Page<NombreFechaDTOResponse> mapped = page.map(nombreFechaMapper::toResponse);
            return new ApiResponse<>(200, "Registros obtenidos correctamente", mapped);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al listar: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<NombreFechaDTOResponse> buscarPorId(Integer oid) {
        try {
            NombreFecha nf = nombreFechaRepository.findById(oid)
                    .orElseThrow(() -> new RuntimeException("NombreFecha no encontrado con ID: " + oid));
            return new ApiResponse<>(200, "Encontrado", nombreFechaMapper.toResponse(nf));
        } catch (RuntimeException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<NombreFechaDTOResponse> guardar(NombreFechaDTORequest dto) {
        try {
            NombreFecha entidad = nombreFechaMapper.toEntity(dto);
            NombreFecha guardado = nombreFechaRepository.save(entidad);
            return new ApiResponse<>(200, "Creado correctamente", nombreFechaMapper.toResponse(guardado));
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al crear: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<NombreFechaDTOResponse> actualizar(Integer oid, NombreFechaDTORequest dto) {
        try {
            NombreFecha existente = nombreFechaRepository.findById(oid)
                    .orElseThrow(() -> new RuntimeException("NombreFecha no encontrado con ID: " + oid));

            nombreFechaMapper.update(existente, dto);
            NombreFecha actualizado = nombreFechaRepository.save(existente);
            return new ApiResponse<>(200, "Actualizado correctamente", nombreFechaMapper.toResponse(actualizado));
        } catch (RuntimeException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al actualizar: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            if (!nombreFechaRepository.existsById(oid)) {
                return new ApiResponse<>(404, "NombreFecha no encontrado con ID: " + oid, null);
            }
            nombreFechaRepository.deleteById(oid);
            return new ApiResponse<>(200, "Eliminado correctamente", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar: " + e.getMessage(), null);
        }
    }
}
