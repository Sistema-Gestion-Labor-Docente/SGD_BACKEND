package co.edu.unicauca.sgd.api.service.necesidad.impl;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.Asignacion;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.EavAtributo;
import co.edu.unicauca.sgd.api.domain.Materia;
import co.edu.unicauca.sgd.api.domain.Necesidad;
import co.edu.unicauca.sgd.api.domain.Seleccionado;
import co.edu.unicauca.sgd.api.domain.TipoActividad;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.EstadoActividad;
import co.edu.unicauca.sgd.api.domain.UsuarioDetalle;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.AtributoDTO;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sgd.api.dto.necesidades.AsignacionDTORequest;
import co.edu.unicauca.sgd.api.dto.necesidades.AsignacionDTOResponse;
import co.edu.unicauca.sgd.api.enums.ContratacionEnum;
import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;
import co.edu.unicauca.sgd.api.exception.ValidacionNegocioException;
import co.edu.unicauca.sgd.api.exception.RecursoNoEncontradoException;
import co.edu.unicauca.sgd.api.exception.asignacion.AsignacionCalendarioInvalidoException;
import co.edu.unicauca.sgd.api.exception.asignacion.AsignacionDocenteDuplicadoException;
import co.edu.unicauca.sgd.api.exception.asignacion.AsignacionLimiteDocentesException;
import co.edu.unicauca.sgd.api.exception.asignacion.AsignacionNoEncontradaException;
import co.edu.unicauca.sgd.api.exception.asignacion.AsignacionTipoActividadNoConfiguradaException;
import co.edu.unicauca.sgd.api.exception.asignacion.AsignacionOperacionNoPermitidaException;
import co.edu.unicauca.sgd.api.mapper.AsignacionMapper;
import co.edu.unicauca.sgd.api.repository.ActividadRepository;
import co.edu.unicauca.sgd.api.repository.AsignacionRepository;
import co.edu.unicauca.sgd.api.repository.EstadoActividadRepository;
import co.edu.unicauca.sgd.api.repository.EavAtributoRepository;
import co.edu.unicauca.sgd.api.repository.NecesidadRepository;
import co.edu.unicauca.sgd.api.repository.SeleccionadoRepository;
import co.edu.unicauca.sgd.api.repository.TipoActividadRepository;
import co.edu.unicauca.sgd.api.service.EavAtributoService;
import co.edu.unicauca.sgd.api.service.necesidad.AsignacionService;

@Service
public class AsignacionServiceImpl implements AsignacionService {

    private static final float PREPARACION_FACTOR = 2.5f;
    private static final String DOCENCIA_DIRECTA = "DOCENCIA";
    private static final float HORAS_MAX_PLANTA_TIEMPO_COMPLETO = 14f;
    private static final float HORAS_MAX_PLANTA_MEDIO_TIEMPO = 8f;
    private static final float HORAS_MAX_OCASIONAL_TIEMPO_COMPLETO = 16f;
    private static final float HORAS_MAX_OCASIONAL_MEDIO_TIEMPO = 12f;
    private static final float HORAS_MAX_CATEDRA_O_BECARIO = 12f;
    private static final float EPSILON = 0.0001f;
    private static final int SEMESTRE_MIN = 1;
    private static final int SEMESTRE_MAX = 10;

    private final AsignacionRepository asignacionRepository;
    private final NecesidadRepository necesidadRepository;
    private final SeleccionadoRepository seleccionadoRepository;
    private final TipoActividadRepository tipoActividadRepository;
    private final EstadoActividadRepository estadoActividadRepository;
    private final ActividadRepository actividadRepository;
    private final EavAtributoService eavAtributoService;
    private final EavAtributoRepository eavAtributoRepository;
    private final AsignacionMapper asignacionMapper;

    public AsignacionServiceImpl(AsignacionRepository asignacionRepository,
                                 NecesidadRepository necesidadRepository,
                                 SeleccionadoRepository seleccionadoRepository,
                                 TipoActividadRepository tipoActividadRepository,
                                 EstadoActividadRepository estadoActividadRepository,
                                 ActividadRepository actividadRepository,
                                 EavAtributoService eavAtributoService,
                                 EavAtributoRepository eavAtributoRepository,
                                 AsignacionMapper asignacionMapper) {
        this.asignacionRepository = asignacionRepository;
        this.necesidadRepository = necesidadRepository;
        this.seleccionadoRepository = seleccionadoRepository;
        this.tipoActividadRepository = tipoActividadRepository;
        this.estadoActividadRepository = estadoActividadRepository;
        this.actividadRepository = actividadRepository;
        this.eavAtributoService = eavAtributoService;
        this.eavAtributoRepository = eavAtributoRepository;
        this.asignacionMapper = asignacionMapper;
    }

    @Override
    public ApiResponse<Page<AsignacionDTOResponse>> listar(Integer oidCalendario,
                                                           Integer oidDepartamento,
                                                           Integer oidNecesidad,
                                                           Integer oidSeleccionado,
                                                           String nombreMateria,
                                                           Integer semestreMateria,
                                                           String codigoMateria,
                                                           Pageable pageable) {
        Pageable pageableToUse = pageable != null ? pageable : Pageable.unpaged();

        if (oidCalendario == null || oidDepartamento == null) {
            return new ApiResponse<>(400, "El calendario y el departamento son obligatorios.", Page.empty(pageableToUse));
        }
        if (semestreMateria != null
                && (semestreMateria < SEMESTRE_MIN || semestreMateria > SEMESTRE_MAX)) {
            return new ApiResponse<>(400,
                    String.format("El semestre de la materia debe estar entre %d y %d.", SEMESTRE_MIN, SEMESTRE_MAX),
                    Page.empty(pageableToUse));
        }

        Specification<Asignacion> specification = Specification.where((root, query, cb) ->
                cb.equal(root.join("necesidad").join("calendario").get("oidcalendario"), oidCalendario));

        specification = specification.and((root, query, cb) ->
                cb.equal(root.join("necesidad").join("materia").join("departamento").get("oidDepartamento"), oidDepartamento));

        if (oidNecesidad != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.join("necesidad").get("oidNecesidad"), oidNecesidad));
        }

        if (oidSeleccionado != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.join("seleccionado").get("oidSeleccionado"), oidSeleccionado));
        }
        if (StringUtils.hasText(nombreMateria)) {
            String likeNombre = "%" + nombreMateria.trim().toUpperCase() + "%";
            specification = specification.and((root, query, cb) ->
                    cb.like(cb.upper(root.join("necesidad").join("materia").get("nombre")), likeNombre));
        }
        if (semestreMateria != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.join("necesidad").join("materia").get("semestre"), semestreMateria));
        }
        if (StringUtils.hasText(codigoMateria)) {
            String likeCodigo = "%" + codigoMateria.trim().toUpperCase() + "%";
            specification = specification.and((root, query, cb) ->
                    cb.like(cb.upper(root.join("necesidad").join("materia").get("codigo")), likeCodigo));
        }

        Page<Asignacion> asignaciones = asignacionRepository.findAll(specification, pageableToUse);

        if (!asignaciones.hasContent()) {
            return new ApiResponse<>(204, "No se encontraron asignaciones.", Page.empty(pageableToUse));
        }

        Page<AsignacionDTOResponse> page = asignaciones.map(asignacionMapper::toResponse);
        return new ApiResponse<>(200, "Asignaciones recuperadas correctamente.", page);
    }

    @Override
    public ApiResponse<AsignacionDTOResponse> buscarPorId(Integer oidAsignacion) {
        return asignacionRepository.findById(oidAsignacion)
                .map(asignacion -> new ApiResponse<>(200, "Asignación encontrada correctamente.", asignacionMapper.toResponse(asignacion)))
                .orElseThrow(() -> new AsignacionNoEncontradaException(oidAsignacion));
    }

    @Override
    @Transactional
    public ApiResponse<AsignacionDTOResponse> crear(AsignacionDTORequest request) {
        try {
            Asignacion asignacion = new Asignacion();
            prepararAsignacion(asignacion, request, true);
            asignacion = asignacionRepository.save(asignacion);
            redistribuirHoras(asignacion.getNecesidad());
            validarHorasMaximasPorContratacion(asignacion.getSeleccionado());
            actualizarEstadoNecesidad(asignacion.getNecesidad());
            return new ApiResponse<>(201, "Asignación creada correctamente.", asignacionMapper.toResponse(asignacion));
        } catch (DataIntegrityViolationException e) {
            throw new AsignacionDocenteDuplicadoException();
        }
    }

    @Override
    @Transactional
    public ApiResponse<AsignacionDTOResponse> actualizar(Integer oidAsignacion, AsignacionDTORequest request) {
        Asignacion asignacion = asignacionRepository.findById(oidAsignacion)
                .orElseThrow(() -> new AsignacionNoEncontradaException(oidAsignacion));

        if (!Objects.equals(asignacion.getNecesidad().getOidNecesidad(), request.getOidNecesidad())
                || !Objects.equals(asignacion.getSeleccionado().getOidSeleccionado(), request.getOidSeleccionado())) {
            throw new AsignacionOperacionNoPermitidaException();
        }

        Integer necesidadOriginal = asignacion.getNecesidad().getOidNecesidad();
        prepararAsignacion(asignacion, request, false);
        asignacion = asignacionRepository.save(asignacion);

        redistribuirHoras(asignacion.getNecesidad());
        validarHorasMaximasPorContratacion(asignacion.getSeleccionado());
        if (!Objects.equals(necesidadOriginal, asignacion.getNecesidad().getOidNecesidad())) {
            necesidadRepository.findById(necesidadOriginal)
                    .ifPresent(this::redistribuirHoras);
        }

        return new ApiResponse<>(200, "Asignación actualizada correctamente.", asignacionMapper.toResponse(asignacion));
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminar(Integer oidAsignacion) {
        Asignacion asignacion = asignacionRepository.findById(oidAsignacion)
                .orElseThrow(() -> new AsignacionNoEncontradaException(oidAsignacion));

        Necesidad necesidad = asignacion.getNecesidad();
        eavAtributoService.eliminarAtributosActividad(asignacion.getActividad());
        asignacionRepository.delete(asignacion);
        redistribuirHoras(necesidad);
        actualizarEstadoNecesidad(necesidad);

        return new ApiResponse<>(204, "Asignación eliminada correctamente.", null);
    }

    @Override
    @Transactional(readOnly = true)
    public void validarSeleccionadoCalendario(Integer oidNecesidad, Integer oidSeleccionado) {
        if (oidNecesidad == null || oidSeleccionado == null) {
            throw new ValidacionNegocioException("Debe proporcionar el identificador de la necesidad y del seleccionado para validar los calendarios.");
        }
        Necesidad necesidad = necesidadRepository.findById(oidNecesidad)
                .orElseThrow(() -> new RecursoNoEncontradoException("La necesidad indicada no existe."));
        Seleccionado seleccionado = seleccionadoRepository.findById(oidSeleccionado)
                .orElseThrow(() -> new RecursoNoEncontradoException("El seleccionado indicado no existe."));
        validarCalendarioCompartido(necesidad, seleccionado);
    }

    private void prepararAsignacion(Asignacion asignacion, AsignacionDTORequest request, boolean esNuevaAsignacion) {
        Necesidad necesidad = necesidadRepository.findById(request.getOidNecesidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("La necesidad indicada no existe."));

        Seleccionado seleccionado = seleccionadoRepository.findById(request.getOidSeleccionado())
                .orElseThrow(() -> new RecursoNoEncontradoException("El seleccionado indicado no existe."));

        validarCalendarioCompartido(necesidad, seleccionado);

        validarRequisitosCalendarioPorContratacion(seleccionado, necesidad.getCalendario());

        long totalAsignados = asignacionRepository.countByNecesidad_OidNecesidad(necesidad.getOidNecesidad());
        if (esNuevaAsignacion && totalAsignados >= 3) {
            throw new AsignacionLimiteDocentesException();
        }

        if (esNuevaAsignacion && asignacionRepository
                .findByNecesidad_OidNecesidadAndSeleccionado_OidSeleccionado(necesidad.getOidNecesidad(), seleccionado.getOidSeleccionado())
                .isPresent()) {
            throw new AsignacionDocenteDuplicadoException();
        }

        TipoActividad tipoActividad = tipoActividadRepository.findByNombreIgnoreCase(DOCENCIA_DIRECTA)
                .orElseThrow(AsignacionTipoActividadNoConfiguradaException::new);

        EstadoActividad estadoActividad = estadoActividadRepository.findById(1)
                .orElseThrow(() -> new RecursoNoEncontradoException("El estado de actividad indicado no existe."));

        Actividad actividad = asignacion.getActividad();
        if (actividad == null) {
            actividad = new Actividad();
        }
        String nombreMateria = necesidad.getMateria() != null ? necesidad.getMateria().getNombre() : null;
        actividad.setTipoActividad(tipoActividad);
        actividad.setEstadoActividad(estadoActividad);
        actividad.setNombreActividad(nombreMateria);

        actividad = actividadRepository.save(actividad);

        asignacion.setActividad(actividad);
        asignacion.setNecesidad(necesidad);
        asignacion.setSeleccionado(seleccionado);

        sincronizarAtributosActividadDocencia(actividad, necesidad);
    }

    private void sincronizarAtributosActividadDocencia(Actividad actividad, Necesidad necesidad) {
        if (actividad == null || necesidad == null) {
            return;
        }

        Materia materia = necesidad.getMateria();
        if (materia == null || materia.getPlan() == null || materia.getPlan().getPrograma() == null) {
            return;
        }

        String codigo = materia.getCodigo();
        String grupo = necesidad.getGrupo();
        String nombreMateria = materia.getNombre();
        String nombrePrograma = materia.getPlan().getPrograma().getNombre();
        Integer semestre = materia.getSemestre();

        Map<String, EavAtributo> cacheAtributos = eavAtributoRepository.findAll().stream()
                .collect(Collectors.toMap(EavAtributo::getNombre, a -> a));

        ActividadBaseDTO actividadDTO = new ActividadBaseDTO();
        actividadDTO.setOidActividad(actividad.getOidActividad());
        actividadDTO.setTipoActividad(actividad.getTipoActividad());
        actividadDTO.setOidEstadoActividad(
                actividad.getEstadoActividad() != null ? actividad.getEstadoActividad().getOidEstadoActividad() : null);
        actividadDTO.setNombreActividad(actividad.getNombreActividad());
        actividadDTO.setSemanas(actividad.getSemanas());

        List<AtributoDTO> atributos = List.of(
                new AtributoDTO("CODIGO", codigo != null ? codigo : ""),
                new AtributoDTO("GRUPO", grupo != null ? grupo : ""),
                new AtributoDTO("MATERIA", nombreMateria != null ? nombreMateria : ""),
                new AtributoDTO("PROGRAMA", nombrePrograma != null ? nombrePrograma : ""),
                new AtributoDTO("SEMESTRE", semestre != null ? semestre.toString() : "")
        );

        actividadDTO.setAtributos(atributos);

        eavAtributoService.actualizarAtributosDinamicos(actividadDTO, actividad, cacheAtributos);
    }

    private void validarRequisitosCalendarioPorContratacion(Seleccionado seleccionado, Calendario calendario) {
        if (calendario == null) {
            throw new ValidacionNegocioException("El calendario asociado a la necesidad no existe.");
        }
        ContratacionEnum tipo = seleccionado.getTipo();
        if (tipo == null) {
            throw new ValidacionNegocioException("El docente seleccionado no tiene tipo de contratación configurado.");
        }
        if (calendario.getSemanasClase() == null) {
            throw new ValidacionNegocioException(String.format(
                    "El calendario %s no tiene configuradas las semanas de clase requeridas para la contratación %s.",
                    calendario.getOidcalendario(), tipo.getValor()));
        }
        if (requiereSemanasPreparacion(tipo) && calendario.getSemanasPreparacion() == null) {
            throw new ValidacionNegocioException(String.format(
                    "El calendario %s no tiene configuradas las semanas de preparación requeridas para la contratación %s.",
                    calendario.getOidcalendario(), tipo.getValor()));
        }
    }

    private boolean requiereSemanasPreparacion(ContratacionEnum tipo) {
        return tipo == ContratacionEnum.PLANTA
                || tipo == ContratacionEnum.OCASIONAL;
    }

    private boolean esContratacionSinPreparacion(ContratacionEnum tipo) {
        return tipo == ContratacionEnum.CATEDRA
                || tipo == ContratacionEnum.BECARIOS_Y_PRACTICANTES
                || tipo == ContratacionEnum.BECARIOS_POSTGRADO;
    }

    private void validarHorasMaximasPorContratacion(Seleccionado seleccionado) {
        if (seleccionado == null || seleccionado.getOidSeleccionado() == null) {
            return;
        }
        ContratacionEnum tipo = seleccionado.getTipo();
        if (tipo == null) {
            throw new ValidacionNegocioException("El docente seleccionado no tiene tipo de contratación configurado.");
        }
        String dedicacion = obtenerDedicacion(seleccionado);
        float limite = obtenerLimiteHorasDocencia(tipo, dedicacion);
        if (limite <= 0f) {
            return;
        }
        List<Asignacion> asignacionesDocente = asignacionRepository.findBySeleccionado_OidSeleccionado(seleccionado.getOidSeleccionado());
        if (asignacionesDocente == null || asignacionesDocente.isEmpty()) {
            return;
        }
        float totalHoras = asignacionesDocente.stream()
                .map(Asignacion::getHorasDocencia)
                .filter(Objects::nonNull)
                .reduce(0f, Float::sum);
        if (totalHoras - limite > EPSILON) {
            throw new ValidacionNegocioException(String.format(
                    "El docente %s supera el máximo de %.0f horas permitidas para su contratación %s.",
                    obtenerNombreDocente(seleccionado), limite, tipo.getValor()));
        }
    }

    private float obtenerLimiteHorasDocencia(ContratacionEnum tipo, String dedicacion) {
        boolean medioTiempo = esDedicacionMedioTiempo(dedicacion);
        switch (tipo) {
            case PLANTA:
                return medioTiempo ? HORAS_MAX_PLANTA_MEDIO_TIEMPO : HORAS_MAX_PLANTA_TIEMPO_COMPLETO;
            case OCASIONAL:
                return medioTiempo ? HORAS_MAX_OCASIONAL_MEDIO_TIEMPO : HORAS_MAX_OCASIONAL_TIEMPO_COMPLETO;
            case CATEDRA:
            case BECARIOS_Y_PRACTICANTES:
                return HORAS_MAX_CATEDRA_O_BECARIO;
            case BECARIOS_POSTGRADO:
            default:
                return 0f;
        }
    }

    private String obtenerNombreDocente(Seleccionado seleccionado) {
        if (seleccionado.getUsuario() == null) {
            return "con OID " + seleccionado.getOidSeleccionado();
        }
        String nombres = seleccionado.getUsuario().getNombres() != null ? seleccionado.getUsuario().getNombres() : "";
        String apellidos = seleccionado.getUsuario().getApellidos() != null ? seleccionado.getUsuario().getApellidos() : "";
        return (nombres + " " + apellidos).trim();
    }

    private String obtenerDedicacion(Seleccionado seleccionado) {
        if (seleccionado == null) {
            return null;
        }
        if (seleccionado.getDedicacion() != null && !seleccionado.getDedicacion().isBlank()) {
            return seleccionado.getDedicacion();
        }
        Usuario usuario = seleccionado.getUsuario();
        if (usuario == null) {
            return null;
        }
        UsuarioDetalle detalle = usuario.getUsuarioDetalle();
        return detalle != null ? detalle.getDedicacion() : null;
    }

    private boolean esDedicacionMedioTiempo(String dedicacion) {
        if (dedicacion == null || dedicacion.isBlank()) {
            return false;
        }
        String normalizada = normalizarTexto(dedicacion);
        return "MEDIO TIEMPO".equals(normalizada);
    }

    private String normalizarTexto(String valor) {
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[\\s_]+", "")
                .toUpperCase();
    }

    private void redistribuirHoras(Necesidad necesidad) {
        List<Asignacion> asignaciones = asignacionRepository.findByNecesidad_OidNecesidad(necesidad.getOidNecesidad());
        if (asignaciones.isEmpty()) {
            return;
        }

        Materia materia = necesidad.getMateria();
        Calendario calendario = necesidad.getCalendario();

        float horasMateria = materia.getHorasSemana() != null ? materia.getHorasSemana().floatValue() : 0f;
        float semanasDocencia = calendario.getSemanasClase() != null ? calendario.getSemanasClase() : 0f;
        float semanasPreparacion = calendario.getSemanasPreparacion() != null ? calendario.getSemanasPreparacion() : semanasDocencia;

        float horasPorDocente = horasMateria / asignaciones.size();
        float horasPreparacion = horasPorDocente * PREPARACION_FACTOR;

        asignaciones.forEach(asignacion -> {
            asignacion.setHorasDocencia(horasPorDocente);
            asignacion.setSemanasDocencia(semanasDocencia);
            ContratacionEnum tipo = asignacion.getSeleccionado() != null ? asignacion.getSeleccionado().getTipo() : null;
            boolean sinPreparacion = esContratacionSinPreparacion(tipo);
            asignacion.setHorasPreparacion(sinPreparacion ? 0f : horasPreparacion);
            asignacion.setSemanasPreparacion(sinPreparacion ? 0f : semanasPreparacion);

            Actividad actividad = asignacion.getActividad();
            if (actividad != null) {
                actividad.setSemanas(semanasDocencia);
                actividad.setNombreActividad(asignacion.getActividad().getNombreActividad());
            }
        });

        asignacionRepository.saveAll(asignaciones);
    }

    private void actualizarEstadoNecesidad(Necesidad necesidad) {
        long totalAsignaciones = asignacionRepository.countByNecesidad_OidNecesidad(necesidad.getOidNecesidad());
        EstadoNecesidad nuevoEstado = totalAsignaciones > 0 ? EstadoNecesidad.ASIGNADA : EstadoNecesidad.NO_ASIGNADA;
        if (necesidad.getEstado() != nuevoEstado) {
            necesidad.setEstado(nuevoEstado);
            necesidadRepository.save(necesidad);
        }
    }

    private void validarCalendarioCompartido(Necesidad necesidad, Seleccionado seleccionado) {
        if (necesidad == null || necesidad.getCalendario() == null) {
            throw new ValidacionNegocioException("La necesidad indicada no tiene un calendario asociado.");
        }
        if (seleccionado == null || seleccionado.getCalendario() == null) {
            throw new ValidacionNegocioException("El seleccionado indicado no tiene un calendario asociado.");
        }
        if (!Objects.equals(necesidad.getCalendario().getOidcalendario(), seleccionado.getCalendario().getOidcalendario())) {
            throw new AsignacionCalendarioInvalidoException();
        }
    }
}
