package co.edu.unicauca.sgd.api.service.materias.impl;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import co.edu.unicauca.sgd.api.domain.UsuarioDepartamento;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.UsuarioDepartamentoDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.HorasLaborDocenteDTO;
import co.edu.unicauca.sgd.api.dto.materias.UsuarioDepartamentoDTOResponse;
import co.edu.unicauca.sgd.api.enums.ContratacionEnum;
import co.edu.unicauca.sgd.api.exception.UsuarioDepartamentoAlreadyExistsException;
import co.edu.unicauca.sgd.api.exception.UsuarioDepartamentoException;
import co.edu.unicauca.sgd.api.exception.UsuarioDepartamentoInternalException;
import co.edu.unicauca.sgd.api.exception.UsuarioDepartamentoNotFoundException;
import co.edu.unicauca.sgd.api.exception.UsuarioDepartamentoValidationException;
import co.edu.unicauca.sgd.api.mapper.UsuarioDepartamentoMapper;
import co.edu.unicauca.sgd.api.repository.CargoActividadRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioActividadCalendarioRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioDepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.projection.UsuarioHorasPorTipoActividadProjection;
import co.edu.unicauca.sgd.api.service.materias.UsuarioDepartamentoService;
import jakarta.transaction.Transactional;

@Service
public class UsuarioDepartamentoServiceImpl implements UsuarioDepartamentoService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioDepartamentoServiceImpl.class);
    private static final String DOCENCIA = "DOCENCIA";
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

    private final UsuarioDepartamentoRepository repository;

    private final UsuarioDepartamentoMapper mapper;

    private final UsuarioActividadCalendarioRepository usuarioActividadCalendarioRepository;

    private final CargoActividadRepository cargoActividadRepository;

    public UsuarioDepartamentoServiceImpl(
            @Autowired UsuarioDepartamentoRepository repository,
            @Autowired UsuarioDepartamentoMapper mapper,
            @Autowired UsuarioActividadCalendarioRepository usuarioActividadCalendarioRepository,
            @Autowired CargoActividadRepository cargoActividadRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.usuarioActividadCalendarioRepository = usuarioActividadCalendarioRepository;
        this.cargoActividadRepository = cargoActividadRepository;
    }

    @Override
    @Transactional
    public ApiResponse<Page<UsuarioDepartamentoDTOResponse>> obtenerTodos(
            Integer oidUsuario,
            Integer oidDepartamento,
            String identificacion,
            String nombreCompleto,
            String correo,
            String contratacion,
            String dedicacion,
            Pageable pageable) {
        try {
            String identificacionFiltro = sanitizeIdentificacion(identificacion);
            String contratacionFiltro = resolveContratacion(contratacion);
            String dedicacionFiltro = resolveDedicacion(dedicacion);
            String nombreFiltro = sanitizeText(nombreCompleto);
            String correoFiltro = sanitizeText(correo);

            Specification<UsuarioDepartamento> spec = (root, query, cb) -> {
                query.distinct(true);
                return cb.conjunction();
            };

            if (oidUsuario != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("oidUsuario"), oidUsuario));
            }
            if (oidDepartamento != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("departamento").get("oidDepartamento"), oidDepartamento));
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
                String contratacionUpper = contratacionFiltro.toUpperCase(Locale.ROOT);
                spec = spec.and((root, query, cb) ->
                        cb.equal(cb.upper(root.join("usuario").join("usuarioDetalle").get("contratacion")), contratacionUpper));
            }
            if (dedicacionFiltro != null) {
                String dedicacionUpper = dedicacionFiltro.toUpperCase(Locale.ROOT);
                spec = spec.and((root, query, cb) ->
                        cb.equal(cb.upper(root.join("usuario").join("usuarioDetalle").get("dedicacion")), dedicacionUpper));
            }

            Page<UsuarioDepartamento> page = repository.findAll(spec, pageable);

            List<UsuarioDepartamento> contenido = page.getContent();
            Map<Integer, HorasLaborDocenteDTO> horasLaborPorUsuario = Map.of();
            if (!contenido.isEmpty()) {
                List<Integer> oidsUsuarios = contenido.stream()
                        .map(UsuarioDepartamento::getOidUsuario)
                        .collect(Collectors.toList());
                List<UsuarioHorasPorTipoActividadProjection> proyecciones =
                        usuarioActividadCalendarioRepository.sumarHorasPorUsuariosYTipoActividad(oidsUsuarios);
                horasLaborPorUsuario = construirHorasLaborPorUsuario(proyecciones);
            }

            Map<Integer, HorasLaborDocenteDTO> horasFinal = horasLaborPorUsuario;
            Page<UsuarioDepartamentoDTOResponse> response = page.map(entidad -> {
                UsuarioDepartamentoDTOResponse dto = mapper.toResponse(entidad);
                HorasLaborDocenteDTO resumen = horasFinal.get(entidad.getOidUsuario());
                if (resumen == null) {
                    resumen = crearResumenHorasVacio();
                }
                dto.setHorasLaborDocente(resumen);
                dto.setTotalHorasActividades(resumen.getTotalHorasAsignadas());
                return dto;
            });

            logger.info("UsuarioDepartamento encontrados: {}", response.getTotalElements());
            boolean hasContent = response.hasContent();
            String message = hasContent ? "Registros recuperados correctamente." : "No se encontraron asignaciones usuario-departamento.";
            return new ApiResponse<>(200, message, response);
        } catch (UsuarioDepartamentoException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioDepartamentoInternalException("Error al listar usuario-departamento.", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<UsuarioDepartamentoDTOResponse> buscarPorUsuario(Integer oidUsuario) {
        try {
            UsuarioDepartamento entity = repository.findById(oidUsuario)
                .orElseThrow(() -> new UsuarioDepartamentoNotFoundException("No existe asignacion para el usuario: " + oidUsuario));
            UsuarioDepartamentoDTOResponse dto = mapper.toResponse(entity);
            List<UsuarioHorasPorTipoActividadProjection> proyecciones =
                    usuarioActividadCalendarioRepository.sumarHorasPorUsuariosYTipoActividad(List.of(oidUsuario));
            Map<Integer, HorasLaborDocenteDTO> horasPorUsuario = construirHorasLaborPorUsuario(proyecciones);
            HorasLaborDocenteDTO resumen = horasPorUsuario.get(oidUsuario);
            if (resumen == null) {
                resumen = crearResumenHorasVacio();
            }
            dto.setHorasLaborDocente(resumen);
            dto.setTotalHorasActividades(resumen.getTotalHorasAsignadas());
            return new ApiResponse<>(200, "Asignacion encontrada correctamente.", dto);
        } catch (UsuarioDepartamentoException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioDepartamentoInternalException("Error interno al buscar la asignacion.", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<UsuarioDepartamentoDTOResponse> guardar(UsuarioDepartamentoDTORequest request) {
        try {
            if (request.getOidUsuario() == null || request.getOidDepartamento() == null) {
                throw new UsuarioDepartamentoValidationException("Los campos oidUsuario y oidDepartamento son obligatorios.");
            }
            if (repository.existsById(request.getOidUsuario())) {
                throw new UsuarioDepartamentoAlreadyExistsException("El usuario ya tiene un departamento asignado.");
            }
            UsuarioDepartamento entity = mapper.convertToEntity(request);
            UsuarioDepartamento saved = repository.save(entity);
            logger.info("Usuario {} asignado a departamento {}", saved.getOidUsuario(),
                    saved.getDepartamento() != null ? saved.getDepartamento().getOidDepartamento() : null);
            return new ApiResponse<>(200, "Asignacion guardada correctamente.", mapper.toResponse(saved));
        } catch (UsuarioDepartamentoException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioDepartamentoInternalException("Error al guardar la asignacion.", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<UsuarioDepartamentoDTOResponse> actualizar(Integer oidUsuario, UsuarioDepartamentoDTORequest request) {
        try {
            if (request.getOidDepartamento() == null) {
                throw new UsuarioDepartamentoValidationException("El campo oidDepartamento es obligatorio para la actualizacion.");
            }

            UsuarioDepartamento existente = repository.findById(oidUsuario)
                .orElseThrow(() -> new UsuarioDepartamentoNotFoundException("No existe asignacion para el usuario: " + oidUsuario));

            request.setOidUsuario(oidUsuario);

            mapper.actualizarCamposBasicos(existente, request);
            UsuarioDepartamento actualizado = repository.save(existente);

            logger.info("Usuario {} reasignado al departamento {}",
                    actualizado.getOidUsuario(),
                    actualizado.getDepartamento() != null ? actualizado.getDepartamento().getOidDepartamento() : null);

            return new ApiResponse<>(200, "Asignacion actualizada correctamente.", mapper.toResponse(actualizado));
        } catch (UsuarioDepartamentoException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioDepartamentoInternalException("Error interno al actualizar la asignacion.", e);
        }
    }

    @Override
    public ApiResponse<Void> eliminar(Integer oidUsuario) {
        try {
            if (!repository.existsById(oidUsuario)) {
                throw new UsuarioDepartamentoNotFoundException("No existe asignacion para el usuario: " + oidUsuario);
            }
            repository.deleteById(oidUsuario);
            logger.info("Asignacion eliminada para el usuario {}", oidUsuario);
            return new ApiResponse<>(200, "Asignacion eliminada correctamente.", null);
        } catch (UsuarioDepartamentoException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioDepartamentoInternalException("Error al eliminar la asignacion.", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<List<UsuarioDepartamentoDTOResponse>> obtenerProfesoresPorTipoActividad(String filtro, Integer oidDepartamento) {
        try {
            if (oidDepartamento == null) {
                throw new UsuarioDepartamentoValidationException("El parámetro oidDepartamento es obligatorio.");
            }
            String filtroNormalizado = filtro == null ? DOCENCIA : filtro.trim().toUpperCase();

            List<UsuarioDepartamento> profesores;
            String mensajeVacio;
            String mensajeExitoso;

            switch (filtroNormalizado) {
                case "DOCENCIA":
                    profesores = repository.findProfesoresConTipoActividad(DOCENCIA, oidDepartamento);
                    mensajeVacio = "No se encontraron profesores con actividades de tipo DOCENCIA.";
                    mensajeExitoso = "Profesores con actividades de tipo DOCENCIA recuperados correctamente.";
                    break;
                case "NO_DOCENCIA":
                    profesores = repository.findProfesoresConTipoActividadDiferente(DOCENCIA, oidDepartamento);
                    mensajeVacio = "No se encontraron profesores con actividades diferentes a DOCENCIA.";
                    mensajeExitoso = "Profesores con actividades diferentes a DOCENCIA recuperados correctamente.";
                    break;
                default:
                    throw new UsuarioDepartamentoValidationException("Filtro inválido. Use DOCENCIA o NO_DOCENCIA.");
            }

            return construirRespuestaProfesores(profesores, mensajeVacio, mensajeExitoso);
        } catch (UsuarioDepartamentoException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioDepartamentoInternalException("Error al listar profesores por tipo de actividad.", e);
        }
    }

    private ApiResponse<List<UsuarioDepartamentoDTOResponse>> construirRespuestaProfesores(
            List<UsuarioDepartamento> profesores,
            String mensajeVacio,
            String mensajeExitoso) {

        List<UsuarioDepartamentoDTOResponse> dtoList = profesores.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        if (!dtoList.isEmpty()) {
            List<Integer> oids = profesores.stream()
                    .map(UsuarioDepartamento::getOidUsuario)
                    .collect(Collectors.toList());
            List<UsuarioHorasPorTipoActividadProjection> proyecciones =
                    usuarioActividadCalendarioRepository.sumarHorasPorUsuariosYTipoActividad(oids);
            Map<Integer, HorasLaborDocenteDTO> horasPorUsuario = construirHorasLaborPorUsuario(proyecciones);

            dtoList.forEach(dto -> {
                Integer oidUsuario = dto.getUsuario() != null ? dto.getUsuario().getOidUsuario() : null;
                if (oidUsuario != null) {
                    HorasLaborDocenteDTO resumen = horasPorUsuario.get(oidUsuario);
                    if (resumen == null) {
                        resumen = crearResumenHorasVacio();
                    }
                    dto.setHorasLaborDocente(resumen);
                    dto.setTotalHorasActividades(resumen.getTotalHorasAsignadas());
                }
            });
        }

        String mensaje = dtoList.isEmpty() ? mensajeVacio : mensajeExitoso;
        return new ApiResponse<>(200, mensaje, dtoList);
    }

    private String sanitizeIdentificacion(String identificacion) {
        if (!StringUtils.hasText(identificacion)) {
            return null;
        }
        String trimmed = identificacion.trim();
        if (!IDENTIFICACION_PATTERN.matcher(trimmed).matches()) {
            throw new UsuarioDepartamentoValidationException(
                    "La identificación debe contener entre 7 y 15 dígitos numéricos.");
        }
        return trimmed;
    }

    private String resolveContratacion(String contratacion) {
        if (!StringUtils.hasText(contratacion)) {
            return null;
        }
        String normalizedInput = normalizeValue(contratacion);
        for (ContratacionEnum tipo : ContratacionEnum.values()) {
            if (normalizedInput.equals(normalizeValue(tipo.name()))
                    || normalizedInput.equals(normalizeValue(tipo.getValor()))) {
                return tipo.getValor();
            }
        }
        throw new UsuarioDepartamentoValidationException(
                "La contratación indicada no es válida. Valores permitidos: " + CONTRATACIONES_PERMITIDAS + ".");
    }

    private String resolveDedicacion(String dedicacion) {
        if (!StringUtils.hasText(dedicacion)) {
            return null;
        }
        String normalized = normalizeValue(dedicacion);
        String canonical = DEDICACIONES_PERMITIDAS.get(normalized);
        if (canonical == null) {
            throw new UsuarioDepartamentoValidationException(
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

    private Map<Integer, HorasLaborDocenteDTO> construirHorasLaborPorUsuario(
            List<UsuarioHorasPorTipoActividadProjection> proyecciones) {
        if (proyecciones == null || proyecciones.isEmpty()) {
            return Map.of();
        }

        Map<Integer, List<UsuarioHorasPorTipoActividadProjection>> porUsuario = proyecciones.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(UsuarioHorasPorTipoActividadProjection::getOidUsuario));

        Set<Integer> tiposIds = proyecciones.stream()
                .map(UsuarioHorasPorTipoActividadProjection::getOidTipoActividad)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));

        Map<Integer, Float> maxPorTipo = calcularMaximoHorasPorTipoActividad(tiposIds);

        Map<Integer, HorasLaborDocenteDTO> resultado = new HashMap<>();

        for (Map.Entry<Integer, List<UsuarioHorasPorTipoActividadProjection>> entry : porUsuario.entrySet()) {
            Integer oidUsuario = entry.getKey();
            List<UsuarioHorasPorTipoActividadProjection> lista = entry.getValue();

            Map<String, Float> asignadasPorGrupo = new HashMap<>();
            Map<String, Float> maxPorGrupo = new HashMap<>();

            for (UsuarioHorasPorTipoActividadProjection p : lista) {
                if (p == null) {
                    continue;
                }
                String nombreTipo = p.getNombreTipoActividad();
                String grupo = agruparTipoActividad(nombreTipo);
                float asignadas = p.getTotalHoras() != null ? p.getTotalHoras() : 0f;
                float maxTipo = maxPorTipo.getOrDefault(p.getOidTipoActividad(), 0f);

                asignadasPorGrupo.merge(grupo, asignadas, Float::sum);
                maxPorGrupo.merge(grupo, maxTipo, Float::sum);
            }

            Map<String, Float> disponiblesPorGrupo = new HashMap<>();
            float totalAsignadas = 0f;

            for (String grupo : asignadasPorGrupo.keySet()) {
                float asignadas = asignadasPorGrupo.getOrDefault(grupo, 0f);
                float max = maxPorGrupo.getOrDefault(grupo, 0f);
                float disponibles = max - asignadas;
                if (disponibles < 0f) {
                    disponibles = 0f;
                }
                disponiblesPorGrupo.put(grupo, disponibles);
                totalAsignadas += asignadas;
            }

            HorasLaborDocenteDTO dto = new HorasLaborDocenteDTO();
            dto.setHorasAsignadasPorTipoActividad(asignadasPorGrupo);
            dto.setHorasDisponiblesPorTipoActividad(disponiblesPorGrupo);
            dto.setTotalHorasAsignadas(totalAsignadas);
            float totalDisponibles = HorasLaborDocenteDTO.HORAS_MAX_SEMANA - totalAsignadas;
            if (totalDisponibles < 0f) {
                totalDisponibles = 0f;
            }
            dto.setTotalHorasDisponibles(totalDisponibles);

            resultado.put(oidUsuario, dto);
        }

        return resultado;
    }

    private Map<Integer, Float> calcularMaximoHorasPorTipoActividad(Set<Integer> tiposIds) {
        if (tiposIds == null || tiposIds.isEmpty()) {
            return Map.of();
        }
        Map<Integer, Float> resultado = new HashMap<>();
        for (Integer oidTipo : tiposIds) {
            if (oidTipo == null) {
                continue;
            }
            Float maximo = cargoActividadRepository.findByTipoActividad_OidTipoActividad(oidTipo).stream()
                    .map(c -> c.getMaxHorasSemana() != null ? c.getMaxHorasSemana() : 0f)
                    .max(Float::compare)
                    .orElse(0f);
            resultado.put(oidTipo, maximo);
        }
        return resultado;
    }

    private String agruparTipoActividad(String nombreTipo) {
        if (nombreTipo == null) {
            return "DESCONOCIDO";
        }
        String normalizado = normalizeValue(nombreTipo);
        boolean esDocencia = normalizado.contains("DOCENCIA");
        boolean esPreparacion = normalizado.contains("PREPARACION");
        if (esDocencia || esPreparacion) {
            return "DOCENCIA";
        }
        return nombreTipo;
    }

    private HorasLaborDocenteDTO crearResumenHorasVacio() {
        HorasLaborDocenteDTO dto = new HorasLaborDocenteDTO();
        dto.setHorasAsignadasPorTipoActividad(Map.of());
        dto.setHorasDisponiblesPorTipoActividad(Map.of());
        dto.setTotalHorasAsignadas(0f);
        dto.setTotalHorasDisponibles(0f);
        return dto;
    }
}
