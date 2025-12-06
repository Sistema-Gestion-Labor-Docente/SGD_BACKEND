package co.edu.unicauca.sgd.api.service.actividad.laborDocente.impl;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.ActividadCalendario;
import co.edu.unicauca.sgd.api.domain.Asignacion;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.CargoActividad;
import co.edu.unicauca.sgd.api.domain.EavAtributo;
import co.edu.unicauca.sgd.api.domain.EstadoActividad;
import co.edu.unicauca.sgd.api.domain.Fecha;
import co.edu.unicauca.sgd.api.domain.Necesidad;
import co.edu.unicauca.sgd.api.domain.TipoActividad;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioActividadCalendario;
import co.edu.unicauca.sgd.api.domain.UsuarioDepartamento;
import co.edu.unicauca.sgd.api.domain.UsuarioDetalle;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.UsuarioDTO;
import co.edu.unicauca.sgd.api.dto.AtributoDTO;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.DocenciaDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.HorasLaborDocenteDTO;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioCreacionResultadoDTO;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioUsuarioDTO;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.ValidacionHorasCargoDTOResponse;
import co.edu.unicauca.sgd.api.exception.AsignacionHorasExcedidasException;
import co.edu.unicauca.sgd.api.exception.RecursoNoEncontradoException;
import co.edu.unicauca.sgd.api.exception.ValidacionNegocioException;
import co.edu.unicauca.sgd.api.enums.ContratacionEnum;
import co.edu.unicauca.sgd.api.exception.usuarioactividad.UsuarioActividadCalendarioActualizacionException;
import co.edu.unicauca.sgd.api.exception.usuarioactividad.UsuarioActividadCalendarioConsultaException;
import co.edu.unicauca.sgd.api.exception.usuarioactividad.UsuarioActividadCalendarioCreacionException;
import co.edu.unicauca.sgd.api.exception.usuarioactividad.UsuarioActividadCalendarioEliminacionException;
import co.edu.unicauca.sgd.api.exception.usuarioactividad.UsuarioActividadCalendarioException;
import co.edu.unicauca.sgd.api.exception.usuarioactividad.UsuarioActividadCalendarioValidacionException;
import co.edu.unicauca.sgd.api.mapper.AsignacionMapper;
import co.edu.unicauca.sgd.api.mapper.MateriaMapper;
import co.edu.unicauca.sgd.api.mapper.NecesidadMapper;
import co.edu.unicauca.sgd.api.mapper.UsuarioActividadCalendarioMapper;
import co.edu.unicauca.sgd.api.repository.AsignacionRepository;
import co.edu.unicauca.sgd.api.repository.ActividadCalendarioRepository;
import co.edu.unicauca.sgd.api.repository.ActividadRepository;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.CargoActividadRepository;
import co.edu.unicauca.sgd.api.repository.EavAtributoRepository;
import co.edu.unicauca.sgd.api.repository.EstadoActividadRepository;
import co.edu.unicauca.sgd.api.repository.FechaRepository;
import co.edu.unicauca.sgd.api.repository.TipoActividadRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioActividadCalendarioRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioDepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioRepository;
import co.edu.unicauca.sgd.api.repository.projection.UsuarioHorasPorTipoActividadProjection;
import co.edu.unicauca.sgd.api.service.EavAtributoService;
import co.edu.unicauca.sgd.api.service.actividad.laborDocente.UsuarioActividadCalendarioService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class UsuarioActividadCalendarioServiceImpl implements UsuarioActividadCalendarioService {

    private final ActividadRepository actividadRepository;
    private final UsuarioRepository usuarioRepository;
    private final CalendarioRepository calendarioRepository;
    private final ActividadCalendarioRepository actividadCalendarioRepository;
    private final UsuarioActividadCalendarioRepository usuarioActividadCalendarioRepository;
    private final UsuarioDepartamentoRepository usuarioDepartamentoRepository;
    private final UsuarioActividadCalendarioMapper mapper;
    private final CargoActividadRepository cargoActividadRepository;

    /**
     * Cargos que corresponden a roles dentro de proyectos de investigación
     * y que comparten el mismo límite de horas de manera conjunta.
     */
    private static final Set<Integer> CARGOS_PROYECTO_INVESTIGACION = Set.of(2, 3, 4);
    private final EstadoActividadRepository estadoActividadRepository;
    private final EavAtributoService eavAtributoService;
    private final EavAtributoRepository eavAtributoRepository;
    private final TipoActividadRepository tipoActividadRepository;
    private final FechaRepository fechaRepository;
    private final AsignacionRepository asignacionRepository;
    private final AsignacionMapper asignacionMapper;
    private final NecesidadMapper necesidadMapper;
    private final MateriaMapper materiaMapper;
    private final ObjectMapper objectMapper;

    private static final float EPSILON = 0.0001f;
    private static final float HORAS_DEFAULT_CONTRATACION = 40f;
    private static final int NOMBRE_PERIODO_INICIO = 1;
    private static final int NOMBRE_PERIODO_FIN = 10;

    public UsuarioActividadCalendarioServiceImpl(
            ActividadRepository actividadRepository,
            UsuarioRepository usuarioRepository,
            CalendarioRepository calendarioRepository,
            ActividadCalendarioRepository actividadCalendarioRepository,
            UsuarioActividadCalendarioRepository usuarioActividadCalendarioRepository,
            UsuarioDepartamentoRepository usuarioDepartamentoRepository,
            UsuarioActividadCalendarioMapper mapper,
            CargoActividadRepository cargoActividadRepository,
            EstadoActividadRepository estadoActividadRepository,
            EavAtributoService eavAtributoService,
            EavAtributoRepository eavAtributoRepository,
            TipoActividadRepository tipoActividadRepository,
            FechaRepository fechaRepository,
            AsignacionRepository asignacionRepository,
            AsignacionMapper asignacionMapper,
            NecesidadMapper necesidadMapper,
            MateriaMapper materiaMapper,
            ObjectMapper objectMapper) {
        this.actividadRepository = actividadRepository;
        this.usuarioRepository = usuarioRepository;
        this.calendarioRepository = calendarioRepository;
        this.actividadCalendarioRepository = actividadCalendarioRepository;
        this.usuarioActividadCalendarioRepository = usuarioActividadCalendarioRepository;
        this.usuarioDepartamentoRepository = usuarioDepartamentoRepository;
        this.mapper = mapper;
        this.cargoActividadRepository = cargoActividadRepository;
        this.estadoActividadRepository = estadoActividadRepository;
        this.eavAtributoService = eavAtributoService;
        this.eavAtributoRepository = eavAtributoRepository;
        this.tipoActividadRepository = tipoActividadRepository;
        this.fechaRepository = fechaRepository;
        this.asignacionRepository = asignacionRepository;
        this.asignacionMapper = asignacionMapper;
        this.necesidadMapper = necesidadMapper;
        this.materiaMapper = materiaMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ApiResponse<UsuarioActividadCalendarioDTOResponse> crearActividadConRelaciones(@Valid UsuarioActividadCalendarioDTORequest request) {
        try {
            UsuarioActividadCalendarioDTOResponse dto = ejecutarCreacionActividad(request);
            return new ApiResponse<>(201, "Actividad creada con relaciones", dto);
        } catch (UsuarioActividadCalendarioException | RecursoNoEncontradoException
                 | ValidacionNegocioException | AsignacionHorasExcedidasException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioActividadCalendarioCreacionException("Error al crear la actividad con sus relaciones.", e);
        }
    }

    @Override
    public ApiResponse<List<UsuarioActividadCalendarioCreacionResultadoDTO>> crearActividadesConRelaciones(List<UsuarioActividadCalendarioDTORequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new ValidacionNegocioException("Debe suministrar al menos una actividad para crear.");
        }
        List<UsuarioActividadCalendarioCreacionResultadoDTO> resultados = new ArrayList<>(requests.size());
        for (int i = 0; i < requests.size(); i++) {
            UsuarioActividadCalendarioDTORequest request = requests.get(i);
            try {
                UsuarioActividadCalendarioDTOResponse dto = ejecutarCreacionActividad(request);
                resultados.add(UsuarioActividadCalendarioCreacionResultadoDTO.builder()
                        .indice(i)
                        .exito(true)
                        .mensaje("Actividad creada correctamente.")
                        .actividad(dto)
                        .build());
            } catch (Exception ex) {
                resultados.add(UsuarioActividadCalendarioCreacionResultadoDTO.builder()
                        .indice(i)
                        .exito(false)
                        .mensaje(ex.getMessage())
                        .actividad(null)
                        .build());
            }
        }
        return new ApiResponse<>(200, "Procesamiento masivo finalizado.", resultados);
    }

    @Override
    @Transactional
    public ApiResponse<UsuarioActividadCalendarioDTOResponse> actualizarActividadConRelaciones(Integer oidActividad, @Valid UsuarioActividadCalendarioDTORequest request) {        
        try {
            return ejecutarActualizacionActividad(oidActividad, request);
        } catch (UsuarioActividadCalendarioException | RecursoNoEncontradoException
                 | ValidacionNegocioException | AsignacionHorasExcedidasException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioActividadCalendarioActualizacionException("Error al actualizar la actividad con sus relaciones.", e);
        }
    }

    private UsuarioActividadCalendarioDTOResponse ejecutarCreacionActividad(UsuarioActividadCalendarioDTORequest request) {
        if (request.getUsuarios() == null || request.getUsuarios().isEmpty()) {
            throw new ValidacionNegocioException("Debe especificar al menos un usuario para la actividad.");
        }
        Map<Integer, Float> horasPorUsuario = new HashMap<>();
        Map<Integer, Integer> cargoIdsPorUsuario = new HashMap<>();
        Set<Integer> cargoIdsSolicitados = new HashSet<>();
        for (UsuarioActividadCalendarioUsuarioDTO usuarioDTO : request.getUsuarios()) {
            if (usuarioDTO.getOidUsuario() == null) {
                throw new ValidacionNegocioException("Cada usuario debe incluir su identificador.");
            }
            if (usuarioDTO.getHoras() == null || usuarioDTO.getHoras() <= 0) {
                throw new ValidacionNegocioException("Las horas por usuario deben ser mayores a cero.");
            }
            horasPorUsuario.merge(usuarioDTO.getOidUsuario(), usuarioDTO.getHoras(), Float::sum);
            if (usuarioDTO.getOidCargoActividad() != null) {
                cargoIdsPorUsuario.put(usuarioDTO.getOidUsuario(), usuarioDTO.getOidCargoActividad());
                cargoIdsSolicitados.add(usuarioDTO.getOidCargoActividad());
            }
        }

        Map<Integer, CargoActividad> cargosRegistrados = cargarCargos(cargoIdsSolicitados);
        Map<Integer, CargoActividad> cargoPorUsuario = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : cargoIdsPorUsuario.entrySet()) {
            CargoActividad cargo = cargosRegistrados.get(entry.getValue());
            if (cargo == null) {
                throw new RecursoNoEncontradoException("Cargo de actividad no encontrado: " + entry.getValue());
            }
            cargoPorUsuario.put(entry.getKey(), cargo);
        }

        TipoActividad tipoActividad = resolverTipoActividad(cargoPorUsuario.values(), request, null);
        EstadoActividad estadoActividad = estadoActividadRepository.findById(request.getOidEstadoActividad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Estado de actividad no encontrado"));

        Calendario calendario = calendarioRepository.findById(request.getOidCalendario())
                .orElseThrow(() -> new RecursoNoEncontradoException("Calendario no encontrado"));

        Map<Integer, Integer> departamentosPorUsuario = resolverDepartamentosParaValidaciones(cargoPorUsuario);

        Map<Integer, Usuario> usuariosValidadosPorContratacion = validarHorasPorContratacionUsuarios(
                horasPorUsuario,
                calendario,
                null
        );

        validarMaximoHorasUsuarios(
                horasPorUsuario,
                cargoPorUsuario,
                departamentosPorUsuario,
                tipoActividad,
                null
        );

        validarMaximoActividadesPorUsuario(new HashSet<>(horasPorUsuario.keySet()), cargoPorUsuario, null);

        Actividad actividad = new Actividad();
        actividad.setTipoActividad(tipoActividad);
        actividad.setEstadoActividad(estadoActividad);
        actividad.setNombreActividad(request.getNombreActividad());
        actividad.setSemanas(request.getSemanas());
        actividad = actividadRepository.save(actividad);

        final Actividad actividadFinal = actividad;

        ActividadCalendario actividadCalendario = actividadCalendarioRepository
                .findByActividad_OidActividadAndCalendario_Oidcalendario(actividad.getOidActividad(), calendario.getOidcalendario())
                .orElseGet(() -> {
                    ActividadCalendario ac = new ActividadCalendario();
                    ac.setActividad(actividadFinal);
                    ac.setCalendario(calendario);
                    ac.setUsuarioCreacion("system");
                    return actividadCalendarioRepository.save(ac);
                });

        for (Integer oidUsuario : horasPorUsuario.keySet()) {
            Usuario usuario = usuariosValidadosPorContratacion.getOrDefault(oidUsuario,
                    usuarioRepository.findById(oidUsuario)
                            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado")));
            boolean exists = usuarioActividadCalendarioRepository.existsByActividadCalendario_OidActividadCalendarioAndUsuario_OidUsuario(
                    actividadCalendario.getOidActividadCalendario(), oidUsuario);
            if (!exists) {
                UsuarioActividadCalendario relacion = new UsuarioActividadCalendario();
                relacion.setUsuario(usuario);
                relacion.setActividadCalendario(actividadCalendario);
                relacion.setCargoActividad(cargoPorUsuario.get(oidUsuario));
                relacion.setHorasActividad(horasPorUsuario.get(oidUsuario));
                relacion.setUsuarioCreacion("system");
                usuarioActividadCalendarioRepository.save(relacion);
            }
        }

        if (request.getAtributos() != null && !request.getAtributos().isEmpty()) {
            Map<String, EavAtributo> cacheAtributos = eavAtributoRepository.findAll().stream()
                    .collect(Collectors.toMap(EavAtributo::getNombre, Function.identity()));

            ActividadBaseDTO actividadBaseDTO = new ActividadBaseDTO();
            actividadBaseDTO.setOidActividad(actividad.getOidActividad());
            actividadBaseDTO.setAtributos(
                    request.getAtributos().stream()
                            .map(a -> new AtributoDTO(a.getNombre(), a.getValor()))
                            .collect(Collectors.toList())
            );

            eavAtributoService.guardarAtributosDinamicos(actividadBaseDTO, actividad, cacheAtributos);
        }

        List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository
                .findByActividadCalendario_OidActividadCalendario(actividadCalendario.getOidActividadCalendario());
        UsuarioActividadCalendarioDTOResponse dtoResponse = mapper.toResponse(
                actividad,
                relaciones,
                calendario,
                (request.getAtributos() != null) ? request.getAtributos().stream()
                        .map(a -> new AtributoDTO(a.getNombre(), a.getValor()))
                        .collect(Collectors.toList()) : List.of()
        );
        asignarHorasLaborPorUsuario(actividad, relaciones, dtoResponse);
        return dtoResponse;
    }

    private ApiResponse<UsuarioActividadCalendarioDTOResponse> ejecutarActualizacionActividad(
            Integer oidActividad,
            UsuarioActividadCalendarioDTORequest request) {
        Actividad actividad = actividadRepository.findById(oidActividad)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad no encontrada"));

        if (request.getUsuarios() == null || request.getUsuarios().isEmpty()) {
            throw new ValidacionNegocioException("Debe especificar al menos un usuario para la actividad.");
        }
        Map<Integer, Float> horasPorUsuario = new HashMap<>();
        Map<Integer, Integer> cargoIdsPorUsuario = new HashMap<>();
        Set<Integer> cargoIdsSolicitados = new HashSet<>();
        for (UsuarioActividadCalendarioUsuarioDTO usuarioDTO : request.getUsuarios()) {
            if (usuarioDTO.getOidUsuario() == null) {
                throw new ValidacionNegocioException("Cada usuario debe incluir su identificador.");
            }
            if (usuarioDTO.getHoras() == null || usuarioDTO.getHoras() <= 0) {
                throw new ValidacionNegocioException("Las horas por usuario deben ser mayores a cero.");
            }
            horasPorUsuario.merge(usuarioDTO.getOidUsuario(), usuarioDTO.getHoras(), Float::sum);
            if (usuarioDTO.getOidCargoActividad() != null) {
                cargoIdsPorUsuario.put(usuarioDTO.getOidUsuario(), usuarioDTO.getOidCargoActividad());
                cargoIdsSolicitados.add(usuarioDTO.getOidCargoActividad());
            }
        }

        Map<Integer, CargoActividad> cargosRegistrados = cargarCargos(cargoIdsSolicitados);
        Map<Integer, CargoActividad> cargoPorUsuario = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : cargoIdsPorUsuario.entrySet()) {
            CargoActividad cargo = cargosRegistrados.get(entry.getValue());
            if (cargo == null) {
                throw new RecursoNoEncontradoException("Cargo de actividad no encontrado: " + entry.getValue());
            }
            cargoPorUsuario.put(entry.getKey(), cargo);
        }

        EstadoActividad estadoActividad = estadoActividadRepository.findById(request.getOidEstadoActividad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Estado de actividad no encontrado"));

        TipoActividad tipoActividad = resolverTipoActividad(cargoPorUsuario.values(), request, actividad);

        Calendario calendarioDestino = calendarioRepository.findById(request.getOidCalendario())
                .orElseThrow(() -> new RecursoNoEncontradoException("Calendario no encontrado"));

        Map<Integer, Integer> departamentosPorUsuario = resolverDepartamentosParaValidaciones(cargoPorUsuario);

        Map<Integer, Usuario> usuariosValidadosPorContratacion = validarHorasPorContratacionUsuarios(
                horasPorUsuario,
                calendarioDestino,
                actividad.getOidActividad()
        );

        validarMaximoHorasUsuarios(
                horasPorUsuario,
                cargoPorUsuario,
                departamentosPorUsuario,
                tipoActividad,
                actividad.getOidActividad()
        );

        validarMaximoActividadesPorUsuario(new HashSet<>(horasPorUsuario.keySet()), cargoPorUsuario, actividad.getOidActividad());

        // Actualizar datos base de Actividad
        actividad.setTipoActividad(tipoActividad);
        actividad.setEstadoActividad(estadoActividad);
        actividad.setNombreActividad(request.getNombreActividad());
        actividad.setSemanas(request.getSemanas());
        actividad = actividadRepository.save(actividad);

        final Actividad actividadFinal = actividad;

        // Obtener/crear ActividadCalendario destino (puede ser el mismo o uno nuevo si cambió el calendario)
        ActividadCalendario actividadCalendarioDestino = actividadCalendarioRepository
                .findByActividad_OidActividadAndCalendario_Oidcalendario(actividad.getOidActividad(), calendarioDestino.getOidcalendario())
                .orElseGet(() -> {
                    ActividadCalendario ac = new ActividadCalendario();
                    ac.setActividad(actividadFinal);
                    ac.setCalendario(calendarioDestino);
                    ac.setUsuarioCreacion("system");
                    return actividadCalendarioRepository.save(ac);
                });

        // Sincronizar relaciones de usuarios:
        //  - obtener relaciones existentes para la actividad en el calendario destino
        List<UsuarioActividadCalendario> relacionesExistentes = usuarioActividadCalendarioRepository.findByActividadCalendario_OidActividadCalendario(actividadCalendarioDestino.getOidActividadCalendario());
        Set<Integer> existentesOids = relacionesExistentes.stream()
                .map(r -> r.getUsuario().getOidUsuario())
                .collect(Collectors.toSet());

        Set<Integer> solicitados = new HashSet<>(horasPorUsuario.keySet());

        // Añadir nuevos
        for (Integer oidUsuario : solicitados) {
            if (!existentesOids.contains(oidUsuario)) {
                Usuario usuario = usuariosValidadosPorContratacion.get(oidUsuario);
                if (usuario == null) {
                    usuario = usuarioRepository.findById(oidUsuario)
                            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
                }
                UsuarioActividadCalendario relacion = new UsuarioActividadCalendario();
                relacion.setUsuario(usuario);
                relacion.setActividadCalendario(actividadCalendarioDestino);
                relacion.setCargoActividad(cargoPorUsuario.get(oidUsuario));
                relacion.setHorasActividad(horasPorUsuario.get(oidUsuario));
                relacion.setUsuarioCreacion("system");
                usuarioActividadCalendarioRepository.save(relacion);
            }
        }

        // Eliminar relaciones que ya no están solicitadas (solo en el calendario destino)
        for (UsuarioActividadCalendario exist : relacionesExistentes) {
            Integer oidUsuarioExistente = exist.getUsuario().getOidUsuario();
            if (!solicitados.contains(oidUsuarioExistente)) {
                usuarioActividadCalendarioRepository.deleteByActividadCalendario_OidActividadCalendarioAndUsuario_OidUsuario(actividadCalendarioDestino.getOidActividadCalendario(), oidUsuarioExistente);
            } else {
                exist.setCargoActividad(cargoPorUsuario.get(oidUsuarioExistente));
                exist.setHorasActividad(horasPorUsuario.get(oidUsuarioExistente));
                usuarioActividadCalendarioRepository.save(exist);
            }
        }

        // Si el calendario cambió respecto a otras ActividadCalendario existentes, limpiamos actividadCalendario vacíos
        List<ActividadCalendario> actividadCalendariosPrevios = actividadCalendarioRepository.findByActividad_OidActividad(actividad.getOidActividad());
        for (ActividadCalendario acPrev : actividadCalendariosPrevios) {
            // si es distinto al destino, y no tiene relaciones, lo borramos
            if (!acPrev.getOidActividadCalendario().equals(actividadCalendarioDestino.getOidActividadCalendario())) {
                List<UsuarioActividadCalendario> rels = usuarioActividadCalendarioRepository.findByActividadCalendario_OidActividadCalendario(acPrev.getOidActividadCalendario());
                if (rels.isEmpty()) {
                    actividadCalendarioRepository.deleteById(acPrev.getOidActividadCalendario());
                }
            }
        }

        // Actualizar atributos EAV
        if (request.getAtributos() != null && !request.getAtributos().isEmpty()) {
            Map<String, EavAtributo> cacheAtributos = eavAtributoRepository.findAll().stream()
                    .collect(Collectors.toMap(EavAtributo::getNombre, Function.identity()));

            ActividadBaseDTO actividadBaseDTO = new ActividadBaseDTO();
            actividadBaseDTO.setOidActividad(actividad.getOidActividad());
            actividadBaseDTO.setAtributos(
                    request.getAtributos().stream()
                            .map(a -> new AtributoDTO(a.getNombre(), a.getValor()))
                            .collect(Collectors.toList())
            );

            eavAtributoService.actualizarAtributosDinamicos(actividadBaseDTO, actividad, cacheAtributos);
        }

        // Construir respuesta: relaciones actuales del actividadCalendarioDestino
        List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository.findByActividadCalendario_OidActividadCalendario(actividadCalendarioDestino.getOidActividadCalendario());
        UsuarioActividadCalendarioDTOResponse dto = mapper.toResponse(
                actividad, relaciones, calendarioDestino,
                (request.getAtributos() != null) ? request.getAtributos().stream()
                        .map(a -> new AtributoDTO(a.getNombre(), a.getValor()))
                        .collect(Collectors.toList()) : List.of()
        );
        asignarHorasLaborPorUsuario(actividad, relaciones, dto);

        return new ApiResponse<>(200, "Actividad actualizada con relaciones", dto);
    }

    private Map<Integer, CargoActividad> cargarCargos(Set<Integer> cargoIds) {
        if (cargoIds == null || cargoIds.isEmpty()) {
            return Map.of();
        }
        return cargoActividadRepository.findAllById(cargoIds).stream()
                .collect(Collectors.toMap(CargoActividad::getOidCargoActividad, Function.identity()));
    }

    private TipoActividad resolverTipoActividad(Collection<CargoActividad> cargos, UsuarioActividadCalendarioDTORequest request, Actividad actividadActual) {
        CargoActividad referencia = null;
        if (cargos != null) {
            for (CargoActividad cargo : cargos) {
                if (cargo == null) {
                    continue;
                }
                if (referencia == null) {
                    referencia = cargo;
                } else if (referencia.getTipoActividad() != null && cargo.getTipoActividad() != null
                        && !Objects.equals(referencia.getTipoActividad().getOidTipoActividad(),
                        cargo.getTipoActividad().getOidTipoActividad())) {
                    throw new ValidacionNegocioException("Todos los cargos asociados deben pertenecer al mismo tipo de actividad.");
                }
            }
        }
        if (referencia != null && referencia.getTipoActividad() != null) {
            return referencia.getTipoActividad();
        }
        if (request.getOidTipoActividad() != null) {
            return tipoActividadRepository.findById(request.getOidTipoActividad())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de actividad no encontrado"));
        }
        if (actividadActual != null && actividadActual.getTipoActividad() != null) {
            return actividadActual.getTipoActividad();
        }
        throw new ValidacionNegocioException("Debe especificar un cargo o un tipo de actividad para la asignación.");
    }

    private void validarMaximoHorasUsuarios(Map<Integer, Float> horasPorUsuario,
                                            Map<Integer, CargoActividad> cargoPorUsuario,
                                            Map<Integer, Integer> departamentoPorUsuario,
                                            TipoActividad tipoActividad,
                                            Integer oidActividadActual) {
        if (horasPorUsuario == null || horasPorUsuario.isEmpty()) {
            return;
        }

        if (tipoActividad == null) {
            throw new ValidacionNegocioException("No se pudo determinar el tipo de actividad para validar horas.");
        }

        Float limiteHorasPorTipo = (cargoPorUsuario == null || cargoPorUsuario.isEmpty())
                ? obtenerMaximoHorasPorTipoActividad(tipoActividad.getOidTipoActividad())
                : null;

        Map<Integer, Map<Integer, Float>> horasSolicitadasPorCargoDepto = new HashMap<>();

        for (Map.Entry<Integer, Float> entry : horasPorUsuario.entrySet()) {
            Integer oidUsuario = entry.getKey();
            float horasSolicitadas = entry.getValue() == null ? 0f : entry.getValue();
            CargoActividad cargoUsuario = cargoPorUsuario != null ? cargoPorUsuario.get(oidUsuario) : null;

            if (cargoUsuario != null && esCargoTipoProgramaDepartamento(cargoUsuario)) {
                if (cargoUsuario.getMaxHorasSemana() == null) {
                    continue;
                }
                Integer oidDepartamento = departamentoPorUsuario != null ? departamentoPorUsuario.get(oidUsuario) : null;
                if (oidDepartamento == null) {
                    throw new ValidacionNegocioException(
                            String.format("El usuario %d no tiene un departamento asignado para el cargo %s.",
                                    oidUsuario, cargoUsuario.getNombre()));
                }
                Map<Integer, Float> horasPorDepto = horasSolicitadasPorCargoDepto
                        .computeIfAbsent(cargoUsuario.getOidCargoActividad(), id -> new HashMap<>());
                float acumuladoDepto = horasPorDepto.getOrDefault(oidDepartamento, 0f) + horasSolicitadas;
                horasPorDepto.put(oidDepartamento, acumuladoDepto);

                float horasAsignadasDepto = calcularHorasPorDepartamentoYCargo(
                        oidDepartamento, cargoUsuario.getOidCargoActividad(), oidActividadActual);
                if ((horasAsignadasDepto + acumuladoDepto) - cargoUsuario.getMaxHorasSemana() > EPSILON) {
                    throw new ValidacionNegocioException(
                            String.format("El departamento %d supera el máximo de %.2f horas para el cargo %s.",
                                    oidDepartamento, cargoUsuario.getMaxHorasSemana(), cargoUsuario.getNombre()));
                }
                continue;
            }

            boolean esCargoProyectoInvestigacion = esCargoProyectoInvestigacion(cargoUsuario);

            float horasAsignadas;
            if (cargoUsuario != null) {
                if (esCargoProyectoInvestigacion) {
                    horasAsignadas = calcularHorasPorUsuarioYGrupoCargos(oidUsuario, CARGOS_PROYECTO_INVESTIGACION, oidActividadActual);
                } else {
                    horasAsignadas = calcularHorasPorUsuarioYCargo(oidUsuario, cargoUsuario.getOidCargoActividad(), oidActividadActual);
                }
            } else {
                horasAsignadas = calcularHorasPorUsuarioYTipoActividad(oidUsuario, tipoActividad.getOidTipoActividad(), oidActividadActual);
            }

            float limite;
            if (cargoUsuario != null) {
                Float maxHoras = cargoUsuario.getMaxHorasSemana();
                limite = (maxHoras == null ? Float.MAX_VALUE : maxHoras);
            } else {
                limite = (limiteHorasPorTipo == null ? Float.MAX_VALUE : limiteHorasPorTipo);
            }

            if (limite != Float.MAX_VALUE && (horasAsignadas + horasSolicitadas) - limite > EPSILON) {
                throw new AsignacionHorasExcedidasException(oidUsuario, limite, horasAsignadas, horasSolicitadas);
            }
        }
    }

    private float calcularHorasPorUsuarioYCargo(Integer oidUsuario, Integer oidCargoActividad, Integer oidActividadActual) {
        if (oidUsuario == null || oidCargoActividad == null) {
            return 0f;
        }
        List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository
                .findByUsuario_OidUsuarioAndCargoActividad_OidCargoActividad(oidUsuario, oidCargoActividad);
        return sumarHorasRelacionadas(relaciones, oidActividadActual);
    }

    private float calcularHorasPorUsuarioYGrupoCargos(Integer oidUsuario, Set<Integer> cargos, Integer oidActividadActual) {
        if (oidUsuario == null || cargos == null || cargos.isEmpty()) {
            return 0f;
        }
        double total = 0d;
        for (Integer oidCargo : cargos) {
            if (oidCargo == null) {
                continue;
            }
            List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository
                    .findByUsuario_OidUsuarioAndCargoActividad_OidCargoActividad(oidUsuario, oidCargo);
            total += sumarHorasRelacionadas(relaciones, oidActividadActual);
        }
        return (float) total;
    }

    private float calcularHorasPorUsuarioYTipoActividad(Integer oidUsuario, Integer oidTipoActividad, Integer oidActividadActual) {
        if (oidUsuario == null || oidTipoActividad == null) {
            return 0f;
        }
        List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository
                .findByUsuario_OidUsuarioAndActividadCalendario_Actividad_TipoActividad_OidTipoActividad(oidUsuario, oidTipoActividad);
        return sumarHorasRelacionadas(relaciones, oidActividadActual);
    }

    private float calcularHorasTotalesUsuario(Integer oidUsuario, Integer oidActividadActual) {
        if (oidUsuario == null) {
            return 0f;
        }
        List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository
                .findByUsuario_OidUsuario(oidUsuario);
        return sumarHorasRelacionadas(relaciones, oidActividadActual);
    }

    private void validarMaximoActividadesPorUsuario(Set<Integer> usuarios,
                                                    Map<Integer, CargoActividad> cargoPorUsuario,
                                                    Integer oidActividadActual) {
        if (usuarios == null || usuarios.isEmpty() || cargoPorUsuario == null || cargoPorUsuario.isEmpty()) {
            return;
        }
        for (Integer oidUsuario : usuarios) {
            CargoActividad cargo = cargoPorUsuario.get(oidUsuario);
            if (cargo == null || cargo.getMaxActividades() == null) {
                continue;
            }
            List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository
                    .findByUsuario_OidUsuarioAndCargoActividad_OidCargoActividad(oidUsuario, cargo.getOidCargoActividad());
            int actividadesAsignadas = contarActividadesRelacionadas(relaciones, oidActividadActual);
            if (actividadesAsignadas + 1 > cargo.getMaxActividades()) {
                throw new ValidacionNegocioException(
                        String.format("El usuario %d ya alcanzó el máximo de %d actividades para el cargo %s.",
                                oidUsuario, cargo.getMaxActividades(), cargo.getNombre()));
            }
        }
    }

    private int contarActividadesRelacionadas(List<UsuarioActividadCalendario> relaciones, Integer oidActividadActual) {
        if (relaciones == null || relaciones.isEmpty()) {
            return 0;
        }
        return (int) relaciones.stream()
                .map(rel -> rel.getActividadCalendario() != null ? rel.getActividadCalendario().getActividad() : null)
                .filter(Objects::nonNull)
                .map(Actividad::getOidActividad)
                .filter(Objects::nonNull)
                .filter(oid -> oidActividadActual == null || !oid.equals(oidActividadActual))
                .distinct()
                .count();
    }

    private float calcularHorasPorDepartamentoYCargo(Integer oidDepartamento, Integer oidCargoActividad, Integer oidActividadActual) {
        if (oidDepartamento == null || oidCargoActividad == null) {
            return 0f;
        }
        List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository
                .findByDepartamentoAndCargo(oidDepartamento, oidCargoActividad);
        return sumarHorasPorRelaciones(relaciones, oidActividadActual);
    }

    private float sumarHorasPorRelaciones(List<UsuarioActividadCalendario> relaciones, Integer oidActividadActual) {
        if (relaciones == null || relaciones.isEmpty()) {
            return 0f;
        }
        double total = 0d;
        for (UsuarioActividadCalendario relacion : relaciones) {
            if (relacion == null) {
                continue;
            }
            ActividadCalendario actividadCalendario = relacion.getActividadCalendario();
            Actividad actividad = actividadCalendario != null ? actividadCalendario.getActividad() : null;
            Integer oidActividad = actividad != null ? actividad.getOidActividad() : null;
            if (oidActividad != null && oidActividad.equals(oidActividadActual)) {
                continue;
            }
            Float horas = relacion.getHorasActividad();
            if (horas != null) {
                total += horas;
            }
        }
        return (float) total;
    }

    private float sumarHorasRelacionadas(List<UsuarioActividadCalendario> relaciones, Integer oidActividadActual) {
        if (relaciones == null || relaciones.isEmpty()) {
            return 0f;
        }
        Set<Integer> actividadesProcesadas = new HashSet<>();
        double total = 0d;
        for (UsuarioActividadCalendario relacion : relaciones) {
            if (relacion == null) {
                continue;
            }
            ActividadCalendario actividadCalendario = relacion.getActividadCalendario();
            Actividad actividad = actividadCalendario != null ? actividadCalendario.getActividad() : null;
            Integer oidActividad = actividad != null ? actividad.getOidActividad() : null;
            if (oidActividad != null && oidActividad.equals(oidActividadActual)) {
                continue;
            }
            Float horasRelacion = relacion.getHorasActividad();
            if (horasRelacion != null) {
                total += horasRelacion;
                continue;
            }
        }
        return (float) total;
    }

    private Float obtenerMaximoHorasPorTipoActividad(Integer oidTipoActividad) {
        if (oidTipoActividad == null) {
            return null;
        }
        return cargoActividadRepository.findByTipoActividad_OidTipoActividad(oidTipoActividad).stream()
                .map(CargoActividad::getMaxHorasSemana)
                .filter(value -> value != null)
                .max(Float::compare)
                .orElse(null);
    }

    private Map<Integer, Integer> resolverDepartamentosParaValidaciones(Map<Integer, CargoActividad> cargoPorUsuario) {
        if (cargoPorUsuario == null || cargoPorUsuario.isEmpty()) {
            return Map.of();
        }
        Set<Integer> usuariosNecesitanDepartamento = cargoPorUsuario.entrySet().stream()
                .filter(entry -> esCargoTipoProgramaDepartamento(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        if (usuariosNecesitanDepartamento.isEmpty()) {
            return Map.of();
        }
        return obtenerDepartamentosPorUsuarios(usuariosNecesitanDepartamento);
    }

    private Map<Integer, Integer> obtenerDepartamentosPorUsuarios(Set<Integer> usuarios) {
        if (usuarios == null || usuarios.isEmpty()) {
            return Map.of();
        }
        List<UsuarioDepartamento> asignaciones = usuarioDepartamentoRepository.findAllById(usuarios);
        Map<Integer, Integer> departamentoPorUsuario = asignaciones.stream()
                .collect(Collectors.toMap(UsuarioDepartamento::getOidUsuario,
                        ud -> ud.getDepartamento() != null ? ud.getDepartamento().getOidDepartamento() : null));
        for (Integer usuario : usuarios) {
            Integer departamento = departamentoPorUsuario.get(usuario);
            if (departamento == null) {
                throw new ValidacionNegocioException(
                        String.format("El usuario %d no tiene un departamento asignado.", usuario));
            }
        }
        return departamentoPorUsuario;
    }

    private boolean esCargoTipoProgramaDepartamento(CargoActividad cargoActividad) {
        return cargoActividad != null && "PROGRAMADEPARTAMENTO".equalsIgnoreCase(cargoActividad.getTipo());
    }

    private boolean esCargoProyectoInvestigacion(CargoActividad cargoActividad) {
        return cargoActividad != null
                && cargoActividad.getOidCargoActividad() != null
                && CARGOS_PROYECTO_INVESTIGACION.contains(cargoActividad.getOidCargoActividad());
    }

    private void asignarHorasLaborPorUsuario(Actividad actividad,
                                             List<UsuarioActividadCalendario> relaciones,
                                             UsuarioActividadCalendarioDTOResponse dto) {
        if (dto == null || dto.getUsuarios() == null || dto.getUsuarios().isEmpty()) {
            return;
        }
        List<Integer> oidsUsuarios = dto.getUsuarios().stream()
                .map(UsuarioDTO::getOidUsuario)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (oidsUsuarios.isEmpty()) {
            return;
        }

        Map<Integer, HorasLaborDocenteDTO> horasPorUsuario = obtenerResumenHorasLaborUsuarios(oidsUsuarios);
        Map<Integer, HorasLaborDocenteDTO> resumenLocal = construirResumenHorasLocal(actividad, relaciones);

        dto.getUsuarios().forEach(usuarioDto -> {
            Integer oidUsuario = usuarioDto.getOidUsuario();
            if (oidUsuario == null) {
                return;
            }
            HorasLaborDocenteDTO resumen = horasPorUsuario.get(oidUsuario);
            if (resumen == null) {
                resumen = resumenLocal.getOrDefault(oidUsuario, crearResumenHorasVacio());
            }
            usuarioDto.setHorasLaborDocente(resumen);
        });
    }

    @Override
    public ApiResponse<Page<UsuarioActividadCalendarioDTOResponse>> listarActividadesConRelaciones(
            Integer oidCalendario,
            Integer oidDepartamento,
            Integer oidTipoActividad,
            Integer oidEstadoActividad,
            Integer oidUsuarioResponsable,
            Pageable pageable) {
        try {
            return ejecutarListadoActividades(oidCalendario, oidDepartamento, oidTipoActividad, oidEstadoActividad,
                    oidUsuarioResponsable, pageable);
        } catch (UsuarioActividadCalendarioException | ValidacionNegocioException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioActividadCalendarioConsultaException("Error al listar las actividades con sus relaciones.", e);
        }
    }

    private ApiResponse<Page<UsuarioActividadCalendarioDTOResponse>> ejecutarListadoActividades(
            Integer oidCalendario,
            Integer oidDepartamento,
            Integer oidTipoActividad,
            Integer oidEstadoActividad,
            Integer oidUsuarioResponsable,
            Pageable pageable) {

        // Validaciones básicas
        if (oidCalendario == null || oidDepartamento == null) {
            throw new ValidacionNegocioException("Los parámetros oidCalendario y oidDepartamento son obligatorios");
        }
        // 1) Obtener page de ids de actividad (paginado)
        Page<Integer> idsPage = usuarioActividadCalendarioRepository.findDistinctActividadIdsByFilters(
                oidCalendario, oidDepartamento, oidTipoActividad, oidEstadoActividad, oidUsuarioResponsable, pageable);

        List<Integer> actividadIds = idsPage.getContent();
        if (actividadIds.isEmpty()) {
            Page<UsuarioActividadCalendarioDTOResponse> empty = new PageImpl<>(List.of(), pageable, idsPage.getTotalElements());
            return new ApiResponse<>(200, "No se encontraron actividades.", empty);
        }

        // 2) Traer actividades en bloque (mejor que findById en un loop)
        List<Actividad> actividades = actividadRepository.findAllById(actividadIds);
        Map<Integer, Actividad> actividadById = actividades.stream()
                .collect(Collectors.toMap(Actividad::getOidActividad, Function.identity()));

        // 3) Traer todas las relaciones para esas actividades (en un solo query)
        List<UsuarioActividadCalendario> relacionesAll =
                usuarioActividadCalendarioRepository.findByActividadCalendario_Actividad_OidActividadIn(actividadIds);

        // Agrupar relaciones por OID actividad
        Map<Integer, List<UsuarioActividadCalendario>> relacionesPorActividad = relacionesAll.stream()
                .collect(Collectors.groupingBy(rel -> {
                    Actividad act = rel.getActividadCalendario() != null ? rel.getActividadCalendario().getActividad() : null;
                    return act != null ? act.getOidActividad() : null;
                }));

        // 4) Construir DTOs en el mismo orden que actividadIds (mantener paginación ordenada)
        List<UsuarioActividadCalendarioDTOResponse> dtos = new ArrayList<>(actividadIds.size());
        for (Integer oidAct : actividadIds) {
            Actividad actividad = actividadById.get(oidAct);
            if (actividad == null) {
                // fallback: crear stub parcial (evita NPEs)
                actividad = new Actividad();
                actividad.setOidActividad(oidAct);
            }

            // relaciones para esta actividad (puede ser null o vacío)
            List<UsuarioActividadCalendario> relaciones = relacionesPorActividad.getOrDefault(oidAct, List.of());

            // calendario: tomar de la primera relación si existe
            Calendario calendario = relaciones.isEmpty() ? null : relaciones.get(0).getActividadCalendario().getCalendario();

            // atributos EAV (mantengo la llamada actual por actividad)
            List<AtributoDTO> atributos = eavAtributoService.obtenerAtributosPorActividad(actividad);

            // Mapear usando el mapper existente
            UsuarioActividadCalendarioDTOResponse dto = mapper.toResponse(actividad, relaciones, calendario, atributos);
            asignarHorasLaborPorUsuario(actividad, relaciones, dto);

            dtos.add(dto);
        }

        Page<UsuarioActividadCalendarioDTOResponse> resultPage =
                new PageImpl<>(dtos, pageable, idsPage.getTotalElements());

        boolean hasContent = resultPage.hasContent();
        String message = hasContent ? "Actividades encontradas" : "No se encontraron actividades.";
        return new ApiResponse<>(200, message, resultPage);
    }

    @Override
    public ApiResponse<UsuarioActividadCalendarioDTOResponse> obtenerActividadConRelaciones(Integer oidActividad) {
        try {
            Actividad actividad = actividadRepository.findById(oidActividad)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Actividad no encontrada"));
            List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository.findByActividadCalendario_Actividad_OidActividad(oidActividad);
            Calendario calendario = relaciones.isEmpty() ? null : relaciones.get(0).getActividadCalendario().getCalendario();
            List<AtributoDTO> atributos = eavAtributoService.obtenerAtributosPorActividad(actividad);
            UsuarioActividadCalendarioDTOResponse dto = mapper.toResponse(actividad, relaciones, calendario, atributos);
            asignarHorasLaborPorUsuario(actividad, relaciones, dto);
            return new ApiResponse<>(200, "Actividad encontrada", dto);
        } catch (UsuarioActividadCalendarioException | RecursoNoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioActividadCalendarioConsultaException("Error al obtener la actividad con sus relaciones.", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminarActividad(Integer oidActividad) {
        try {
            return ejecutarEliminacionActividad(oidActividad);
        } catch (UsuarioActividadCalendarioException | RecursoNoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioActividadCalendarioEliminacionException("Error al eliminar la actividad y sus relaciones.", e);
        }
    }

    private ApiResponse<Void> ejecutarEliminacionActividad(Integer oidActividad) {
        Optional<Actividad> actividadOpt = actividadRepository.findById(oidActividad);
        if (actividadOpt.isEmpty()) {
            throw new RecursoNoEncontradoException("Actividad no encontrada");
        }

        // Eliminar todas relaciones de usuario asociadas a la actividad (todas las actividadCalendario)
        List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository
                .findByActividadCalendario_Actividad_OidActividad(oidActividad);
        if (!relaciones.isEmpty()) {
            usuarioActividadCalendarioRepository.deleteAll(relaciones);
        }

        // Borrar todas las filas de ACTIVIDADCALENDARIO para esa actividad
        List<ActividadCalendario> actividadCalendarios = actividadCalendarioRepository
                .findByActividad_OidActividad(oidActividad);
        if (!actividadCalendarios.isEmpty()) {
            actividadCalendarioRepository.deleteAll(actividadCalendarios);
        }

        // Eliminar atributos EAV asociados a la actividad
        Actividad actividad = actividadOpt.get();
        ActividadBaseDTO vacio = new ActividadBaseDTO();
        vacio.setAtributos(List.of());
        // Borra los atributos actuales y no agrega ninguno
        eavAtributoService.actualizarAtributosDinamicos(vacio, actividad, Map.of());

        // Finalmente borrar la Actividad
        actividadRepository.deleteById(oidActividad);

        return new ApiResponse<>(204, "Actividad y relaciones eliminadas", null);
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminarRelacion(Integer oidActividad, Integer oidUsuario, Integer oidCalendario) {
        try {
            return ejecutarEliminacionRelacion(oidActividad, oidUsuario, oidCalendario);
        } catch (UsuarioActividadCalendarioException | RecursoNoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioActividadCalendarioEliminacionException("Error al eliminar la relación usuario-actividad-calendario.", e);
        }
    }

    private ApiResponse<Void> ejecutarEliminacionRelacion(Integer oidActividad, Integer oidUsuario, Integer oidCalendario) {
        // buscar el ActividadCalendario correspondiente
        Optional<ActividadCalendario> optAc = actividadCalendarioRepository.findByActividad_OidActividadAndCalendario_Oidcalendario(oidActividad, oidCalendario);
        if (optAc.isEmpty()) {
            throw new RecursoNoEncontradoException("Relación no encontrada para la actividad y calendario especificados");
        }
        ActividadCalendario ac = optAc.get();
        boolean exists = usuarioActividadCalendarioRepository.existsByActividadCalendario_OidActividadCalendarioAndUsuario_OidUsuario(ac.getOidActividadCalendario(), oidUsuario);
        if (!exists) {
            throw new RecursoNoEncontradoException("Relación usuario-actividad no encontrada");
        }

        usuarioActividadCalendarioRepository.deleteByActividadCalendario_OidActividadCalendarioAndUsuario_OidUsuario(ac.getOidActividadCalendario(), oidUsuario);

        // Si quedan 0 relaciones para el actividadCalendario, opcionalmente podríamos borrar la fila actividadCalendario
        List<UsuarioActividadCalendario> restantes = usuarioActividadCalendarioRepository.findByActividadCalendario_OidActividadCalendario(ac.getOidActividadCalendario());
        if (restantes.isEmpty()) {
            actividadCalendarioRepository.deleteById(ac.getOidActividadCalendario());
        }

        return new ApiResponse<>(204, "Relación eliminada correctamente", null);
    }

    // Listar por tipo de actividad
    @Override
    @Transactional
    public ApiResponse<Page<DocenciaDTOResponse>> listarPorTipoDocencia(
            Integer oidCalendario,
            Integer oidDepartamento,
            Integer oidUsuario,
            String tipoContratacion,
            Integer semestre,
            Pageable pageable) {
        if (oidCalendario == null) {
            throw new ValidacionNegocioException("El oidCalendario es obligatorio para listar la docencia.");
        }
        if (oidDepartamento == null) {
            throw new ValidacionNegocioException("El oidDepartamento es obligatorio para listar la docencia.");
        }

        Pageable pageableToUse = pageable != null ? pageable : Pageable.unpaged();
        ContratacionEnum tipoEnum = parseContratacion(tipoContratacion);

        try {
            Specification<Asignacion> specification = Specification
                    .where(conCalendario(oidCalendario))
                    .and(conDepartamento(oidDepartamento));

            if (oidUsuario != null) {
                specification = specification.and(conUsuarioSeleccionado(oidUsuario));
            }
            if (tipoEnum != null) {
                specification = specification.and(conTipoContratacion(tipoEnum));
            }
            if (semestre != null) {
                specification = specification.and(conSemestre(semestre));
            }

            Page<Asignacion> asignaciones = asignacionRepository.findAll(specification, pageableToUse);
            if (asignaciones.isEmpty()) {
                return new ApiResponse<>(200, "No se encontraron actividades de Docencia.", Page.empty(pageableToUse));
            }

            List<Integer> actividadIds = asignaciones.stream()
                    .map(Asignacion::getActividad)
                    .filter(Objects::nonNull)
                    .map(Actividad::getOidActividad)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Integer, List<UsuarioActividadCalendario>> relacionesPorActividad =
                    agruparRelacionesPorActividad(actividadIds);

            Page<DocenciaDTOResponse> page = asignaciones.map(
                    asignacion -> construirDocenciaDto(asignacion, relacionesPorActividad));

            return new ApiResponse<>(200, "Actividades de Docencia encontradas", page);
        } catch (UsuarioActividadCalendarioException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioActividadCalendarioConsultaException("Error al listar las actividades de tipo Docencia.", e);
        }
    }

    private Map<Integer, List<UsuarioActividadCalendario>> agruparRelacionesPorActividad(List<Integer> actividadIds) {
        if (actividadIds == null || actividadIds.isEmpty()) {
            return Map.of();
        }
        return usuarioActividadCalendarioRepository
                .findByActividadCalendario_Actividad_OidActividadIn(actividadIds)
                .stream()
                .collect(Collectors.groupingBy(rel ->
                        rel.getActividadCalendario().getActividad().getOidActividad()));
    }

    private DocenciaDTOResponse construirDocenciaDto(
            Asignacion asignacion,
            Map<Integer, List<UsuarioActividadCalendario>> relacionesPorActividad) {

        Actividad actividad = asignacion.getActividad();
        Necesidad necesidad = asignacion.getNecesidad();
        Calendario calendario = necesidad != null ? necesidad.getCalendario() : null;

        List<UsuarioActividadCalendario> relaciones = actividad != null
                ? relacionesPorActividad.getOrDefault(actividad.getOidActividad(), List.of())
                : List.of();
        List<AtributoDTO> atributos = actividad != null
                ? eavAtributoService.obtenerAtributosPorActividad(actividad)
                : List.of();

        DocenciaDTOResponse dto;
        if (actividad != null) {
            dto = mapper.toDocenciaResponse(actividad, relaciones, calendario, atributos);
        } else {
            dto = new DocenciaDTOResponse();
            dto.setUsuarios(List.of());
            if (calendario != null) {
                dto.setOidCalendario(calendario.getOidcalendario());
                dto.setNombreCalendario(calendario.getAnioCalendario() + " - " + calendario.getNumeroCalendario());
            }
        }

        dto.setAsignacion(asignacionMapper.toResponse(asignacion));
        if (necesidad != null) {
            dto.setNecesidad(necesidadMapper.toResponse(necesidad));
            if (necesidad.getMateria() != null) {
                dto.setMateria(materiaMapper.toResponse(necesidad.getMateria()));
            }
        }
        return dto;
    }

    private Specification<Asignacion> conCalendario(Integer oidCalendario) {
        return (root, query, cb) -> cb.equal(
                root.join("necesidad").join("calendario").get("oidcalendario"),
                oidCalendario);
    }

    private Specification<Asignacion> conDepartamento(Integer oidDepartamento) {
        return (root, query, cb) -> cb.equal(
                root.join("necesidad").join("materia").join("departamento").get("oidDepartamento"),
                oidDepartamento);
    }

    private Specification<Asignacion> conUsuarioSeleccionado(Integer oidUsuario) {
        return (root, query, cb) -> cb.equal(
                root.join("seleccionado").join("usuario").get("oidUsuario"),
                oidUsuario);
    }

    private Specification<Asignacion> conTipoContratacion(ContratacionEnum tipo) {
        return (root, query, cb) -> cb.equal(root.join("seleccionado").get("tipo"), tipo);
    }

    private Specification<Asignacion> conSemestre(Integer semestre) {
        return (root, query, cb) -> cb.equal(
                root.join("necesidad").join("materia").get("semestre"),
                semestre);
    }

    private ContratacionEnum parseContratacion(String tipoContratacion) {
        if (tipoContratacion == null || tipoContratacion.isBlank()) {
            return null;
        }
        String normalizado = normalizarEtiqueta(tipoContratacion);
        return Arrays.stream(ContratacionEnum.values())
                .filter(valor -> normalizarEtiqueta(valor.name()).equals(normalizado)
                        || normalizarEtiqueta(valor.getValor()).equals(normalizado))
                .findFirst()
                .orElseThrow(() -> new ValidacionNegocioException(
                        "Tipo de contratación no válido: " + tipoContratacion));
    }

    private String normalizarEtiqueta(String valor) {
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[\\s_]+", "")
                .toUpperCase();
    }

    @Override
    public ApiResponse<ValidacionHorasCargoDTOResponse> validarCupoUsuariosEnCargo(
            Integer oidTipoActividad,
            Integer oidCargoActividad,
            Integer oidCalendario,
            List<Integer> oidsUsuarios) {
        try {
            return ejecutarValidacionCupo(oidTipoActividad, oidCargoActividad, oidCalendario, oidsUsuarios);
        } catch (UsuarioActividadCalendarioException | RecursoNoEncontradoException | ValidacionNegocioException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioActividadCalendarioValidacionException("Error al validar el cupo de horas para los usuarios.", e);
        }
    }

    private ApiResponse<ValidacionHorasCargoDTOResponse> ejecutarValidacionCupo(
            Integer oidTipoActividad,
            Integer oidCargoActividad,
            Integer oidCalendario,
            List<Integer> oidsUsuarios) {

        if (oidCalendario == null) {
            throw new ValidacionNegocioException("El oidCalendario es obligatorio para la validación.");
        }
        if ((oidTipoActividad == null) && (oidCargoActividad == null)) {
            throw new ValidacionNegocioException("Debe suministrar al menos un tipo de actividad o un cargo para validar el cupo.");
        }
        if (oidsUsuarios == null || oidsUsuarios.isEmpty()) {
            throw new ValidacionNegocioException("Debe suministrar al menos un usuario para validar el cupo.");
        }

        Set<Integer> usuariosEvaluados = oidsUsuarios.stream()
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (usuariosEvaluados.isEmpty()) {
            throw new ValidacionNegocioException("No se recibieron identificadores de usuario válidos.");
        }

        CargoActividad cargoActividad = null;
        if (oidCargoActividad != null) {
            cargoActividad = cargoActividadRepository.findById(oidCargoActividad)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Cargo de actividad no encontrado."));
        }

        TipoActividad tipoActividad = obtenerTipoActividadParaValidacion(oidTipoActividad, cargoActividad);
        if (tipoActividad == null) {
            throw new ValidacionNegocioException("No se pudo determinar un tipo de actividad para validar el cupo.");
        }
        validarConsistenciaCargoYTipo(cargoActividad, tipoActividad);

        Float limiteHoras = cargoActividad != null
                ? cargoActividad.getMaxHorasSemana()
                : obtenerMaximoHorasPorTipoActividad(tipoActividad.getOidTipoActividad());

        if (limiteHoras == null) {
            throw new ValidacionNegocioException("No existe un límite de horas configurado para el cargo o tipo de actividad proporcionado.");
        }

        boolean validarPorDepartamento = cargoActividad != null && esCargoTipoProgramaDepartamento(cargoActividad);
        boolean puedeAsignar = true;
        Float menorDisponible = null;
        Integer usuarioMenorCupo = null;

        if (validarPorDepartamento) {
            Map<Integer, Integer> departamentos = obtenerDepartamentosPorUsuarios(usuariosEvaluados);
            Map<Integer, Float> horasDepartamento = new HashMap<>();
            for (Integer depto : new HashSet<>(departamentos.values())) {
                horasDepartamento.put(depto,
                        calcularHorasPorDepartamentoYCargo(depto, cargoActividad.getOidCargoActividad(), null));
            }

            for (Integer oidUsuario : usuariosEvaluados) {
                Integer depto = departamentos.get(oidUsuario);
                float disponible = limiteHoras - horasDepartamento.getOrDefault(depto, 0f);
                if (disponible <= EPSILON) {
                    puedeAsignar = false;
                }
                float disponibleNormalizado = disponible < 0 ? 0f : disponible;
                if (menorDisponible == null || disponibleNormalizado < menorDisponible) {
                    menorDisponible = disponibleNormalizado;
                    usuarioMenorCupo = oidUsuario;
                }
            }
        } else {
            for (Integer oidUsuario : usuariosEvaluados) {
                float horasAsignadas = cargoActividad != null
                        ? calcularHorasPorUsuarioYCargo(oidUsuario, cargoActividad.getOidCargoActividad(), null)
                        : calcularHorasPorUsuarioYTipoActividad(oidUsuario, tipoActividad.getOidTipoActividad(), null);

                float disponible = limiteHoras - horasAsignadas;
                if (disponible <= EPSILON) {
                    puedeAsignar = false;
                }
                float disponibleNormalizado = disponible < 0 ? 0f : disponible;
                if (menorDisponible == null || disponibleNormalizado < menorDisponible) {
                    menorDisponible = disponibleNormalizado;
                    usuarioMenorCupo = oidUsuario;
                }
            }
        }

        Float semanasMaximas = calcularSemanasMaximasCalendario(oidCalendario);

        ValidacionHorasCargoDTOResponse data = ValidacionHorasCargoDTOResponse.builder()
                .puedeAsignar(puedeAsignar)
                .oidUsuarioMenorCupo(usuarioMenorCupo)
                .horasDisponiblesUsuarioMenorCupo(menorDisponible)
                .horasMaximasCargo(limiteHoras)
                .semanasMaximas(semanasMaximas)
                .build();

        String mensaje = puedeAsignar
                ? "Los usuarios pueden recibir más horas para el cargo solicitado."
                : "Al menos uno de los usuarios alcanzó el límite de horas permitido para el cargo.";

        return new ApiResponse<>(200, mensaje, data);
    }

    private TipoActividad obtenerTipoActividadParaValidacion(Integer oidTipoActividad, CargoActividad cargoActividad) {
        if (cargoActividad != null && cargoActividad.getTipoActividad() != null) {
            return cargoActividad.getTipoActividad();
        }
        if (oidTipoActividad == null) {
            return null;
        }
        return tipoActividadRepository.findById(oidTipoActividad)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de actividad no encontrado."));
    }

    private void validarConsistenciaCargoYTipo(CargoActividad cargoActividad, TipoActividad tipoActividad) {
        if (cargoActividad == null || cargoActividad.getTipoActividad() == null) {
            return;
        }
        Integer tipoCargo = cargoActividad.getTipoActividad().getOidTipoActividad();
        if (!tipoCargo.equals(tipoActividad.getOidTipoActividad())) {
            throw new ValidacionNegocioException("El cargo proporcionado no corresponde al tipo de actividad indicado.");
        }
    }

    private Float calcularSemanasMaximasCalendario(Integer oidCalendario) {
        Optional<Fecha> inicioOpt = fechaRepository
                .findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(oidCalendario, NOMBRE_PERIODO_INICIO);
        Optional<Fecha> finOpt = fechaRepository
                .findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(oidCalendario, NOMBRE_PERIODO_FIN);

        if (inicioOpt.isEmpty() || finOpt.isEmpty()) {
            return null;
        }

        LocalDateTime inicio = resolverMarcaTemporal(inicioOpt.get());
        LocalDateTime fin = resolverMarcaTemporal(finOpt.get());
        if (inicio == null || fin == null || fin.isBefore(inicio)) {
            return null;
        }

        long semanas = ChronoUnit.WEEKS.between(inicio.toLocalDate(), fin.toLocalDate()) + 1;
        return (float) semanas;
    }

    private LocalDateTime resolverMarcaTemporal(Fecha fecha) {
        if (fecha == null) {
            return null;
        }
        if (fecha.getFechaInicial() != null) {
            return fecha.getFechaInicial();
        }
        return fecha.getFechaFin();
    }

    private Map<Integer, Usuario> validarHorasPorContratacionUsuarios(Map<Integer, Float> horasPorUsuario,
                                                                      Calendario calendario,
                                                                      Integer oidActividadActual) {
        if (horasPorUsuario == null || horasPorUsuario.isEmpty()) {
            return Map.of();
        }
        if (calendario == null) {
            throw new ValidacionNegocioException("Debe especificar un calendario válido para la asignación.");
        }
        Map<Integer, Usuario> usuariosProcesados = new HashMap<>();

        for (Map.Entry<Integer, Float> entry : horasPorUsuario.entrySet()) {
            Integer oidUsuario = entry.getKey();
            Usuario usuario = usuarioRepository.findById(oidUsuario)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

            float limite = determinarLimiteHorasPorContratacion(calendario, usuario);
            float horasAsignadas = calcularHorasTotalesUsuario(oidUsuario, oidActividadActual);
            float horasSolicitadas = entry.getValue() == null ? 0f : entry.getValue();
            if ((horasAsignadas + horasSolicitadas) - limite > EPSILON) {
                throw new ValidacionNegocioException(
                        String.format("El usuario %s supera el máximo de %.2f horas permitidas para su contratación.",
                                obtenerDescripcionUsuario(usuario), limite));
            }
            usuariosProcesados.put(oidUsuario, usuario);
        }

        return usuariosProcesados;
    }

    private float determinarLimiteHorasPorContratacion(Calendario calendario, Usuario usuario) {
        UsuarioDetalle detalle = usuario.getUsuarioDetalle();
        String contratacion = detalle != null ? detalle.getContratacion() : null;
        if (contratacion == null || contratacion.isBlank()) {
            throw new ValidacionNegocioException(
                    String.format("El usuario %s no tiene configurado el tipo de contratación.", obtenerDescripcionUsuario(usuario)));
        }
        String tipoNormalizado = normalizarEtiqueta(contratacion);
        boolean esPlanta = "PLANTA".equals(tipoNormalizado);
        boolean esOcasional = "OCASIONAL".equals(tipoNormalizado) || "OCASIONALES".equals(tipoNormalizado);

        Float limite;
        if (esPlanta) {
            limite = calendario.getHorasPlanta();
        } else if (esOcasional) {
            limite = calendario.getHorasOcasionales();
        } else {
            throw new ValidacionNegocioException(
                    String.format("Solo usuarios con contratación PLANTA u OCASIONAL pueden asignarse a actividades. Usuario: %s.",
                            obtenerDescripcionUsuario(usuario)));
        }

        float maximoBase = limite != null ? limite : HORAS_DEFAULT_CONTRATACION;
        boolean medioTiempo = esPlanta || esOcasional
                ? esDedicacionMedioTiempo(detalle != null ? detalle.getDedicacion() : null)
                : false;
        return medioTiempo ? maximoBase / 2f : maximoBase;
    }

    private String obtenerDescripcionUsuario(Usuario usuario) {
        String identificacion = usuario.getIdentificacion() != null ? usuario.getIdentificacion() : "";
        String nombres = usuario.getNombres() != null ? usuario.getNombres() : "";
        String apellidos = usuario.getApellidos() != null ? usuario.getApellidos() : "";
        return String.format("%s %s %s", identificacion, nombres, apellidos).trim();
    }

    private boolean esDedicacionMedioTiempo(String dedicacion) {
        if (dedicacion == null || dedicacion.isBlank()) {
            return false;
        }
        String dedicacionNormalizada = normalizarEtiqueta(dedicacion);
        return "MEDIOTIEMPO".equals(dedicacionNormalizada);
    }

    private Map<Integer, HorasLaborDocenteDTO> obtenerResumenHorasLaborUsuarios(List<Integer> oidsUsuarios) {
        if (oidsUsuarios == null || oidsUsuarios.isEmpty()) {
            return Map.of();
        }
        List<UsuarioHorasPorTipoActividadProjection> proyecciones =
                usuarioActividadCalendarioRepository.sumarHorasPorUsuariosYTipoActividad(oidsUsuarios);
        return construirHorasLaborPorUsuario(proyecciones);
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
        String normalizado = normalizarEtiqueta(nombreTipo);
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

    private Map<Integer, HorasLaborDocenteDTO> construirResumenHorasLocal(
            Actividad actividad,
            List<UsuarioActividadCalendario> relaciones) {
        if (relaciones == null || relaciones.isEmpty()) {
            return Map.of();
        }
        Map<Integer, HorasLaborDocenteDTO> resultado = new HashMap<>();
        for (UsuarioActividadCalendario relacion : relaciones) {
            if (relacion == null || relacion.getUsuario() == null) {
                continue;
            }
            Integer oidUsuario = relacion.getUsuario().getOidUsuario();
            if (oidUsuario == null) {
                continue;
            }
            HorasLaborDocenteDTO parcial = construirResumenHorasPorRelacion(actividad, relacion);
            if (parcial == null) {
                continue;
            }
            resultado.merge(oidUsuario, parcial, this::combinarResumenHoras);
        }
        return resultado;
    }

    private HorasLaborDocenteDTO combinarResumenHoras(HorasLaborDocenteDTO existente, HorasLaborDocenteDTO adicional) {
        if (existente == null) {
            return adicional;
        }
        if (adicional == null) {
            return existente;
        }
        HorasLaborDocenteDTO combinado = new HorasLaborDocenteDTO();
        Map<String, Float> asignadas = new HashMap<>();
        if (existente.getHorasAsignadasPorTipoActividad() != null) {
            asignadas.putAll(existente.getHorasAsignadasPorTipoActividad());
        }
        if (adicional.getHorasAsignadasPorTipoActividad() != null) {
            adicional.getHorasAsignadasPorTipoActividad()
                    .forEach((k, v) -> asignadas.merge(k, v, Float::sum));
        }
        combinado.setHorasAsignadasPorTipoActividad(asignadas);

        Map<String, Float> disponibles = new HashMap<>();
        for (Map.Entry<String, Float> entry : asignadas.entrySet()) {
            float disponible = HorasLaborDocenteDTO.HORAS_MAX_SEMANA - entry.getValue();
            if (disponible < 0f) {
                disponible = 0f;
            }
            disponibles.put(entry.getKey(), disponible);
        }
        combinado.setHorasDisponiblesPorTipoActividad(disponibles);

        float totalAsignadas =
                (existente.getTotalHorasAsignadas() != null ? existente.getTotalHorasAsignadas() : 0f)
                        + (adicional.getTotalHorasAsignadas() != null ? adicional.getTotalHorasAsignadas() : 0f);
        combinado.setTotalHorasAsignadas(totalAsignadas);
        float totalDisponibles = HorasLaborDocenteDTO.HORAS_MAX_SEMANA - totalAsignadas;
        if (totalDisponibles < 0f) {
            totalDisponibles = 0f;
        }
        combinado.setTotalHorasDisponibles(totalDisponibles);
        return combinado;
    }

    private HorasLaborDocenteDTO construirResumenHorasPorRelacion(Actividad actividad, UsuarioActividadCalendario relacion) {
        if (relacion == null) {
            return null;
        }
        HorasLaborDocenteDTO dto = new HorasLaborDocenteDTO();

        String nombreTipo = actividad != null && actividad.getTipoActividad() != null
                ? actividad.getTipoActividad().getNombre()
                : "DESCONOCIDO";
        Integer oidTipoActividad = actividad != null && actividad.getTipoActividad() != null
                ? actividad.getTipoActividad().getOidTipoActividad()
                : null;

        float horasAsignadas = relacion.getHorasActividad() != null ? relacion.getHorasActividad() : 0f;

        float limite = HorasLaborDocenteDTO.HORAS_MAX_SEMANA;
        CargoActividad cargo = relacion.getCargoActividad();
        if (cargo != null && cargo.getMaxHorasSemana() != null) {
            limite = cargo.getMaxHorasSemana();
        } else if (oidTipoActividad != null) {
            Float maximo = obtenerMaximoHorasPorTipoActividad(oidTipoActividad);
            if (maximo != null) {
                limite = maximo;
            }
        }

        float disponible = limite - horasAsignadas;
        if (disponible < 0f) {
            disponible = 0f;
        }

        dto.setHorasAsignadasPorTipoActividad(Map.of(nombreTipo, horasAsignadas));
        dto.setHorasDisponiblesPorTipoActividad(Map.of(nombreTipo, disponible));
        dto.setTotalHorasAsignadas(horasAsignadas);
        float totalDisponibles = HORAS_DEFAULT_CONTRATACION - horasAsignadas;
        if (totalDisponibles < 0f) {
            totalDisponibles = 0f;
        }
        dto.setTotalHorasDisponibles(totalDisponibles);
        return dto;
    }
}
