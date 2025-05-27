package co.edu.unicauca.sed.api.service.actividad.Impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.edu.unicauca.sed.api.service.EavAtributoService;
import co.edu.unicauca.sed.api.service.actividad.ActividadDTOService;
import co.edu.unicauca.sed.api.service.actividad.ActividadDetalleService;
import co.edu.unicauca.sed.api.service.actividad.ActividadQueryService;
import co.edu.unicauca.sed.api.service.actividad.ActividadService;
import co.edu.unicauca.sed.api.service.actividad.EstadoActividadService;
import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.EavAtributo;
import co.edu.unicauca.sed.api.domain.EstadoFuente;
import co.edu.unicauca.sed.api.domain.PeriodoAcademico;
import co.edu.unicauca.sed.api.domain.Proceso;
import co.edu.unicauca.sed.api.domain.Usuario;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.EvaluadorAsignacionDTO;
import co.edu.unicauca.sed.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sed.api.exception.ValidationException;
import co.edu.unicauca.sed.api.mapper.ActividadMapper;
import co.edu.unicauca.sed.api.repository.ActividadRepository;
import co.edu.unicauca.sed.api.repository.EavAtributoRepository;
import co.edu.unicauca.sed.api.repository.EstadoFuenteRepository;
import co.edu.unicauca.sed.api.repository.TipoActividadRepository;
import co.edu.unicauca.sed.api.repository.UsuarioRepository;
import co.edu.unicauca.sed.api.service.fuente.FuenteService;
import co.edu.unicauca.sed.api.service.periodo_academico.PeriodoAcademicoService;
import co.edu.unicauca.sed.api.service.proceso.ProcesoService;

@Service
public class ActividadServiceImpl implements ActividadService {

    private static final Logger logger = LoggerFactory.getLogger(ActividadServiceImpl.class);

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private ActividadDTOService actividadDTOService;

    @Autowired
    private ActividadQueryService actividadQueryService;

    @Autowired
    private ActividadMapper actividadMapper;

    @Autowired
    private PeriodoAcademicoService periodoAcademicoService;

    @Autowired
    private EstadoActividadService estadoActividadService;

    @Autowired
    private FuenteService fuenteService;

    @Autowired
    private ProcesoService procesoService;

    @Autowired
    private EavAtributoService eavAtributoService;

    @Autowired
    private EavAtributoRepository eavAtributoRepository;

    @Autowired
    EstadoFuenteRepository estadoFuenteRepository;

    @Autowired
    private ActividadDetalleService actividadDetalleService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoActividadRepository tipoActividadRepository;

    @Override
    public ApiResponse<Page<ActividadBaseDTO>> obtenerTodos(Pageable paginacion, Boolean ordenAscendente) {
        boolean orden = (ordenAscendente != null) ? ordenAscendente : true;

        Page<Actividad> actividades = actividadRepository.findAll(paginacion);
        if (actividades.isEmpty()) {
            return new ApiResponse<>(404, "No se encontraron actividades.", Page.empty());
        }

        List<ActividadBaseDTO> actividadDTOs = actividades.getContent().stream()
                .map(actividadDTOService::buildActividadBaseDTO)
                .collect(Collectors.toList());

        List<ActividadBaseDTO> sortedDTOs = actividadQueryService.ordenarActividadesPorTipo(actividadDTOs, orden);
        return new ApiResponse<>(200, "Actividades obtenidas correctamente.",
                new PageImpl<>(sortedDTOs, paginacion, actividades.getTotalElements()));
    }

    @Override
    public Actividad buscarPorId(Integer id) {
        return actividadRepository.findById(id).orElse(null);
    }

    @Override
    public ApiResponse<ActividadBaseDTO> buscarDTOPorId(Integer id) {
        try {
            Actividad actividad = actividadRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("No se encontró una actividad con el ID: " + id));
            return new ApiResponse<>(200, "Actividad encontrada.", actividadDTOService.buildActividadBaseDTO(actividad));
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        }
    }

    @Transactional
    @Override
    public ApiResponse<List<Actividad>> guardar(List<ActividadBaseDTO> actividadesDTO) {
        List<Actividad> actividadesGuardadas = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        Map<String, EavAtributo> cacheAtributos = eavAtributoRepository.findAll().stream()
            .collect(Collectors.toMap(EavAtributo::getNombre, Function.identity()));

        EstadoFuente estadoFuentePendiente = estadoFuenteRepository.findByNombreEstado("PENDIENTE")
            .orElseThrow(() -> new IllegalArgumentException("Estado 'PENDIENTE' no encontrado."));

        Integer idPeriodoAcademico = periodoAcademicoService.obtenerIdPeriodoAcademicoActivo();

        Map<Integer, Usuario> cacheUsuariosPorId = new HashMap<>();
        Map<String, Usuario> cacheUsuariosPorIdentificacion = new HashMap<>();
        Map<String, Usuario> cacheEvaluadores = new HashMap<>();

        for (ActividadBaseDTO dto : actividadesDTO) {
            try {
                /* 
                if (dto.getEsLaborDocente()) {
                    Optional<Actividad> existente = actividadRepository.findByIdLaborDocente(dto.getIdLaborDocente());
                    if (existente.isPresent()) {
                        logger.info("Actividad duplicada detectada con idLaborDocente: " + dto.getIdLaborDocente());
                        errores.add("Actividad duplicada con idLaborDocente: " + dto.getIdLaborDocente());
                        continue;
                    }
                }
                */
                if (dto.getOidEvaluado() == null) {
                    throw new ValidationException(400, "El id evaluado no puede ser nulo.");
                }
                if (dto.getTipoActividad() == null || dto.getTipoActividad().getOidTipoActividad() == null) {
                    throw new ValidationException(400, "El tipo de actividad no puede ser nulo.");
                }
                tipoActividadRepository.findById(dto.getTipoActividad().getOidTipoActividad())
                    .orElseThrow(() -> new ValidationException(400, "El tipo de actividad con ID "
                            + dto.getTipoActividad().getOidTipoActividad() + " no existe."));

                if (dto.getHoras() == null || dto.getHoras() <= 0) {
                    throw new ValidationException(400, "La cantidad de horas no puede ser nula o negativa.");
                }
                if (dto.getSemanas() == null || dto.getSemanas() <= 0) {
                    throw new ValidationException(400, "La cantidad de semanas no puede ser nula o negativa.");
                }
                Actividad guardada = guardarActividad(dto, cacheAtributos, estadoFuentePendiente, idPeriodoAcademico, cacheUsuariosPorId, cacheUsuariosPorIdentificacion, cacheEvaluadores);
                actividadesGuardadas.add(guardada);
            } catch (DataIntegrityViolationException e) {
                errores.add("Actividad con ID " + dto.getOidActividad() + ": ya existe.");
            } catch (Exception e) {
                logger.info("Error guardando actividad: " + e.getMessage());
                errores.add("Actividad con ID " + dto.getOidActividad() + ": " + e.getMessage());
            }
        }

        if (!actividadesGuardadas.isEmpty()) {
            String mensaje = construirMensajeFinal(actividadesGuardadas.size(), errores.size());
            return new ApiResponse<>(201, mensaje, actividadesGuardadas);
        } else {
            return new ApiResponse<>(400, "No se pudo guardar ninguna actividad. Errores: " + String.join(" | ", errores), null);
        }
    }

    private String construirMensajeFinal(int exitosas, int fallidas) {
        String mensaje = "Actividades guardadas: " + exitosas;
        if (fallidas > 0) {
            mensaje += ". Con errores en " + fallidas + " actividad(es).";
        }
        return mensaje;
    }    

    private Actividad guardarActividad(ActividadBaseDTO dto, Map<String, EavAtributo> cacheAtributos, EstadoFuente estadoFuentePendiente,
        Integer idPeriodoAcademico, Map<Integer, Usuario> cacheUsuariosPorId, Map<String, Usuario> cacheUsuariosPorIdentificacion, Map<String, Usuario> cacheEvaluadores) {

        validarDuplicado(dto);

        Actividad actividad = actividadMapper.convertToEntity(dto);
        asignarPeriodoAcademicoActivo(actividad, idPeriodoAcademico);

        if (actividad.getProceso().getNombreProceso() == null || actividad.getProceso().getNombreProceso().isEmpty()) {
            actividad.getProceso().setNombreProceso("ACTIVIDAD");
        }

        asignarUsuario(actividad, dto, cacheUsuariosPorId, cacheUsuariosPorIdentificacion, cacheEvaluadores);
        procesoService.guardarProceso(actividad);
        asignarNombreActividad(actividad, dto);

        Actividad actividadGuardada = actividadRepository.save(actividad);
        guardarComponentesRelacionados(dto, actividadGuardada, cacheAtributos, estadoFuentePendiente);

        return actividadGuardada;
    }

    private void validarDuplicado(ActividadBaseDTO dto) {
        if (dto.getOidActividad() != null && actividadRepository.existsById(dto.getOidActividad())) {
            throw new DataIntegrityViolationException("La actividad con ID " + dto.getOidActividad() + " ya existe.");
        }
    }

    private void asignarUsuario(Actividad actividad, ActividadBaseDTO dto, Map<Integer, Usuario> cacheUsuariosPorId, Map<String, Usuario> cacheUsuariosPorIdentificacion, Map<String, Usuario> cacheEvaluadores) {
        Usuario evaluado = obtenerUsuarioEvaluado(dto, cacheUsuariosPorId, cacheUsuariosPorIdentificacion);
        actividad.getProceso().setEvaluado(evaluado);

        Usuario evaluador;
        if (dto.getOidEvaluador() != 0) {
            evaluador = new Usuario(dto.getOidEvaluador());
            actividad.setAsignacionDefault(false);
        } else {
            Integer idTipoActividad = dto.getTipoActividad().getOidTipoActividad();
            EvaluadorAsignacionDTO evaluadorAsignacion = obtenerEvaluadorAutomatico(idTipoActividad, evaluado, cacheEvaluadores);
            evaluador = evaluadorAsignacion.getEvaluador();
            actividad.setAsignacionDefault(evaluadorAsignacion.isAsignacionDefault());
        }

        actividad.getProceso().setEvaluador(evaluador);
    }

    private void asignarNombreActividad(Actividad actividad, ActividadBaseDTO dto) {
        if (actividad.getNombreActividad() == null || actividad.getNombreActividad().isEmpty()) {
            actividad.setNombreActividad(actividadDetalleService.generarNombreActividad(dto));
        }
    }

    private void guardarComponentesRelacionados(ActividadBaseDTO dto,
            Actividad actividadGuardada,
            Map<String, EavAtributo> cacheAtributos,
            EstadoFuente estadoFuentePendiente) {
        fuenteService.crearTipoFuente(actividadGuardada, estadoFuentePendiente);
        eavAtributoService.guardarAtributosDinamicos(dto, actividadGuardada, cacheAtributos);
    }

    @Transactional
    @Override
    public ApiResponse<Actividad> actualizar(Integer idActividad, ActividadBaseDTO actividadDTO) {
        try {
            Actividad actividadExistente = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ValidationException(404, "Actividad con ID " + idActividad + " no encontrada."));

            if (actividadDTO.getOidActividad() != null && !actividadDTO.getOidActividad().equals(idActividad)
                    && actividadRepository.existsById(actividadDTO.getOidActividad())) {
                return new ApiResponse<>(409, "Error: Ya existe una actividad con ID " + actividadDTO.getOidActividad(),null);
            }

            if (actividadDTO.getNombreActividad() == null || actividadDTO.getNombreActividad().isEmpty()) {
                actividadDTO.setNombreActividad(actividadDetalleService.generarNombreActividad(actividadDTO));
            }

            Proceso proceso = actividadExistente.getProceso();
            if (proceso == null) {
                throw new ValidationException(400, "Error: La actividad no tiene un proceso asociado.");
            }

            proceso.setEvaluador(new Usuario(actividadDTO.getOidEvaluador()));
            proceso.setEvaluado(new Usuario(actividadDTO.getOidEvaluado()));

            // Si el nombre del proceso está vacío, asignar un valor por defecto
            if (proceso.getNombreProceso() == null || proceso.getNombreProceso().isEmpty()) {
                proceso.setNombreProceso("ACTUALIZADO - ACTIVIDAD");
            }

            procesoService.actualizar(actividadExistente.getProceso().getOidProceso(), proceso);

            actividadMapper.actualizarCamposBasicos(actividadExistente, actividadDTO);
            estadoActividadService.asignarEstadoActividad(actividadExistente, actividadDTO.getOidEstadoActividad());
            Map<String, EavAtributo> cacheAtributos = eavAtributoRepository.findAll().stream()
                    .collect(Collectors.toMap(EavAtributo::getNombre, Function.identity()));

            eavAtributoService.actualizarAtributosDinamicos(actividadDTO, actividadExistente, cacheAtributos);
            actividadExistente.setAsignacionDefault(false);

            Actividad actividadActualizada = actividadRepository.save(actividadExistente);
            return new ApiResponse<>(200, "Actividad actualizada correctamente.", actividadActualizada);
        } catch (ValidationException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        }
    }

    @Transactional
    @Override
    public ApiResponse<Void> eliminar(Integer id) {
        try {
            if (!actividadRepository.existsById(id)) {
                return new ApiResponse<>(404, "Actividad con ID " + id + " no encontrada.", null);
            }
            actividadRepository.deleteById(id);
            return new ApiResponse<>(200, "Actividad eliminada correctamente.", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar la actividad: " + e.getMessage(), null);
        }
    }

    private void asignarPeriodoAcademicoActivo(Actividad actividad, Integer idPeriodoAcademico) {
        if (idPeriodoAcademico == null) {
            throw new IllegalArgumentException("El ID del periodo académico no puede ser nulo.");
        }
    
        if (actividad.getProceso() == null) {
            actividad.setProceso(new Proceso());
        }
    
        PeriodoAcademico periodoAcademico = new PeriodoAcademico();
        periodoAcademico.setOidPeriodoAcademico(idPeriodoAcademico);
        actividad.getProceso().setOidPeriodoAcademico(periodoAcademico);
    }

    private EvaluadorAsignacionDTO obtenerEvaluadorAutomatico(Integer oidTipoActividad,Usuario evaluado,Map<String, Usuario> cacheEvaluadores) {
        final int ROL_DECANO = 3;
        final int ROL_JEFE_DEPTO = 4;
        final int ROL_SECRETARIA = 5;

        String facultad = evaluado.getUsuarioDetalle().getFacultad();
        String departamento = evaluado.getUsuarioDetalle().getDepartamento();

        try {
            switch (oidTipoActividad) {
                case 4: {
                    String key = claveEvaluador("FACULTAD", facultad, ROL_DECANO);
                    Usuario evaluador = cacheEvaluadores.computeIfAbsent(key,
                        k -> usuarioRepository.findFirstActiveByFacultadAndRolId(facultad, ROL_DECANO)
                            .orElseThrow(() -> new RuntimeException("No se encontró Decano activo para la facultad " + facultad)));
                    return new EvaluadorAsignacionDTO(evaluador, false);
                }
                case 3, 5, 6, 8, 9: {
                    String key = claveEvaluador("DEPARTAMENTO", departamento, ROL_JEFE_DEPTO);
                    Usuario evaluador = cacheEvaluadores.computeIfAbsent(key,
                        k -> usuarioRepository.findFirstActiveByDepartamentoAndRolId(departamento, ROL_JEFE_DEPTO)
                            .orElseThrow(() -> new RuntimeException("No se encontró Jefe de Departamento activo para el departamento " + departamento)));
                    return new EvaluadorAsignacionDTO(evaluador, false);
                }
                case 1, 2, 7:
                default: {
                    String key = claveEvaluador("DEPARTAMENTO", departamento, ROL_JEFE_DEPTO);
                    Usuario evaluador = cacheEvaluadores.computeIfAbsent(key,
                        k -> usuarioRepository.findFirstActiveByDepartamentoAndRolId(departamento, ROL_JEFE_DEPTO)
                            .orElseThrow(() -> new RuntimeException("No se encontró Jefe de Departamento activo para el departamento " + departamento)));
                    return new EvaluadorAsignacionDTO(evaluador, true);
                }
            }
        } catch (RuntimeException e) {
            // Fallback definitivo a secretaria de facultad
            String fallbackKey = claveEvaluador("FACULTAD", facultad, ROL_SECRETARIA);
            Usuario evaluadorFallback = cacheEvaluadores.computeIfAbsent(fallbackKey,
                    k -> usuarioRepository.findFirstActiveByFacultadAndRolId(facultad, ROL_SECRETARIA)
                        .orElseThrow(() -> new RuntimeException("❌ No se encontró secretaria/o activa para la facultad " + facultad)));
            return new EvaluadorAsignacionDTO(evaluadorFallback, true);
        }
    }

    private String claveEvaluador(String tipo, String valor, int rolId) {
        return tipo + ":" + valor + ":" + rolId;
    }

    private Usuario obtenerUsuarioEvaluado(ActividadBaseDTO dto,Map<Integer, Usuario> cachePorId, Map<String, Usuario> cachePorIdentificacion) {
        Integer oidEvaluado = dto.getOidEvaluado();

        // 1. Buscar por ID en caché
        if (cachePorId.containsKey(oidEvaluado)) {
            return cachePorId.get(oidEvaluado);
        }

        // 2. Buscar por ID en base de datos
        Optional<Usuario> posibleEvaluado = usuarioRepository.findById(oidEvaluado);
        if (posibleEvaluado.isPresent()) {
            cachePorId.put(oidEvaluado, posibleEvaluado.get());
            return posibleEvaluado.get();
        }

        // 3. Buscar por identificación (string)
        String identificacion = String.valueOf(oidEvaluado);

        if (cachePorIdentificacion.containsKey(identificacion)) {
            return cachePorIdentificacion.get(identificacion);
        }

        Usuario evaluado = usuarioRepository.findByIdentificacion(identificacion);
        if (evaluado == null) {
            throw new RuntimeException("No se encontró evaluado con identificación " + identificacion);
        }

        // Guardar en caché por identificación
        cachePorIdentificacion.put(identificacion, evaluado);
        return evaluado;
    }
}
