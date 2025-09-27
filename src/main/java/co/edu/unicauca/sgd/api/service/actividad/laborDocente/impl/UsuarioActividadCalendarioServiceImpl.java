package co.edu.unicauca.sgd.api.service.actividad.laborDocente.impl;

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
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioActividadCalendario;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.AtributoDTO;
import co.edu.unicauca.sgd.api.dto.UsuarioDTO;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.CargoActividadDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.DocenciaDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTOResponse;
import co.edu.unicauca.sgd.api.mapper.UsuarioActividadCalendarioMapper;
import co.edu.unicauca.sgd.api.repository.ActividadCalendarioRepository;
import co.edu.unicauca.sgd.api.repository.ActividadRepository;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.CargoActividadRepository;
import co.edu.unicauca.sgd.api.repository.EavAtributoRepository;
import co.edu.unicauca.sgd.api.repository.EstadoActividadRepository;
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
    private final ObjectMapper objectMapper;

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
        this.objectMapper = objectMapper;
    }

    public ApiResponse<UsuarioActividadCalendarioDTOResponse> crearActividadConRelaciones(@Valid UsuarioActividadCalendarioDTORequest request) {

        CargoActividad cargoActividad = cargoActividadRepository.findById(request.getOidCargoActividad())
                .orElseThrow(() -> new RuntimeException("Cargo de actividad no encontrado"));

        EstadoActividad estadoActividad = estadoActividadRepository.findById(request.getOidEstadoActividad())
                .orElseThrow(() -> new RuntimeException("Estado de actividad no encontrado"));

        // 1. Crear Actividad
        Actividad actividad = new Actividad();
        actividad.setTipoActividad(cargoActividad.getTipoActividad());
        actividad.setEstadoActividad(estadoActividad);
        actividad.setNombreActividad(request.getNombreActividad());
        actividad.setHoras(request.getHoras());
        actividad.setSemanas(request.getSemanas());
        actividad = actividadRepository.save(actividad);

        final Actividad actividadFinal = actividad;

        // 2. Obtener Calendario y crear/obtener ActividadCalendario
        Calendario calendario = calendarioRepository.findById(request.getOidCalendario())
                .orElseThrow(() -> new RuntimeException("Calendario no encontrado"));

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
        if (actividadCalendario.getCargoActividad() == null || !actividadCalendario.getCargoActividad().getOidCargoActividad().equals(cargoActividad.getOidCargoActividad())) {
            actividadCalendario.setCargoActividad(cargoActividad);
            actividadCalendario = actividadCalendarioRepository.save(actividadCalendario);
        }

        // 3. Crear relaciones UsuarioActividadCalendario apuntando a actividadCalendario
        for (Integer oidUsuario : request.getOidsUsuarios()) {
            Usuario usuario = usuarioRepository.findById(oidUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
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
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

        CargoActividad cargoActividad = cargoActividadRepository.findById(request.getOidCargoActividad())
                .orElseThrow(() -> new RuntimeException("Cargo de actividad no encontrado"));

        EstadoActividad estadoActividad = estadoActividadRepository.findById(request.getOidEstadoActividad())
                .orElseThrow(() -> new RuntimeException("Estado de actividad no encontrado"));

        // Actualizar datos base de Actividad
        actividad.setTipoActividad(cargoActividad.getTipoActividad());
        actividad.setEstadoActividad(estadoActividad);
        actividad.setNombreActividad(request.getNombreActividad());
        actividad.setHoras(request.getHoras());
        actividad.setSemanas(request.getSemanas());
        actividad = actividadRepository.save(actividad);

        final Actividad actividadFinal = actividad;

        // Obtener/crear ActividadCalendario destino (puede ser el mismo o uno nuevo si cambió el calendario)
        Calendario calendarioDestino = calendarioRepository.findById(request.getOidCalendario())
                .orElseThrow(() -> new RuntimeException("Calendario no encontrado"));

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
        if (actividadCalendarioDestino.getCargoActividad() == null || !actividadCalendarioDestino.getCargoActividad().getOidCargoActividad().equals(cargoActividad.getOidCargoActividad())) {
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
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
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

    @Override
    public ApiResponse<Page<UsuarioActividadCalendarioDTOResponse>> listarActividadesConRelaciones(
            Integer oidCalendario,
            Integer oidDepartamento,
            Integer oidTipoActividad,
            Pageable pageable) {

        // Validaciones básicas
        if (oidCalendario == null || oidDepartamento == null || oidTipoActividad == null) {
            return new ApiResponse<>(400, "Los parámetros oidCalendario, oidDepartamento y oidTipoActividad son obligatorios", Page.empty());
        }

        // Ejecuta la consulta nativa paginada (single function)
        Page<ActividadUsuariosProjection> pageProj = usuarioActividadCalendarioRepository.findActividadesWithUsersByFilters(
                oidCalendario, oidDepartamento, oidTipoActividad, pageable);

        // Convertir cada proyección a DTOResponse
        List<UsuarioActividadCalendarioDTOResponse> dtos = pageProj.getContent().stream().map(proj -> {
            // 1) Construir Actividad domain-lite (necesitamos al menos el OID para pedir atributos)
            Actividad actividad = actividadRepository.findById(proj.getOidActividad())
                    .orElseGet(() -> {
                        // fallback parcial: crear objeto actividad mínimo (sin persistir)
                        Actividad a = new Actividad();
                        a.setOidActividad(proj.getOidActividad());
                        a.setNombreActividad(proj.getNombreActividad());
                        a.setHoras(proj.getHoras() == null ? null : proj.getHoras().floatValue());
                        a.setSemanas(proj.getSemanas() == null ? null : proj.getSemanas().floatValue());
                        a.setIdLaborDocente(proj.getIdLaborDocente());
                        a.setInformeEjecutivo(proj.getInformeEjecutivo());
                        return a;
                    });

            // 2) Obtener atributos EAV por actividad (posible N+1, discuss later)
            List<AtributoDTO> atributos = eavAtributoService.obtenerAtributosPorActividad(actividad);

            // 3) Parsear usuariosJson
            List<UsuarioDTO> usuarios = List.of();
            try {
                String usuariosJson = proj.getUsuariosJson();
                if (usuariosJson != null && !usuariosJson.isBlank()) {
                    // Mapear JSON array a List<Map> y luego a UsuarioDTO
                    var node = objectMapper.readTree(usuariosJson);
                    if (node.isArray()) {
                        usuarios = StreamSupport.stream(node.spliterator(), false)
                            .map(n -> {
                                UsuarioDTO u = new UsuarioDTO();
                                u.setOidUsuario(n.path("oidUsuario").isInt() ? n.path("oidUsuario").asInt() : null);
                                u.setIdentificacion(n.path("identificacion").isTextual() ? n.path("identificacion").asText() : null);
                                u.setNombres(n.path("nombres").isTextual() ? n.path("nombres").asText() : null);
                                u.setApellidos(n.path("apellidos").isTextual() ? n.path("apellidos").asText() : null);
                                // NOTA: llenamos departamento/roles si los necesitas; aquí solo datos básicos
                                return u;
                            })
                            .collect(Collectors.toList());
                    }
                }
            } catch (Exception ex) {
                // no queremos fallar la consulta por un parseo; fallback a lista vacía
                usuarios = List.of();
            }

            // 4) Construir DTOResponse usando mapper existente pero adaptando inputs
            // necesitamos construir un objeto Calendario parcial (solo OID)
            Calendario calendario = new Calendario();
            calendario.setOidcalendario(proj.getOidCalendario());

            // Para compatibilidad con mapper.toResponse(Actividad, relaciones, calendario, atributos)
            // construimos relaciones "simples" en memoria: convertir usuarios a UsuarioActividadCalendario con actividadCalendario stub
            ActividadCalendario acStub = new ActividadCalendario();
            acStub.setOidActividadCalendario(proj.getOidActividadCalendario());
            acStub.setCargoActividad(null); // si quieres el cargo, puedes buscarlo luego por proj.getOidCargoActividad()

            // build fake relaciones list
            List<UsuarioActividadCalendario> relaciones = usuarios.stream().map(uDto -> {
                Usuario u = new Usuario();
                u.setOidUsuario(uDto.getOidUsuario());
                u.setIdentificacion(uDto.getIdentificacion());
                u.setNombres(uDto.getNombres());
                u.setApellidos(uDto.getApellidos());
                UsuarioActividadCalendario rel = new UsuarioActividadCalendario();
                rel.setUsuario(u);
                rel.setActividadCalendario(acStub);
                return rel;
            }).collect(Collectors.toList());

            // Mapear con el mapper existente (usa primer relación para cargo si existe)
            UsuarioActividadCalendarioDTOResponse dto = mapper.toResponse(actividad, relaciones, calendario, atributos);

            // Si necesitas incluir info de cargo desde proj.getOidCargoActividad, puedes recuperarlo:
            if (proj.getOidCargoActividad() != null) {
                cargoActividadRepository.findById(proj.getOidCargoActividad()).ifPresent(cargo -> {
                    // mapper ya habrá colocado cargo si estaba en actividadCalendario; sino actualizamos el campo en DTO
                    if (dto.getActividad() != null && dto.getActividad().getCargoActividad() == null) {
                        var cargoDto = new CargoActividadDTOResponse();
                        cargoDto.setOidCargoActividad(cargo.getOidCargoActividad());
                        cargoDto.setNombre(cargo.getNombre());
                        cargoDto.setMaxHorasSemana(cargo.getMaxHorasSemana());
                        dto.getActividad().setCargoActividad(cargoDto);
                    }
                });
            }

            return dto;
        }).collect(Collectors.toList());

        Page<UsuarioActividadCalendarioDTOResponse> resultPage = new PageImpl<>(dtos, pageable, pageProj.getTotalElements());
        return new ApiResponse<>(200, "Actividades encontradas", resultPage);
    }

    @Override
    public ApiResponse<UsuarioActividadCalendarioDTOResponse> obtenerActividadConRelaciones(Integer oidActividad) {
        Actividad actividad = actividadRepository.findById(oidActividad)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
        List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository.findByActividadCalendario_Actividad_OidActividad(oidActividad);
        Calendario calendario = relaciones.isEmpty() ? null : relaciones.get(0).getActividadCalendario().getCalendario();
        List<AtributoDTO> atributos = eavAtributoService.obtenerAtributosPorActividad(actividad);
        UsuarioActividadCalendarioDTOResponse dto = mapper.toResponse(actividad, relaciones, calendario, atributos);
        return new ApiResponse<>(200, "Actividad encontrada", dto);
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminarActividad(Integer oidActividad) {
        if (!actividadRepository.existsById(oidActividad)) {
            return new ApiResponse<>(404, "Actividad no encontrada", null);
        }
        // Eliminar todas relaciones de usuario asociadas a la actividad (todas las actividadCalendario)
        List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository.findByActividadCalendario_Actividad_OidActividad(oidActividad);
        if (!relaciones.isEmpty()) {
            usuarioActividadCalendarioRepository.deleteAll(relaciones);
        }

        // Borrar todas las filas de ACTIVIDADCALENDARIO para esa actividad
        List<ActividadCalendario> actividadCalendarios = actividadCalendarioRepository.findByActividad_OidActividad(oidActividad);
        if (!actividadCalendarios.isEmpty()) {
            actividadCalendarioRepository.deleteAll(actividadCalendarios);
        }

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
            return new ApiResponse<>(404, "Relación no encontrada (actividad+calendario)", null);
        }
        ActividadCalendario ac = optAc.get();

        boolean exists = usuarioActividadCalendarioRepository.existsByActividadCalendario_OidActividadCalendarioAndUsuario_OidUsuario(ac.getOidActividadCalendario(), oidUsuario);
        if (!exists) {
            return new ApiResponse<>(404, "Relación no encontrada", null);
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


