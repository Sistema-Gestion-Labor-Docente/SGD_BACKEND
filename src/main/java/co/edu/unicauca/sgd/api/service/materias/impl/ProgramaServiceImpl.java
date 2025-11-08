package co.edu.unicauca.sgd.api.service.materias.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import co.edu.unicauca.sgd.api.domain.Programa;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.ProgramaDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.ProgramaDTOResponse;
import co.edu.unicauca.sgd.api.mapper.ProgramaMapper;
import co.edu.unicauca.sgd.api.repository.ProgramaRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioRepository;
import co.edu.unicauca.sgd.api.service.materias.ProgramaService;
import jakarta.transaction.Transactional;

@Service
public class ProgramaServiceImpl implements ProgramaService {

    private static final Logger logger = LoggerFactory.getLogger(ProgramaServiceImpl.class);

    private ProgramaRepository programaRepository;

    private UsuarioRepository usuarioRepository;

    private ProgramaMapper programaMapper;

    public ProgramaServiceImpl(ProgramaRepository programaRepository, UsuarioRepository usuarioRepository,
            ProgramaMapper programaMapper) {
        this.programaRepository = programaRepository;
        this.usuarioRepository = usuarioRepository;
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
            String message = response.hasContent()
                    ? "Programas recuperados correctamente."
                    : "No se encontraron programas.";
            return new ApiResponse<>(200, message, response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al listar programas: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<ProgramaDTOResponse> buscarPorId(Integer oid) {
        try {
            Programa entity = programaRepository.findById(oid)
                .orElseThrow(() -> new IllegalStateException("Programa no encontrado con ID: " + oid));
            return new ApiResponse<>(200, "Programa encontrado correctamente.", programaMapper.toResponse(entity));
        } catch (IllegalStateException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno al buscar el programa: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<ProgramaDTOResponse> guardar(ProgramaDTORequest request) {
        try {
            if (!StringUtils.hasText(request.getNombre())) {
                throw new IllegalArgumentException("El nombre del programa es obligatorio.");
            }
            Programa entidad = programaMapper.convertToEntity(request);

            if (request.getCoordinadorOidUsuario() != null) {
                Usuario coord = usuarioRepository.findById(request.getCoordinadorOidUsuario())
                    .orElseThrow(() -> new IllegalStateException("Coordinador no encontrado con ID: " + request.getCoordinadorOidUsuario()));
                entidad.setCoordinador(coord);
            }

            entidad.setUsuarioCreacion("Usuario");
            Programa saved = programaRepository.save(entidad);
            return new ApiResponse<>(200, "Programa guardado.", programaMapper.toResponse(saved));
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (IllegalStateException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al guardar programa: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<ProgramaDTOResponse> actualizar(Integer id, ProgramaDTORequest request) {
        try {
            Programa existente = programaRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Programa no encontrado con ID: " + id));

            programaMapper.actualizarCamposBasicos(existente, request);

            if (request.getCoordinadorOidUsuario() != null) {
                Usuario coord = usuarioRepository.findById(request.getCoordinadorOidUsuario())
                    .orElseThrow(() -> new IllegalStateException("Coordinador no encontrado con ID: " + request.getCoordinadorOidUsuario()));
                existente.setCoordinador(coord);
            } else {
                existente.setCoordinador(null);
            }

            existente.setUsuarioActualizacion("UsuarioActualizacion");
            Programa actualizado = programaRepository.save(existente);
            return new ApiResponse<>(200, "Programa actualizado.", programaMapper.toResponse(actualizado));
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (IllegalStateException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al actualizar programa: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            if (!programaRepository.existsById(oid)) {
                throw new IllegalStateException("Programa no encontrado con ID: " + oid);
            }
            programaRepository.deleteById(oid);
            logger.info("Programa eliminado ID: {}", oid);
            return new ApiResponse<>(200, "Programa eliminado correctamente.", null);
        } catch (IllegalStateException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar el programa: " + e.getMessage(), null);
        }
    }

}
