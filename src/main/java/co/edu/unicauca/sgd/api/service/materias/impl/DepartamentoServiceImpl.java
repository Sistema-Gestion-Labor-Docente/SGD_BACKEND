package co.edu.unicauca.sgd.api.service.materias.impl;

import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.DepartamentoDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.DepartamentoDTOResponse;
import co.edu.unicauca.sgd.api.mapper.DepartamentoMapper;
import co.edu.unicauca.sgd.api.repository.DepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioRepository;
import co.edu.unicauca.sgd.api.service.materias.DepartamentoService;
import jakarta.transaction.Transactional;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DepartamentoServiceImpl implements DepartamentoService {

    private static final Logger logger = LoggerFactory.getLogger(DepartamentoServiceImpl.class);

    private DepartamentoRepository departamentoRepository;

    private UsuarioRepository usuarioRepository;

    private DepartamentoMapper departamentoMapper;

    public DepartamentoServiceImpl(
        @Autowired DepartamentoRepository departamentoRepository,
        @Autowired UsuarioRepository usuarioRepository,
        @Autowired DepartamentoMapper departamentoMapper
    ) {
        this.departamentoRepository = departamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.departamentoMapper = departamentoMapper;
    }

    @Override
    public ApiResponse<Page<DepartamentoDTOResponse>> obtenerTodos(String nombre, Pageable pageable) {
        try {
            Specification<Departamento> spec = Specification.where(null);

            if (StringUtils.hasText(nombre)) {
                spec = spec.and((root, query, cb) ->
                    cb.like(cb.upper(root.get("nombre")), "%" + nombre.toUpperCase() + "%"));
            }

            Page<Departamento> page = departamentoRepository.findAll(spec, pageable);
            Page<DepartamentoDTOResponse> response = page.map(departamentoMapper::toResponse);

            logger.info("Departamentos encontrados: {}", response.getTotalElements());
            return new ApiResponse<>(200, "Departamentos recuperados correctamente.", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al listar departamentos: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<DepartamentoDTOResponse> buscarPorId(Integer oid) {
        try {
            Departamento entity = departamentoRepository.findById(oid)
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado con ID: " + oid));
            return new ApiResponse<>(200, "Departamento encontrado correctamente.", departamentoMapper.toResponse(entity));
        } catch (RuntimeException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<DepartamentoDTOResponse> guardar(DepartamentoDTORequest request) {
        try {
            Departamento entity = departamentoMapper.convertToEntity(request);
            entity.setNombre(request.getNombre().toUpperCase());

            // resolver jefe si viene
            if (request.getJefeOidUsuario() != null) {
                Usuario jefe = usuarioRepository.findById(request.getJefeOidUsuario())
                    .orElseThrow(() -> new RuntimeException("Jefe no encontrado con ID: " + request.getJefeOidUsuario()));
                entity.setJefe(jefe);
            }

            entity.setUsuarioCreacion("Usuario"); // del contexto
            Departamento saved = departamentoRepository.save(entity);
            logger.info("Departamento guardado ID: {}", saved.getOidDepartamento());
            return new ApiResponse<>(200, "Departamento guardado correctamente.", departamentoMapper.toResponse(saved));
        } catch (RuntimeException e) {
            return new ApiResponse<>(400, "Error en datos: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al guardar el departamento: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<DepartamentoDTOResponse> actualizar(Integer oid, DepartamentoDTORequest request) {
        try {
            Departamento existente = departamentoRepository.findById(oid)
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado con ID: " + oid));

            departamentoMapper.actualizarCamposBasicos(existente, request);

            // actualizar jefe: si viene null explícito => quitar jefe
            if (request.getJefeOidUsuario() != null) {
                Usuario jefe = usuarioRepository.findById(request.getJefeOidUsuario())
                    .orElseThrow(() -> new RuntimeException("Jefe no encontrado con ID: " + request.getJefeOidUsuario()));
                existente.setJefe(jefe);
            } else {
                existente.setJefe(null);
            }

            existente.setUsuarioActualizacion("UsuarioActualizacion");
            Departamento actualizado = departamentoRepository.save(existente);
            logger.info("Departamento actualizado ID: {}", oid);
            return new ApiResponse<>(200, "Departamento actualizado correctamente.", departamentoMapper.toResponse(actualizado));
        } catch (RuntimeException e) {
            return new ApiResponse<>(400, "Error en la actualización: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno al actualizar: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            if (!departamentoRepository.existsById(oid)) {
                return new ApiResponse<>(404, "Departamento no encontrado con ID: " + oid, null);
            }
            departamentoRepository.deleteById(oid);
            logger.info("Departamento eliminado ID: {}", oid);
            return new ApiResponse<>(200, "Departamento eliminado correctamente.", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar el departamento: " + e.getMessage(), null);
        }
    }
}
