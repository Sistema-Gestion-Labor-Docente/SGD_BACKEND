package co.edu.unicauca.sgd.api.service.usuario.laborDocente.impl;

import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.Seleccionado;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioDepartamento;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.SeleccionadoDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.SeleccionadoDTOResponse;
import co.edu.unicauca.sgd.api.mapper.SeleccionadoMapper;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.DepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.SeleccionadoRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioDepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioRepository;
import co.edu.unicauca.sgd.api.service.usuario.laborDocente.SeleccionadoService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeleccionadoServiceImpl implements SeleccionadoService {

    private static final Logger logger = LoggerFactory.getLogger(SeleccionadoServiceImpl.class);

    private final SeleccionadoRepository seleccionadoRepository;
    private final SeleccionadoMapper seleccionadoMapper;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioDepartamentoRepository usuarioDepartamentoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final CalendarioRepository calendarioRepository;

    @Override
    public ApiResponse<Page<SeleccionadoDTOResponse>> obtenerTodos(Integer oidCalendario,
                                                                  Integer oidDepartamento,
                                                                  Pageable pageable) {
        try {
            // Caso: filtrar por calendario y departamento (JOIN con UsuarioDepartamento)
            if (oidCalendario != null && oidDepartamento != null) {
                List<Seleccionado> list = seleccionadoRepository.findByCalendarioAndDepartamento(oidCalendario, oidDepartamento);
                List<SeleccionadoDTOResponse> dtos = list.stream().map(seleccionadoMapper::toResponse).collect(Collectors.toList());
                Page<SeleccionadoDTOResponse> page = listToPage(dtos, pageable);
                logger.info("Seleccionados encontrados para calendario={}, departamento={}: {}", oidCalendario, oidDepartamento, page.getTotalElements());
                return new ApiResponse<>(200, "Seleccionados encontrados correctamente.", page);
            }

            // Caso: filtrar por calendario
            if (oidCalendario != null) {
                List<Seleccionado> list = seleccionadoRepository.findByCalendarioOidcalendario(oidCalendario);
                List<SeleccionadoDTOResponse> dtos = list.stream().map(seleccionadoMapper::toResponse).collect(Collectors.toList());
                Page<SeleccionadoDTOResponse> page = listToPage(dtos, pageable);
                logger.info("Seleccionados encontrados para calendario={}: {}", oidCalendario, page.getTotalElements());
                return new ApiResponse<>(200, "Seleccionados encontrados correctamente.", page);
            }

            // Caso: sin filtros -> paginación normal usando repository
            Page<Seleccionado> pageEnt = seleccionadoRepository.findAll(pageable);
            Page<SeleccionadoDTOResponse> pageDto = pageEnt.map(seleccionadoMapper::toResponse);
            logger.info("Seleccionados encontrados (todos): {}", pageDto.getTotalElements());
            return new ApiResponse<>(200, "Seleccionados encontrados correctamente.", pageDto);

        } catch (Exception e) {
            logger.error("Error al recuperar seleccionados: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error al recuperar los seleccionados: " + e.getMessage(), Page.empty());
        }
    }

    @Override
    public ApiResponse<SeleccionadoDTOResponse> buscarPorId(Integer oid) {
        try {
            Seleccionado s = seleccionadoRepository.findById(oid)
                    .orElseThrow(() -> new RuntimeException("Seleccionado no encontrado con ID: " + oid));
            SeleccionadoDTOResponse dto = seleccionadoMapper.toResponse(s);
            logger.info("Seleccionado encontrado con ID: {}", oid);
            return new ApiResponse<>(200, "Seleccionado encontrado correctamente.", dto);
        } catch (RuntimeException e) {
            logger.warn("Seleccionado no encontrado: {}", e.getMessage());
            return new ApiResponse<>(404, "Seleccionado no encontrado: " + e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Error interno al recuperar seleccionado: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error interno al recuperar el seleccionado: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<SeleccionadoDTOResponse> guardar(SeleccionadoDTORequest request) {
        try {
            validarCalendarioExiste(request.getOidCalendario());
            validarUsuarioExiste(request.getOidUsuario());
            validarNoDuplicado(request.getOidCalendario(), request.getOidUsuario());
            if (request.getOidDepartamento() != null) {
                validarPerteneceDepartamento(request.getOidUsuario(), request.getOidDepartamento());
            }

            // preparar entidad
            Seleccionado entidad = seleccionadoMapper.convertToEntity(request);

            entidad.setUsuarioCreacion("system"); // TODO: cambiar por usuario autenticado

            Seleccionado guardado = seleccionadoRepository.save(entidad);
            SeleccionadoDTOResponse dto = seleccionadoMapper.toResponse(guardado);

            logger.info("Seleccionado guardado con ID: {}", guardado.getOidSeleccionado());
            return new ApiResponse<>(201, "Seleccionado guardado correctamente.", dto);
        } catch (RuntimeException e) {
            logger.warn("Error de validación al guardar seleccionado: {}", e.getMessage());
            return new ApiResponse<>(400, "Error al guardar seleccionado: " + e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Error interno al guardar seleccionado: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error al guardar el seleccionado: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<SeleccionadoDTOResponse> actualizar(Integer oid, SeleccionadoDTORequest request) {
        try {
            Seleccionado existente = seleccionadoRepository.findById(oid)
                    .orElseThrow(() -> new RuntimeException("Seleccionado no encontrado con ID: " + oid));

            // No permitimos cambiar calendario ni usuario por seguridad (si quieres permitirlo, quita estas validaciones)
            if (request.getOidCalendario() != null && !request.getOidCalendario().equals(existente.getCalendario().getOidcalendario())) {
                throw new RuntimeException("El calendario no es editable para este recurso.");
            }
            if (request.getOidUsuario() != null && !request.getOidUsuario().equals(existente.getUsuario().getOidUsuario())) {
                throw new RuntimeException("El usuario no es editable para este recurso.");
            }

            if (request.getTipo() != null && request.getTipo() != existente.getTipo()) {
                existente.setTipo(request.getTipo());
            }

            existente.setUsuarioActualizacion("system"); // TODO: cambiar por usuario autenticado

            Seleccionado actualizado = seleccionadoRepository.save(existente);
            SeleccionadoDTOResponse dto = seleccionadoMapper.toResponse(actualizado);

            logger.info("Seleccionado actualizado con ID: {}", oid);
            return new ApiResponse<>(200, "Seleccionado actualizado correctamente.", dto);
        } catch (RuntimeException e) {
            logger.warn("Error en la actualización del seleccionado: {}", e.getMessage());
            return new ApiResponse<>(400, "Error en la actualización: " + e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Error interno al actualizar seleccionado: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error interno al actualizar el seleccionado: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            if (!seleccionadoRepository.existsById(oid)) {
                return new ApiResponse<>(404, "Seleccionado no encontrado con ID: " + oid, null);
            }
            seleccionadoRepository.deleteById(oid);
            logger.info("Seleccionado eliminado con ID: {}", oid);
            return new ApiResponse<>(204, "Seleccionado eliminado correctamente.", null);
        } catch (Exception e) {
            logger.error("Error al eliminar seleccionado: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error al eliminar el seleccionado: " + e.getMessage(), null);
        }
    }

    /* ------------------ Helpers ------------------ */

    private void validarUsuarioExiste(Integer oidUsuario) {
        if (oidUsuario == null || !usuarioRepository.existsById(oidUsuario)) {
            throw new RuntimeException("Usuario no existe con ID: " + oidUsuario);
        }
    }

    private void validarCalendarioExiste(Integer oidCalendario) {
        if (oidCalendario == null || !calendarioRepository.existsById(oidCalendario)) {
            throw new RuntimeException("Calendario no existe con ID: " + oidCalendario);
        }
    }

    private void validarNoDuplicado(Integer oidCalendario, Integer oidUsuario) {
        if (seleccionadoRepository.existsByCalendarioOidcalendarioAndUsuarioOidUsuario(oidCalendario, oidUsuario)) {
            throw new RuntimeException("El usuario ya está seleccionado para ese calendario.");
        }
    }

    private Page<SeleccionadoDTOResponse> listToPage(List<SeleccionadoDTOResponse> list, Pageable pageable) {
        if (pageable == null) {
            return new PageImpl<>(list);
        }
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        List<SeleccionadoDTOResponse> content = (start <= end) ? list.subList(start, end) : List.of();
        return new PageImpl<>(content, pageable, list.size());
    }

    private void validarPerteneceDepartamento(Integer oidUsuario, Integer oidDepartamento) {
        boolean pertenece = usuarioDepartamentoRepository.existsByUsuarioOidUsuarioAndDepartamentoOidDepartamento(oidUsuario, oidDepartamento);
        
        if (pertenece) {
            return;
        }

        // Cargar usuario y departamento — si no existen, lanzar (evita crear registros incompletos)
        Usuario usuario = usuarioRepository.findById(oidUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no existe con ID: " + oidUsuario));

        Departamento departamento = departamentoRepository.findById(oidDepartamento)
                .orElseThrow(() -> new RuntimeException("Departamento no existe con ID: " + oidDepartamento));

        // Crear y guardar la relación correctamente
        UsuarioDepartamento ud = new UsuarioDepartamento();
        ud.setUsuario(usuario);            // @MapsId copiará el id en oidUsuario
        ud.setDepartamento(departamento);
        usuarioDepartamentoRepository.save(ud);
    }
}
