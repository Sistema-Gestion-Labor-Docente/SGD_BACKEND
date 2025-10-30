package co.edu.unicauca.sgd.api.service.actividad.laborDocente.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.ActividadCalendario;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.CargoActividad;
import co.edu.unicauca.sgd.api.domain.EavAtributo;
import co.edu.unicauca.sgd.api.domain.EstadoActividad;
import co.edu.unicauca.sgd.api.domain.TipoActividad;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioActividadCalendario;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.AtributoDTO;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.CargoActividadDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.DocenciaDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTOResponse;
import co.edu.unicauca.sgd.api.exception.AsignacionHorasExcedidasException;
import co.edu.unicauca.sgd.api.exception.RecursoNoEncontradoException;
import co.edu.unicauca.sgd.api.exception.ValidacionNegocioException;
import co.edu.unicauca.sgd.api.mapper.UsuarioActividadCalendarioMapper;
import co.edu.unicauca.sgd.api.repository.ActividadCalendarioRepository;
import co.edu.unicauca.sgd.api.repository.ActividadRepository;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.CargoActividadRepository;
import co.edu.unicauca.sgd.api.repository.EavAtributoRepository;
import co.edu.unicauca.sgd.api.repository.EstadoActividadRepository;
import co.edu.unicauca.sgd.api.repository.TipoActividadRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioActividadCalendarioRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioRepository;
import co.edu.unicauca.sgd.api.repository.projection.ActividadUsuariosProjection;
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
    private final UsuarioActividadCalendarioMapper mapper;
    private final CargoActividadRepository cargoActividadRepository;
    private final EstadoActividadRepository estadoActividadRepository;
    private final EavAtributoService eavAtributoService;
    private final EavAtributoRepository eavAtributoRepository;
    private final TipoActividadRepository tipoActividadRepository;
    private final ObjectMapper objectMapper;

    private static final float EPSILON = 0.0001f;

    public UsuarioActividadCalendarioServiceImpl(
            ActividadRepository actividadRepository,
            UsuarioRepository usuarioRepository,
            CalendarioRepository calendarioRepository,
            ActividadCalendarioRepository actividadCalendarioRepository,
            UsuarioActividadCalendarioRepository usuarioActividadCalendarioRepository,
            UsuarioActividadCalendarioMapper mapper,
            CargoActividadRepository cargoActividadRepository,
            EstadoActividadRepository estadoActividadRepository,
            EavAtributoService eavAtributoService,
            EavAtributoRepository eavAtributoRepository,
            TipoActividadRepository tipoActividadRepository,
            ObjectMapper objectMapper) {
        this.actividadRepository = actividadRepository;
        this.usuarioRepository = usuarioRepository;
        this.calendarioRepository = calendarioRepository;
        this.actividadCalendarioRepository = actividadCalendarioRepository;
        this.usuarioActividadCalendarioRepository = usuarioActividadCalendarioRepository;
        this.mapper = mapper;
        this.cargoActividadRepository = cargoActividadRepository;
        this.estadoActividadRepository = estadoActividadRepository;
        this.eavAtributoService = eavAtributoService;
        this.eavAtributoRepository = eavAtributoRepository;
        this.tipoActividadRepository = tipoActividadRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ApiResponse<UsuarioActividadCalendarioDTOResponse> crearActividadConRelaciones(@Valid UsuarioActividadCalendarioDTORequest request) {

        CargoActividad cargoActividad = null;
        if (request.getOidCargoActividad() != null) {
            cargoActividad = cargoActividadRepository.findById(request.getOidCargoActividad())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Cargo de actividad no encontrado"));
        }

        TipoActividad tipoActividad = resolverTipoActividad(cargoActividad, request, null);

        EstadoActividad estadoActividad = estadoActividadRepository.findById(request.getOidEstadoActividad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Estado de actividad no encontrado"));

        validarMaximoHorasUsuarios(
                request.getOidsUsuarios(),
                request.getHoras(),
                cargoActividad,
                tipoActividad,
                null
        );

        // 1. Crear Actividad
        Actividad actividad = new Actividad();
        actividad.setTipoActividad(tipoActividad);
        actividad.setEstadoActividad(estadoActividad);
        actividad.setNombreActividad(request.getNombreActividad());
        actividad.setHoras(request.getHoras());
        actividad.setSemanas(request.getSemanas());
        actividad = actividadRepository.save(actividad);

        final Actividad actividadFinal = actividad;

        // 2. Obtener Calendario y crear/obtener ActividadCalendario
        Calendario calendario = calendarioRepository.findById(request.getOidCalendario())
                .orElseThrow(() -> new RecursoNoEncontradoException("Calendario no encontrado"));

        ActividadCalendario actividadCalendario = actividadCalendarioRepository
                .findByActividad_OidActividadAndCalendario_Oidcalendario(actividad.getOidActividad(), calendario.getOidcalendario())
                .orElseGet(() -> {
                    ActividadCalendario ac = new ActividadCalendario();
                    ac.setActividad(actividadFinal);
                    ac.setCalendario(calendario);
                    ac.setUsuarioCreacion("system");
                    return actividadCalendarioRepository.save(ac);
                });

        // Si la actividadCalendario ya existía pero el cargo es distinto, actualizamos
        if (cargoActividad != null || actividadCalendario.getCargoActividad() != null) {
            actividadCalendario.setCargoActividad(cargoActividad);
            actividadCalendario = actividadCalendarioRepository.save(actividadCalendario);
        }

        // 3. Crear relaciones UsuarioActividadCalendario apuntando a actividadCalendario
        List<Integer> usuariosSolicitados = request.getOidsUsuarios() == null ? List.of() : request.getOidsUsuarios();
        for (Integer oidUsuario : usuariosSolicitados) {
            Usuario usuario = usuarioRepository.findById(oidUsuario)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
            // No crear duplicados
            boolean exists = usuarioActividadCalendarioRepository.existsByActividadCalendario_OidActividadCalendarioAndUsuario_OidUsuario(actividadCalendario.getOidActividadCalendario(), oidUsuario);
            if (!exists) {
                UsuarioActividadCalendario relacion = new UsuarioActividadCalendario();
                relacion.setUsuario(usuario);
                relacion.setActividadCalendario(actividadCalendario);
                relacion.setUsuarioCreacion("system");
                usuarioActividadCalendarioRepository.save(relacion);
            }
        }

        // 4. Guardar atributos EAV usando el servicio central (igual que antes)
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

        // 5. Devolver DTOResponse (solo relaciones del actividadCalendario creado)
        List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository.findByActividadCalendario_OidActividadCalendario(actividadCalendario.getOidActividadCalendario());
        UsuarioActividadCalendarioDTOResponse dto = mapper.toResponse(
                actividad, relaciones, calendario,
                (request.getAtributos() != null) ? request.getAtributos().stream()
                        .map(a -> new AtributoDTO(a.getNombre(), a.getValor()))
                        .collect(Collectors.toList()) : List.of()
        );

        return new ApiResponse<>(201, "Actividad creada con relaciones", dto);
    }

    @Override
    @Transactional
    public ApiResponse<UsuarioActividadCalendarioDTOResponse> actualizarActividadConRelaciones(Integer oidActividad, @Valid UsuarioActividadCalendarioDTORequest request) {        
        Actividad actividad = actividadRepository.findById(oidActividad)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad no encontrada"));

        CargoActividad cargoActividad = null;
        if (request.getOidCargoActividad() != null) {
            cargoActividad = cargoActividadRepository.findById(request.getOidCargoActividad())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Cargo de actividad no encontrado"));
        }

        EstadoActividad estadoActividad = estadoActividadRepository.findById(request.getOidEstadoActividad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Estado de actividad no encontrado"));

        TipoActividad tipoActividad = resolverTipoActividad(cargoActividad, request, actividad);

        validarMaximoHorasUsuarios(
                request.getOidsUsuarios(),
                request.getHoras(),
                cargoActividad,
                tipoActividad,
                actividad.getOidActividad()
        );

        // Actualizar datos base de Actividad
        actividad.setTipoActividad(tipoActividad);
        actividad.setEstadoActividad(estadoActividad);
        actividad.setNombreActividad(request.getNombreActividad());
        actividad.setHoras(request.getHoras());
        actividad.setSemanas(request.getSemanas());
        actividad = actividadRepository.save(actividad);

        final Actividad actividadFinal = actividad;

        // Obtener/crear ActividadCalendario destino (puede ser el mismo o uno nuevo si cambió el calendario)
        Calendario calendarioDestino = calendarioRepository.findById(request.getOidCalendario())
                .orElseThrow(() -> new RecursoNoEncontradoException("Calendario no encontrado"));

        ActividadCalendario actividadCalendarioDestino = actividadCalendarioRepository
                .findByActividad_OidActividadAndCalendario_Oidcalendario(actividad.getOidActividad(), calendarioDestino.getOidcalendario())
                .orElseGet(() -> {
                    ActividadCalendario ac = new ActividadCalendario();
                    ac.setActividad(actividadFinal);
                    ac.setCalendario(calendarioDestino);
                    ac.setUsuarioCreacion("system");
                    return actividadCalendarioRepository.save(ac);
                });


        // Si existe pero cargo distinto, actualizar
        if (cargoActividad != null || actividadCalendarioDestino.getCargoActividad() != null) {
            actividadCalendarioDestino.setCargoActividad(cargoActividad);
            actividadCalendarioDestino = actividadCalendarioRepository.save(actividadCalendarioDestino);
        }

        // Sincronizar relaciones de usuarios:
        //  - obtener relaciones existentes para la actividad en el calendario destino
        List<UsuarioActividadCalendario> relacionesExistentes = usuarioActividadCalendarioRepository.findByActividadCalendario_OidActividadCalendario(actividadCalendarioDestino.getOidActividadCalendario());
        Set<Integer> existentesOids = relacionesExistentes.stream()
                .map(r -> r.getUsuario().getOidUsuario())
                .collect(Collectors.toSet());

        Set<Integer> solicitados = new HashSet<>(request.getOidsUsuarios() == null ? List.of() : request.getOidsUsuarios());

        // Añadir nuevos
        for (Integer oidUsuario : solicitados) {
            if (!existentesOids.contains(oidUsuario)) {
                Usuario usuario = usuarioRepository.findById(oidUsuario)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
                UsuarioActividadCalendario relacion = new UsuarioActividadCalendario();
                relacion.setUsuario(usuario);
                relacion.setActividadCalendario(actividadCalendarioDestino);
                relacion.setUsuarioCreacion("system");
                usuarioActividadCalendarioRepository.save(relacion);
            }
        }

        // Eliminar relaciones que ya no están solicitadas (solo en el calendario destino)
        for (UsuarioActividadCalendario exist : relacionesExistentes) {
            Integer oidUsuarioExistente = exist.getUsuario().getOidUsuario();
            if (!solicitados.contains(oidUsuarioExistente)) {
                usuarioActividadCalendarioRepository.deleteByActividadCalendario_OidActividadCalendarioAndUsuario_OidUsuario(actividadCalendarioDestino.getOidActividadCalendario(), oidUsuarioExistente);
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

        return new ApiResponse<>(200, "Actividad actualizada con relaciones", dto);
    }

    private TipoActividad resolverTipoActividad(CargoActividad cargoActividad, UsuarioActividadCalendarioDTORequest request, Actividad actividadActual) {
        if (cargoActividad != null && cargoActividad.getTipoActividad() != null) {
            return cargoActividad.getTipoActividad();
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

    private void validarMaximoHorasUsuarios(List<Integer> oidsUsuarios,
                                            Float horasActividad,
                                            CargoActividad cargoActividad,
                                            TipoActividad tipoActividad,
                                            Integer oidActividadActual) {
        if (oidsUsuarios == null || oidsUsuarios.isEmpty()) {
            return;
        }

        float horasSolicitadas = horasActividad == null ? 0f : horasActividad;
        Set<Integer> usuariosUnicos = new HashSet<>(oidsUsuarios);

        Float limiteHorasPorTipo = null;
        TipoActividad tipoActividadEvaluado = tipoActividad;
        if (tipoActividadEvaluado == null) {
            throw new ValidacionNegocioException("No se pudo determinar el tipo de actividad para validar horas.");
        }
        if (cargoActividad == null) {
            limiteHorasPorTipo = obtenerMaximoHorasPorTipoActividad(tipoActividadEvaluado.getOidTipoActividad());
        }

        for (Integer oidUsuario : usuariosUnicos) {
            float horasAsignadas = cargoActividad != null
                    ? calcularHorasPorUsuarioYCargo(oidUsuario, cargoActividad.getOidCargoActividad(), oidActividadActual)
                    : calcularHorasPorUsuarioYTipoActividad(oidUsuario, tipoActividadEvaluado.getOidTipoActividad(), oidActividadActual);

            float limite = cargoActividad != null
                    ? (cargoActividad.getMaxHorasSemana() == null ? Float.MAX_VALUE : cargoActividad.getMaxHorasSemana())
                    : (limiteHorasPorTipo == null ? Float.MAX_VALUE : limiteHorasPorTipo);

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
                .findByUsuario_OidUsuarioAndActividadCalendario_CargoActividad_OidCargoActividad(oidUsuario, oidCargoActividad);
        return sumarHorasRelacionadas(relaciones, oidActividadActual);
    }

    private float calcularHorasPorUsuarioYTipoActividad(Integer oidUsuario, Integer oidTipoActividad, Integer oidActividadActual) {
        if (oidUsuario == null || oidTipoActividad == null) {
            return 0f;
        }
        List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository
                .findByUsuario_OidUsuarioAndActividadCalendario_Actividad_TipoActividad_OidTipoActividad(oidUsuario, oidTipoActividad);
        return sumarHorasRelacionadas(relaciones, oidActividadActual);
    }

    private float sumarHorasRelacionadas(List<UsuarioActividadCalendario> relaciones, Integer oidActividadActual) {
        if (relaciones == null || relaciones.isEmpty()) {
            return 0f;
        }
        Set<Integer> actividadesProcesadas = new HashSet<>();
        double total = 0d;
        for (UsuarioActividadCalendario relacion : relaciones) {
            if (relacion == null || relacion.getActividadCalendario() == null) {
                continue;
            }
            Actividad actividad = relacion.getActividadCalendario().getActividad();
            if (actividad == null) {
                continue;
            }
            Integer oidActividad = actividad.getOidActividad();
            if (oidActividad != null && oidActividad.equals(oidActividadActual)) {
                continue;
            }
            if (oidActividad != null && !actividadesProcesadas.add(oidActividad)) {
                continue;
            }
            Float horas = actividad.getHoras();
            if (horas != null) {
                total += horas;
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

    @Override
    public ApiResponse<Page<UsuarioActividadCalendarioDTOResponse>> listarActividadesConRelaciones(
            Integer oidCalendario,
            Integer oidDepartamento,
            Integer oidTipoActividad,
            Pageable pageable) {

         // Validaciones básicas
        if (oidCalendario == null || oidDepartamento == null) {
            throw new ValidacionNegocioException("Los par�metros oidCalendario y oidDepartamento son obligatorios");
        }
        // 1) Obtener page de ids de actividad (paginado)
        Page<Integer> idsPage = usuarioActividadCalendarioRepository.findDistinctActividadIdsByFilters(
                oidCalendario, oidDepartamento, oidTipoActividad, pageable);

        List<Integer> actividadIds = idsPage.getContent();
        if (actividadIds.isEmpty()) {
            Page<UsuarioActividadCalendarioDTOResponse> empty = new PageImpl<>(List.of(), pageable, idsPage.getTotalElements());
            return new ApiResponse<>(200, "Actividades encontradas", empty);
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

            // Si necesitas colocar cargo desde actividadCalendario (si no viene por mapper)
            // se puede rellenar aquí igual que antes (cargoActividadRepository.findById(...))

            dtos.add(dto);
        }

        Page<UsuarioActividadCalendarioDTOResponse> resultPage =
                new PageImpl<>(dtos, pageable, idsPage.getTotalElements());

        return new ApiResponse<>(200, "Actividades encontradas", resultPage);
    }

    @Override
    public ApiResponse<UsuarioActividadCalendarioDTOResponse> obtenerActividadConRelaciones(Integer oidActividad) {
        Actividad actividad = actividadRepository.findById(oidActividad)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad no encontrada"));
        List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository.findByActividadCalendario_Actividad_OidActividad(oidActividad);
        Calendario calendario = relaciones.isEmpty() ? null : relaciones.get(0).getActividadCalendario().getCalendario();
        List<AtributoDTO> atributos = eavAtributoService.obtenerAtributosPorActividad(actividad);
        UsuarioActividadCalendarioDTOResponse dto = mapper.toResponse(actividad, relaciones, calendario, atributos);
        return new ApiResponse<>(200, "Actividad encontrada", dto);
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminarActividad(Integer oidActividad) {
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
        // buscar el ActividadCalendario correspondiente
        Optional<ActividadCalendario> optAc = actividadCalendarioRepository.findByActividad_OidActividadAndCalendario_Oidcalendario(oidActividad, oidCalendario);
        if (optAc.isEmpty()) {
            throw new RecursoNoEncontradoException("Relaci�n no encontrada para la actividad y calendario especificados");
        }
        ActividadCalendario ac = optAc.get();
        boolean exists = usuarioActividadCalendarioRepository.existsByActividadCalendario_OidActividadCalendarioAndUsuario_OidUsuario(ac.getOidActividadCalendario(), oidUsuario);
        if (!exists) {
            throw new RecursoNoEncontradoException("Relaci�n usuario-actividad no encontrada");
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
    public ApiResponse<Page<DocenciaDTOResponse>> listarPorTipoDocencia(Pageable pageable) {
        Page<Actividad> actividades = actividadRepository.findByTipoActividad_Nombre("Docencia", pageable);

        Page<DocenciaDTOResponse> page = actividades.map(actividad -> {
            List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository.findByActividadCalendario_Actividad_OidActividad(actividad.getOidActividad());
            Calendario calendario = relaciones.isEmpty() ? null : relaciones.get(0).getActividadCalendario().getCalendario();
            List<AtributoDTO> atributos = eavAtributoService.obtenerAtributosPorActividad(actividad);
            return mapper.toDocenciaResponse(actividad, relaciones, calendario, atributos);
        });

        return new ApiResponse<>(200, "Actividades de Docencia encontradas", page);
    }

}












