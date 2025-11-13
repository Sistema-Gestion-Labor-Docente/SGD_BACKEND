package co.edu.unicauca.sgd.api.service.usuario.laborDocente.impl;

import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.Seleccionado;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioDepartamento;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.SeleccionadoDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.SeleccionadoDTOResponse;
import co.edu.unicauca.sgd.api.enums.ContratacionEnum;
import co.edu.unicauca.sgd.api.exception.seleccionado.SeleccionadoException;
import co.edu.unicauca.sgd.api.exception.seleccionado.SeleccionadoNotFoundException;
import co.edu.unicauca.sgd.api.exception.seleccionado.SeleccionadoValidationException;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Subquery;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeleccionadoServiceImpl implements SeleccionadoService {

    private static final Logger logger = LoggerFactory.getLogger(SeleccionadoServiceImpl.class);
    private static final Pattern IDENTIFICACION_PATTERN = Pattern.compile("^\\d{7,15}$");
    private static final Map<String, String> DEDICACIONES_PERMITIDAS = Map.of(
            normalizeValue("MEDIO TIEMPO"), "MEDIO TIEMPO",
            normalizeValue("TIEMPO COMPLETO"), "TIEMPO COMPLETO",
            normalizeValue("HORAS CATEDRA"), "HORAS CATEDRA"
    );
    private static final String CONTRATACIONES_PERMITIDAS =
            Arrays.stream(ContratacionEnum.values())
                    .map(ContratacionEnum::getValor)
                    .collect(Collectors.joining(", "));

    private final SeleccionadoRepository seleccionadoRepository;
    private final SeleccionadoMapper seleccionadoMapper;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioDepartamentoRepository usuarioDepartamentoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final CalendarioRepository calendarioRepository;

    @Override
    public ApiResponse<Page<SeleccionadoDTOResponse>> obtenerTodos(Integer oidCalendario,
                                                                    Integer oidDepartamento,
                                                                    String identificacion,
                                                                    String nombreCompleto,
                                                                    String correo,
                                                                    String contratacion,
                                                                    String dedicacion,
                                                                    Pageable pageable) {
        Pageable pageableToUse = pageable != null ? pageable : Pageable.unpaged();
        try {
            String identificacionFiltro = sanitizeIdentificacion(identificacion);
            ContratacionEnum contratacionFiltro = resolveContratacion(contratacion);
            String dedicacionFiltro = resolveDedicacion(dedicacion);
            String nombreFiltro = sanitizeText(nombreCompleto);
            String correoFiltro = sanitizeText(correo);

            Specification<Seleccionado> spec = (root, query, cb) -> {
                query.distinct(true);
                return cb.conjunction();
            };

            if (oidCalendario != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.join("calendario").get("oidcalendario"), oidCalendario));
            }
            if (oidDepartamento != null) {
                spec = spec.and((root, query, cb) -> {
                    Subquery<Integer> subquery = query.subquery(Integer.class);
                    var udRoot = subquery.from(UsuarioDepartamento.class);
                    subquery.select(udRoot.get("oidUsuario"))
                            .where(
                                    cb.equal(udRoot.get("departamento").get("oidDepartamento"), oidDepartamento),
                                    cb.equal(udRoot.get("oidUsuario"), root.join("usuario").get("oidUsuario"))
                            );
                    return cb.exists(subquery);
                });
            }
            if (identificacionFiltro != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.join("usuario").get("identificacion"), identificacionFiltro));
            }
            if (StringUtils.hasText(nombreFiltro)) {
                String nombreLike = "%" + nombreFiltro.toUpperCase(Locale.ROOT) + "%";
                spec = spec.and((root, query, cb) ->
                        cb.like(
                                cb.upper(cb.concat(cb.concat(root.join("usuario").get("nombres"), " "),
                                        root.join("usuario").get("apellidos"))),
                                nombreLike));
            }
            if (StringUtils.hasText(correoFiltro)) {
                String correoLike = "%" + correoFiltro.toUpperCase(Locale.ROOT) + "%";
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.upper(root.join("usuario").get("correo")), correoLike));
            }
            if (contratacionFiltro != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("tipo"), contratacionFiltro));
            }
            if (dedicacionFiltro != null) {
                String dedicacionUpper = dedicacionFiltro.toUpperCase(Locale.ROOT);
                spec = spec.and((root, query, cb) ->
                        cb.equal(cb.upper(root.get("dedicacion")), dedicacionUpper));
            }

            Page<Seleccionado> page = seleccionadoRepository.findAll(spec, pageableToUse);
            Page<SeleccionadoDTOResponse> pageDto = page.map(seleccionadoMapper::toResponse);

            boolean hasFilters = oidCalendario != null || oidDepartamento != null
                    || identificacionFiltro != null || StringUtils.hasText(nombreFiltro)
                    || StringUtils.hasText(correoFiltro) || contratacionFiltro != null || dedicacionFiltro != null;
            String message = pageDto.hasContent()
                    ? "Seleccionados encontrados correctamente."
                    : (hasFilters ? "No se encontraron seleccionados para los filtros suministrados."
                            : "No se encontraron seleccionados.");

            logger.info("Seleccionados encontrados: {}", pageDto.getTotalElements());
            return new ApiResponse<>(200, message, pageDto);

        } catch (SeleccionadoException e) {
            logger.warn("Error al recuperar seleccionados: {}", e.getMessage());
            return new ApiResponse<>(e.getStatus().value(), e.getMessage(), Page.empty(pageableToUse));
        } catch (Exception e) {
            logger.error("Error al recuperar seleccionados", e);
            return new ApiResponse<>(500, "Error al recuperar los seleccionados: " + e.getMessage(), Page.empty(pageableToUse));
        }
    }

    @Override
    public ApiResponse<SeleccionadoDTOResponse> buscarPorId(Integer oid) {
        try {
            Seleccionado s = seleccionadoRepository.findById(oid)
                    .orElseThrow(() -> new SeleccionadoNotFoundException("Seleccionado no encontrado con ID: " + oid));
            SeleccionadoDTOResponse dto = seleccionadoMapper.toResponse(s);
            logger.info("Seleccionado encontrado con ID: {}", oid);
            return new ApiResponse<>(200, "Seleccionado encontrado correctamente.", dto);
        } catch (SeleccionadoException e) {
            logger.warn("Seleccionado no encontrado: {}", e.getMessage());
            return new ApiResponse<>(e.getStatus().value(), e.getMessage(), null);
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
            Usuario usuario = obtenerUsuario(request.getOidUsuario());
            validarNoDuplicado(request.getOidCalendario(), request.getOidUsuario());
            if (request.getOidDepartamento() != null) {
                validarPerteneceDepartamento(usuario, request.getOidDepartamento());
            }

            // preparar entidad
            Seleccionado entidad = seleccionadoMapper.convertToEntity(request);
            entidad.setUsuario(usuario);
            entidad.setDedicacion(resolverDedicacion(request.getDedicacion(), usuario));
            entidad.setUsuarioCreacion("system"); // TODO: cambiar por usuario autenticado

            Seleccionado guardado = seleccionadoRepository.save(entidad);
            SeleccionadoDTOResponse dto = seleccionadoMapper.toResponse(guardado);

            logger.info("Seleccionado guardado con ID: {}", guardado.getOidSeleccionado());
            return new ApiResponse<>(201, "Seleccionado guardado correctamente.", dto);
        } catch (SeleccionadoException e) {
            logger.warn("Error al guardar seleccionado: {}", e.getMessage());
            return new ApiResponse<>(e.getStatus().value(), e.getMessage(), null);
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
                    .orElseThrow(() -> new SeleccionadoNotFoundException("Seleccionado no encontrado con ID: " + oid));

            // No permitimos cambiar calendario ni usuario por seguridad (si quieres permitirlo, quita estas validaciones)
            if (request.getOidCalendario() != null && !request.getOidCalendario().equals(existente.getCalendario().getOidcalendario())) {
                throw new SeleccionadoValidationException("El calendario no es editable para este recurso.");
            }
            if (request.getOidUsuario() != null && !request.getOidUsuario().equals(existente.getUsuario().getOidUsuario())) {
                throw new SeleccionadoValidationException("El usuario no es editable para este recurso.");
            }

            if (request.getTipo() != null && request.getTipo() != existente.getTipo()) {
                existente.setTipo(request.getTipo());
            }

            if (request.getDedicacion() != null) {
                existente.setDedicacion(request.getDedicacion());
            }

            existente.setUsuarioActualizacion("system"); // TODO: cambiar por usuario autenticado

            Seleccionado actualizado = seleccionadoRepository.save(existente);
            SeleccionadoDTOResponse dto = seleccionadoMapper.toResponse(actualizado);

            logger.info("Seleccionado actualizado con ID: {}", oid);
            return new ApiResponse<>(200, "Seleccionado actualizado correctamente.", dto);
        } catch (SeleccionadoException e) {
            logger.warn("Error en la actualización del seleccionado: {}", e.getMessage());
            return new ApiResponse<>(e.getStatus().value(), e.getMessage(), null);
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
                throw new SeleccionadoNotFoundException("Seleccionado no encontrado con ID: " + oid);
            }
            seleccionadoRepository.deleteById(oid);
            logger.info("Seleccionado eliminado con ID: {}", oid);
            return new ApiResponse<>(204, "Seleccionado eliminado correctamente.", null);
        } catch (SeleccionadoException e) {
            return new ApiResponse<>(e.getStatus().value(), e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Error al eliminar seleccionado: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error al eliminar el seleccionado: " + e.getMessage(), null);
        }
    }

    /* ------------------ Helpers ------------------ */

    private void validarCalendarioExiste(Integer oidCalendario) {
        if (oidCalendario == null) {
            throw new SeleccionadoValidationException("El calendario es obligatorio.");
        }
        if (!calendarioRepository.existsById(oidCalendario)) {
            throw new SeleccionadoNotFoundException("Calendario no encontrado con ID: " + oidCalendario);
        }
    }

    private void validarNoDuplicado(Integer oidCalendario, Integer oidUsuario) {
        if (seleccionadoRepository.existsByCalendarioOidcalendarioAndUsuarioOidUsuario(oidCalendario, oidUsuario)) {
            throw new SeleccionadoValidationException("El usuario ya está seleccionado para ese calendario.");
        }
    }

    private void validarPerteneceDepartamento(Usuario usuario, Integer oidDepartamento) {
        boolean pertenece = usuarioDepartamentoRepository.existsByUsuarioOidUsuarioAndDepartamentoOidDepartamento(
                usuario.getOidUsuario(), oidDepartamento);
        
        if (pertenece) {
            return;
        }

        Departamento departamento = departamentoRepository.findById(oidDepartamento)
                .orElseThrow(() -> new SeleccionadoNotFoundException("Departamento no encontrado con ID: " + oidDepartamento));

        // Crear y guardar la relación correctamente
        UsuarioDepartamento ud = new UsuarioDepartamento();
        ud.setUsuario(usuario);            // @MapsId copiará el id en oidUsuario
        ud.setDepartamento(departamento);
        usuarioDepartamentoRepository.save(ud);
    }

    private Usuario obtenerUsuario(Integer oidUsuario) {
        if (oidUsuario == null) {
            throw new SeleccionadoValidationException("El usuario es obligatorio.");
        }
        return usuarioRepository.findById(oidUsuario)
                .orElseThrow(() -> new SeleccionadoNotFoundException("Usuario no encontrado con ID: " + oidUsuario));
    }

    private String resolverDedicacion(String dedicacionSolicitud, Usuario usuario) {
        if (StringUtils.hasText(dedicacionSolicitud)) {
            return dedicacionSolicitud;
        }
        return obtenerDedicacionDesdeUsuario(usuario);
    }

    private String obtenerDedicacionDesdeUsuario(Usuario usuario) {
        if (usuario == null || usuario.getUsuarioDetalle() == null) {
            return null;
        }
        return usuario.getUsuarioDetalle().getDedicacion();
    }

    private String sanitizeIdentificacion(String identificacion) {
        if (!StringUtils.hasText(identificacion)) {
            return null;
        }
        String trimmed = identificacion.trim();
        if (!IDENTIFICACION_PATTERN.matcher(trimmed).matches()) {
            throw new SeleccionadoValidationException("La identificación debe contener entre 7 y 15 dígitos numéricos.");
        }
        return trimmed;
    }

    private ContratacionEnum resolveContratacion(String contratacion) {
        if (!StringUtils.hasText(contratacion)) {
            return null;
        }
        String normalized = normalizeValue(contratacion);
        for (ContratacionEnum tipo : ContratacionEnum.values()) {
            if (normalized.equals(normalizeValue(tipo.name()))
                    || normalized.equals(normalizeValue(tipo.getValor()))) {
                return tipo;
            }
        }
        throw new SeleccionadoValidationException(
                "La contratación indicada no es válida. Valores permitidos: " + CONTRATACIONES_PERMITIDAS + ".");
    }

    private String resolveDedicacion(String dedicacion) {
        if (!StringUtils.hasText(dedicacion)) {
            return null;
        }
        String normalized = normalizeValue(dedicacion);
        String canonical = DEDICACIONES_PERMITIDAS.get(normalized);
        if (canonical == null) {
            throw new SeleccionadoValidationException(
                    "La dedicación indicada no es válida. Valores permitidos: MEDIO TIEMPO, TIEMPO COMPLETO, HORAS CATEDRA.");
        }
        return canonical;
    }

    private String sanitizeText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private static String normalizeValue(String value) {
        if (value == null) {
            return null;
        }
        String upper = value.trim().toUpperCase(Locale.ROOT);
        return Normalizer.normalize(upper, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    }
}
