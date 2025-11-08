package co.edu.unicauca.sgd.api.service.calendario.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import co.edu.unicauca.sgd.api.domain.NombreFecha;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.NombreFechaDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.NombreFechaDTOResponse;
import co.edu.unicauca.sgd.api.exception.calendario.NombreFechaConsultaException;
import co.edu.unicauca.sgd.api.exception.calendario.NombreFechaNoEncontradoException;
import co.edu.unicauca.sgd.api.exception.calendario.NombreFechaOperacionNoPermitidaException;
import co.edu.unicauca.sgd.api.exception.calendario.NombreFechaProcesoException;
import co.edu.unicauca.sgd.api.mapper.NombreFechaMapper;
import co.edu.unicauca.sgd.api.repository.NombreFechaRepository;
import co.edu.unicauca.sgd.api.service.calendario.NombreFechaService;
import co.edu.unicauca.sgd.api.utils.StringUtils;
import jakarta.transaction.Transactional;

@Service
public class NombreFechaServiceImpl implements NombreFechaService {

    private static final List<Integer> EXCLUDED_IDS = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 26, 27, 28);
    private static final int LAST_PROTECTED_ID = EXCLUDED_IDS.stream().mapToInt(Integer::intValue).max().orElse(0);
    private static final Sort SORT_BY_ID = Sort.by("oidNombreFecha").ascending();

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
            Pageable sortedPageable = ensureSorted(pageable);
            Page<NombreFecha> page;
            if (StringUtils.hasText(nombre)) {
                page = nombreFechaRepository.findByNombreContainingIgnoreCaseAndOidNombreFechaNotIn(
                        nombre, EXCLUDED_IDS, sortedPageable);
            } else {
                page = nombreFechaRepository.findByOidNombreFechaNotIn(EXCLUDED_IDS, sortedPageable);
            }
            Page<NombreFechaDTOResponse> mapped = page.map(nombreFechaMapper::toResponse);
            boolean hasContent = mapped.hasContent();
            String message = hasContent ? "Registros obtenidos correctamente" : "No se encontraron nombres de fecha.";
            return new ApiResponse<>(200, message, mapped);
        } catch (Exception e) {
            NombreFechaConsultaException ex =
                    new NombreFechaConsultaException("Error al listar NombreFecha", e);
            return new ApiResponse<>(500, ex.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<NombreFechaDTOResponse> buscarPorId(Integer oid) {
        try {
            NombreFecha nf = nombreFechaRepository.findById(oid)
                    .orElseThrow(() -> new NombreFechaNoEncontradoException(oid));
            return new ApiResponse<>(200, "Encontrado", nombreFechaMapper.toResponse(nf));
        } catch (NombreFechaNoEncontradoException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            NombreFechaConsultaException ex =
                    new NombreFechaConsultaException("Error al buscar NombreFecha", e);
            return new ApiResponse<>(500, ex.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<NombreFechaDTOResponse> guardar(NombreFechaDTORequest dto) {
        try {
            NombreFecha entidad = nombreFechaMapper.toEntity(dto);
            entidad.setUsuarioCreacion("system");
            NombreFecha guardado = nombreFechaRepository.save(entidad);
            return new ApiResponse<>(200, "Creado correctamente", nombreFechaMapper.toResponse(guardado));
        } catch (Exception e) {
            NombreFechaProcesoException ex =
                    new NombreFechaProcesoException("Error al crear NombreFecha", e);
            return new ApiResponse<>(500, ex.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<NombreFechaDTOResponse> actualizar(Integer oid, NombreFechaDTORequest dto) {
        try {
            validarOperacionPermitida(oid);

            NombreFecha existente = nombreFechaRepository.findById(oid)
                    .orElseThrow(() -> new NombreFechaNoEncontradoException(oid));

            nombreFechaMapper.update(existente, dto);
            existente.setUsuarioActualizacion("system");
            NombreFecha actualizado = nombreFechaRepository.save(existente);
            return new ApiResponse<>(200, "Actualizado correctamente", nombreFechaMapper.toResponse(actualizado));
        } catch (NombreFechaOperacionNoPermitidaException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (NombreFechaNoEncontradoException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            NombreFechaProcesoException ex =
                    new NombreFechaProcesoException("Error al actualizar NombreFecha", e);
            return new ApiResponse<>(500, ex.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            validarOperacionPermitida(oid);

            if (!nombreFechaRepository.existsById(oid)) {
                throw new NombreFechaNoEncontradoException(oid);
            }
            nombreFechaRepository.deleteById(oid);
            return new ApiResponse<>(200, "Eliminado correctamente", null);
        } catch (NombreFechaOperacionNoPermitidaException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (NombreFechaNoEncontradoException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            NombreFechaProcesoException ex =
                    new NombreFechaProcesoException("Error al eliminar NombreFecha", e);
            return new ApiResponse<>(500, ex.getMessage(), null);
        }
    }

    private void validarOperacionPermitida(Integer oid) {
        if (oid == null) {
            throw new NombreFechaOperacionNoPermitidaException("El identificador es obligatorio.");
        }
        if (EXCLUDED_IDS.contains(oid) || oid <= LAST_PROTECTED_ID) {
            throw new NombreFechaOperacionNoPermitidaException(
                    "Solo se pueden actualizar o eliminar registros con ID mayor a " + LAST_PROTECTED_ID + ".");
        }
    }

    private Pageable ensureSorted(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, Integer.MAX_VALUE, SORT_BY_ID);
        }
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort().and(SORT_BY_ID)
                : SORT_BY_ID;
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
