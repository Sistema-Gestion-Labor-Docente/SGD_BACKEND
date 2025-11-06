package co.edu.unicauca.sgd.api.service.necesidad.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.Asignacion;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Materia;
import co.edu.unicauca.sgd.api.domain.Necesidad;
import co.edu.unicauca.sgd.api.domain.Seleccionado;
import co.edu.unicauca.sgd.api.domain.TipoActividad;
import co.edu.unicauca.sgd.api.domain.EstadoActividad;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.necesidades.AsignacionDTORequest;
import co.edu.unicauca.sgd.api.dto.necesidades.AsignacionDTOResponse;
import co.edu.unicauca.sgd.api.exception.RecursoNoEncontradoException;
import co.edu.unicauca.sgd.api.exception.asignacion.AsignacionCalendarioInvalidoException;
import co.edu.unicauca.sgd.api.exception.asignacion.AsignacionDocenteDuplicadoException;
import co.edu.unicauca.sgd.api.exception.asignacion.AsignacionLimiteDocentesException;
import co.edu.unicauca.sgd.api.exception.asignacion.AsignacionNoEncontradaException;
import co.edu.unicauca.sgd.api.exception.asignacion.AsignacionTipoActividadNoConfiguradaException;
import co.edu.unicauca.sgd.api.mapper.AsignacionMapper;
import co.edu.unicauca.sgd.api.repository.ActividadRepository;
import co.edu.unicauca.sgd.api.repository.AsignacionRepository;
import co.edu.unicauca.sgd.api.repository.EstadoActividadRepository;
import co.edu.unicauca.sgd.api.repository.NecesidadRepository;
import co.edu.unicauca.sgd.api.repository.SeleccionadoRepository;
import co.edu.unicauca.sgd.api.repository.TipoActividadRepository;
import co.edu.unicauca.sgd.api.service.necesidad.AsignacionService;

@Service
public class AsignacionServiceImpl implements AsignacionService {

    private static final float PREPARACION_FACTOR = 2.5f;
    private static final String DOCENCIA_DIRECTA = "DOCENCIA_DIRECTA";

    private final AsignacionRepository asignacionRepository;
    private final NecesidadRepository necesidadRepository;
    private final SeleccionadoRepository seleccionadoRepository;
    private final TipoActividadRepository tipoActividadRepository;
    private final EstadoActividadRepository estadoActividadRepository;
    private final ActividadRepository actividadRepository;
    private final AsignacionMapper asignacionMapper;

    public AsignacionServiceImpl(AsignacionRepository asignacionRepository,
                                 NecesidadRepository necesidadRepository,
                                 SeleccionadoRepository seleccionadoRepository,
                                 TipoActividadRepository tipoActividadRepository,
                                 EstadoActividadRepository estadoActividadRepository,
                                 ActividadRepository actividadRepository,
                                 AsignacionMapper asignacionMapper) {
        this.asignacionRepository = asignacionRepository;
        this.necesidadRepository = necesidadRepository;
        this.seleccionadoRepository = seleccionadoRepository;
        this.tipoActividadRepository = tipoActividadRepository;
        this.estadoActividadRepository = estadoActividadRepository;
        this.actividadRepository = actividadRepository;
        this.asignacionMapper = asignacionMapper;
    }

    @Override
    public ApiResponse<Page<AsignacionDTOResponse>> listar(Integer oidNecesidad, Integer oidSeleccionado, Pageable pageable) {
        Pageable pageableToUse = pageable != null ? pageable : Pageable.unpaged();

        List<Asignacion> asignaciones;
        if (oidNecesidad != null) {
            asignaciones = asignacionRepository.findByNecesidad_OidNecesidad(oidNecesidad);
        } else {
            asignaciones = asignacionRepository.findAll(pageableToUse).getContent();
        }

        if (oidSeleccionado != null) {
            asignaciones = asignaciones.stream()
                    .filter(a -> Objects.equals(a.getSeleccionado().getOidSeleccionado(), oidSeleccionado))
                    .collect(Collectors.toList());
        }

        if (asignaciones.isEmpty()) {
            return new ApiResponse<>(204, "No se encontraron asignaciones.", Page.empty(pageableToUse));
        }

        Page<AsignacionDTOResponse> page = new PageImpl<>(
                asignaciones.stream().map(asignacionMapper::toResponse).collect(Collectors.toList()),
                pageableToUse,
                asignaciones.size()
        );

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

        Integer necesidadOriginal = asignacion.getNecesidad().getOidNecesidad();
        prepararAsignacion(asignacion, request, false);
        asignacion = asignacionRepository.save(asignacion);

        redistribuirHoras(asignacion.getNecesidad());
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
        asignacionRepository.delete(asignacion);
        redistribuirHoras(necesidad);

        return new ApiResponse<>(204, "Asignación eliminada correctamente.", null);
    }

    private void prepararAsignacion(Asignacion asignacion, AsignacionDTORequest request, boolean esNuevaAsignacion) {
        Necesidad necesidad = necesidadRepository.findById(request.getOidNecesidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("La necesidad indicada no existe."));

        Seleccionado seleccionado = seleccionadoRepository.findById(request.getOidSeleccionado())
                .orElseThrow(() -> new RecursoNoEncontradoException("El seleccionado indicado no existe."));

        if (!Objects.equals(necesidad.getCalendario().getOidcalendario(), seleccionado.getCalendario().getOidcalendario())) {
            throw new AsignacionCalendarioInvalidoException();
        }

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

        EstadoActividad estadoActividad = estadoActividadRepository.findById(request.getOidEstadoActividad())
                .orElseThrow(() -> new RecursoNoEncontradoException("El estado de actividad indicado no existe."));

        Actividad actividad = asignacion.getActividad();
        if (actividad == null) {
            actividad = new Actividad();
        }
        actividad.setTipoActividad(tipoActividad);
        actividad.setEstadoActividad(estadoActividad);
        actividad.setNombreActividad(request.getNombreActividad());

        actividadRepository.save(actividad);

        asignacion.setActividad(actividad);
        asignacion.setNecesidad(necesidad);
        asignacion.setSeleccionado(seleccionado);
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
            asignacion.setHorasPreparacion(horasPreparacion);
            asignacion.setSemanasPreparacion(semanasPreparacion);

            Actividad actividad = asignacion.getActividad();
            if (actividad != null) {
                actividad.setHoras(horasPorDocente);
                actividad.setSemanas(semanasDocencia);
                actividad.setNombreActividad(asignacion.getActividad().getNombreActividad());
            }
        });

        asignacionRepository.saveAll(asignaciones);
    }
}
