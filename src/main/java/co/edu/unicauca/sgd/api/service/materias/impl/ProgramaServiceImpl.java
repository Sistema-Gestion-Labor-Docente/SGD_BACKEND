package co.edu.unicauca.sgd.api.service.materias.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import co.edu.unicauca.sgd.api.domain.Programa;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.ProgramaDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.ProgramaDTOResponse;
import co.edu.unicauca.sgd.api.mapper.ProgramaMapper;
import co.edu.unicauca.sgd.api.repository.ProgramaRepository;
import co.edu.unicauca.sgd.api.service.materias.ProgramaService;
import jakarta.transaction.Transactional;

@Service
public class ProgramaServiceImpl implements ProgramaService {

    private static final Logger logger = LoggerFactory.getLogger(ProgramaServiceImpl.class);

    private ProgramaRepository programaRepository;

    private ProgramaMapper programaMapper;

    public ProgramaServiceImpl(ProgramaRepository programaRepository, ProgramaMapper programaMapper) {
        this.programaRepository = programaRepository;
        this.programaMapper = programaMapper;
    }

    @Override
    public ApiResponse<Page<ProgramaDTOResponse>> obtenerTodos(String nombre, Pageable pageable) {
        try {
            Specification<Programa> spec = Specification.where(null);

            if (StringUtils.hasText(nombre)) {
                spec = spec.and((root, query, cb) ->
                    cb.like(cb.upper(root.get("nombre")), "%" + nombre.toUpperCase() + "%"));
            }

            Page<Programa> page = programaRepository.findAll(spec, pageable);
            Page<ProgramaDTOResponse> response = page.map(programaMapper::toResponse);

            logger.info("Programas encontrados: {}", response.getTotalElements());
            return new ApiResponse<>(200, "Programas recuperados correctamente.", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al listar programas: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<ProgramaDTOResponse> buscarPorId(Integer oid) {
        try {
            Programa entity = programaRepository.findById(oid)
                .orElseThrow(() -> new RuntimeException("Programa no encontrado con ID: " + oid));
            return new ApiResponse<>(200, "Programa encontrado correctamente.", programaMapper.toResponse(entity));
        } catch (RuntimeException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<ProgramaDTOResponse> guardar(ProgramaDTORequest request) {
        try {
            Programa entity = programaMapper.convertToEntity(request);
            entity.setUsuarioCreacion("Usuario"); // igual que Calendario
            Programa saved = programaRepository.save(entity);
            logger.info("Programa guardado ID: {}", saved.getOidPrograma());
            return new ApiResponse<>(200, "Programa guardado correctamente.", programaMapper.toResponse(saved));
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al guardar el programa: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<ProgramaDTOResponse> actualizar(Integer oid, ProgramaDTORequest request) {
        try {
            Programa existente = programaRepository.findById(oid)
                .orElseThrow(() -> new RuntimeException("Programa no encontrado con ID: " + oid));
            programaMapper.actualizarCamposBasicos(existente, request);
            existente.setUsuarioActualizacion("UsuarioActualizacion");
            Programa actualizado = programaRepository.save(existente);
            logger.info("Programa actualizado ID: {}", oid);
            return new ApiResponse<>(200, "Programa actualizado correctamente.", programaMapper.toResponse(actualizado));
        } catch (RuntimeException e) {
            return new ApiResponse<>(400, "Error en la actualización: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno al actualizar: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            if (!programaRepository.existsById(oid)) {
                return new ApiResponse<>(404, "Programa no encontrado con ID: " + oid, null);
            }
            programaRepository.deleteById(oid);
            logger.info("Programa eliminado ID: {}", oid);
            return new ApiResponse<>(200, "Programa eliminado correctamente.", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar el programa: " + e.getMessage(), null);
        }
    }

}
