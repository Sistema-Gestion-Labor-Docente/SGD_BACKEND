package co.edu.unicauca.sgd.api.service.actividad.laborDocente.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.ActividadBoolean;
import co.edu.unicauca.sgd.api.domain.ActividadDate;
import co.edu.unicauca.sgd.api.domain.ActividadDecimal;
import co.edu.unicauca.sgd.api.domain.ActividadInt;
import co.edu.unicauca.sgd.api.domain.ActividadVarchar;
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
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTOResponse;
import co.edu.unicauca.sgd.api.mapper.UsuarioActividadCalendarioMapper;
import co.edu.unicauca.sgd.api.repository.ActividadRepository;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.CargoActividadRepository;
import co.edu.unicauca.sgd.api.repository.EavAtributoRepository;
import co.edu.unicauca.sgd.api.repository.EstadoActividadRepository;
import co.edu.unicauca.sgd.api.repository.TipoActividadRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioActividadCalendarioRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioRepository;
import co.edu.unicauca.sgd.api.service.EavAtributoService;
import co.edu.unicauca.sgd.api.service.actividad.laborDocente.UsuarioActividadCalendarioService;
import jakarta.transaction.Transactional;

@Service
public class UsuarioActividadCalendarioServiceImpl implements UsuarioActividadCalendarioService {

    private final ActividadRepository actividadRepository;
    private final UsuarioRepository usuarioRepository;
    private final CalendarioRepository calendarioRepository;
    private final UsuarioActividadCalendarioRepository usuarioActividadCalendarioRepository;
    private final UsuarioActividadCalendarioMapper mapper;
    private final CargoActividadRepository cargoActividadRepository;
    private final EstadoActividadRepository estadoActividadRepository;
    private final EavAtributoService eavAtributoService;
    private final EavAtributoRepository eavAtributoRepository;

    public UsuarioActividadCalendarioServiceImpl(ActividadRepository actividadRepository,
            UsuarioRepository usuarioRepository,
            CalendarioRepository calendarioRepository,
            UsuarioActividadCalendarioRepository usuarioActividadCalendarioRepository,
            UsuarioActividadCalendarioMapper mapper,
            CargoActividadRepository cargoActividadRepository,
            EstadoActividadRepository estadoActividadRepository,
            EavAtributoService eavAtributoService,
            EavAtributoRepository eavAtributoRepository) {
        this.actividadRepository = actividadRepository;
        this.usuarioRepository = usuarioRepository;
        this.calendarioRepository = calendarioRepository;
        this.usuarioActividadCalendarioRepository = usuarioActividadCalendarioRepository;
        this.mapper = mapper;
        this.cargoActividadRepository = cargoActividadRepository;
        this.estadoActividadRepository = estadoActividadRepository;
        this.eavAtributoService = eavAtributoService;
        this.eavAtributoRepository = eavAtributoRepository;
    }

    @Override
    @Transactional
    public ApiResponse<UsuarioActividadCalendarioDTOResponse> crearActividadConRelaciones(UsuarioActividadCalendarioDTORequest request) {

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

        // 2. Crear relaciones UsuarioActividadCalendario
        Calendario calendario = calendarioRepository.findById(request.getOidCalendario())
                .orElseThrow(() -> new RuntimeException("Calendario no encontrado"));

        for (Integer oidUsuario : request.getOidsUsuarios()) {
            Usuario usuario = usuarioRepository.findById(oidUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            UsuarioActividadCalendario relacion = new UsuarioActividadCalendario();
            relacion.setUsuario(usuario);
            relacion.setActividad(actividad);
            relacion.setCalendario(calendario);
            relacion.setUsuarioCreacion("system");
            relacion.setCargoActividad(cargoActividad);
            usuarioActividadCalendarioRepository.save(relacion);
        }

        // 3. Guardar atributos EAV usando el servicio central
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

        // 4. Devolver DTOResponse
        List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository.findByActividad_OidActividad(actividad.getOidActividad());
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
    public ApiResponse<UsuarioActividadCalendarioDTOResponse> actualizarActividadConRelaciones(Integer oidActividad, UsuarioActividadCalendarioDTORequest request) {
        Actividad actividad = actividadRepository.findById(oidActividad)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

        CargoActividad cargoActividad = cargoActividadRepository.findById(request.getOidCargoActividad())
                .orElseThrow(() -> new RuntimeException("Cargo de actividad no encontrado"));

        EstadoActividad estadoActividad = estadoActividadRepository.findById(request.getOidEstadoActividad())
                .orElseThrow(() -> new RuntimeException("Estado de actividad no encontrado"));

        // Actualizar datos base
        actividad.setTipoActividad(cargoActividad.getTipoActividad());
        actividad.setEstadoActividad(estadoActividad);
        actividad.setNombreActividad(request.getNombreActividad());
        actividad.setHoras(request.getHoras());
        actividad.setSemanas(request.getSemanas());
        actividad = actividadRepository.save(actividad);

        // Eliminar relaciones existentes y crear nuevas
        usuarioActividadCalendarioRepository.deleteAll(usuarioActividadCalendarioRepository.findByActividad_OidActividad(oidActividad));
        Calendario calendario = calendarioRepository.findById(request.getOidCalendario())
                .orElseThrow(() -> new RuntimeException("Calendario no encontrado"));

        for (Integer oidUsuario : request.getOidsUsuarios()) {
            Usuario usuario = usuarioRepository.findById(oidUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            UsuarioActividadCalendario relacion = new UsuarioActividadCalendario();
            relacion.setUsuario(usuario);
            relacion.setActividad(actividad);
            relacion.setCalendario(calendario);
            relacion.setCargoActividad(cargoActividad);
            usuarioActividadCalendarioRepository.save(relacion);
        }

        // Actualizar atributos EAV usando el servicio central
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

        List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository.findByActividad_OidActividad(oidActividad);
        UsuarioActividadCalendarioDTOResponse dto = mapper.toResponse(
                actividad, relaciones, calendario,
                (request.getAtributos() != null) ? request.getAtributos().stream()
                        .map(a -> new AtributoDTO(a.getNombre(), a.getValor()))
                        .collect(Collectors.toList()) : List.of()
        );

        return new ApiResponse<>(200, "Actividad actualizada con relaciones", dto);
    }

    @Override
    public ApiResponse<Page<UsuarioActividadCalendarioDTOResponse>> listarActividadesConRelaciones(Pageable pageable) {
        Page<Actividad> actividades = actividadRepository.findAll(pageable);

        Page<UsuarioActividadCalendarioDTOResponse> page = actividades.map(actividad -> {
            List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository.findByActividad_OidActividad(actividad.getOidActividad());
            Calendario calendario = relaciones.isEmpty() ? null : relaciones.get(0).getCalendario();
            // Obtener los atributos desde el servicio EAV para cada actividad:
            List<AtributoDTO> atributos = eavAtributoService.obtenerAtributosPorActividad(actividad);
            return mapper.toResponse(actividad, relaciones, calendario, atributos);
        });

        return new ApiResponse<>(200, "Actividades encontradas", page);
    }

    @Override
    public ApiResponse<UsuarioActividadCalendarioDTOResponse> obtenerActividadConRelaciones(Integer oidActividad) {
        Actividad actividad = actividadRepository.findById(oidActividad)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
        List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository.findByActividad_OidActividad(oidActividad);
        Calendario calendario = relaciones.isEmpty() ? null : relaciones.get(0).getCalendario();
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
        usuarioActividadCalendarioRepository.deleteAll(usuarioActividadCalendarioRepository.findByActividad_OidActividad(oidActividad));
        actividadRepository.deleteById(oidActividad);
        return new ApiResponse<>(204, "Actividad y relaciones eliminadas", null);
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminarRelacion(Integer oidActividad, Integer oidUsuario, Integer oidCalendario) {
        if (!usuarioActividadCalendarioRepository.existsByActividad_OidActividadAndUsuario_OidUsuarioAndCalendario_Oidcalendario(oidActividad, oidUsuario, oidCalendario)) {
            return new ApiResponse<>(404, "Relación no encontrada", null);
        }
        usuarioActividadCalendarioRepository.deleteByActividad_OidActividadAndUsuario_OidUsuarioAndCalendario_Oidcalendario(oidActividad, oidUsuario, oidCalendario);
        return new ApiResponse<>(204, "Relación eliminada correctamente", null);
    }
}


