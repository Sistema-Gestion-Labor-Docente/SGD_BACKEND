package co.edu.unicauca.sgd.api.service.actividad.Impl;

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

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.EavAtributo;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sgd.api.exception.ValidationException;
import co.edu.unicauca.sgd.api.mapper.ActividadMapper;
import co.edu.unicauca.sgd.api.repository.ActividadRepository;
import co.edu.unicauca.sgd.api.repository.EavAtributoRepository;
import co.edu.unicauca.sgd.api.repository.TipoActividadRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioRepository;
import co.edu.unicauca.sgd.api.service.EavAtributoService;
import co.edu.unicauca.sgd.api.service.actividad.ActividadDTOService;
import co.edu.unicauca.sgd.api.service.actividad.ActividadDetalleService;
import co.edu.unicauca.sgd.api.service.actividad.ActividadQueryService;
import co.edu.unicauca.sgd.api.service.actividad.ActividadService;
import co.edu.unicauca.sgd.api.service.actividad.EstadoActividadService;

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
    private EstadoActividadService estadoActividadService;


    @Autowired
    private EavAtributoService eavAtributoService;

    @Autowired
    private EavAtributoRepository eavAtributoRepository;


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


        Integer idPeriodoAcademico = -1;

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
                Actividad guardada = guardarActividad(dto, cacheAtributos, idPeriodoAcademico, cacheUsuariosPorId, cacheUsuariosPorIdentificacion, cacheEvaluadores);
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

    private Actividad guardarActividad(ActividadBaseDTO dto, Map<String, EavAtributo> cacheAtributos,
        Integer idPeriodoAcademico, Map<Integer, Usuario> cacheUsuariosPorId, Map<String, Usuario> cacheUsuariosPorIdentificacion, Map<String, Usuario> cacheEvaluadores) {

        validarDuplicado(dto);

        Actividad actividad = actividadMapper.convertToEntity(dto);

        asignarNombreActividad(actividad, dto);

        Actividad actividadGuardada = actividadRepository.save(actividad);
        guardarComponentesRelacionados(dto, actividadGuardada, cacheAtributos);

        return actividadGuardada;
    }

    private void validarDuplicado(ActividadBaseDTO dto) {
        if (dto.getOidActividad() != null && actividadRepository.existsById(dto.getOidActividad())) {
            throw new DataIntegrityViolationException("La actividad con ID " + dto.getOidActividad() + " ya existe.");
        }
    }


    private void asignarNombreActividad(Actividad actividad, ActividadBaseDTO dto) {
        if (actividad.getNombreActividad() == null || actividad.getNombreActividad().isEmpty()) {
            actividad.setNombreActividad(actividadDetalleService.generarNombreActividad(dto));
        }
    }

    private void guardarComponentesRelacionados(ActividadBaseDTO dto,
            Actividad actividadGuardada,
            Map<String, EavAtributo> cacheAtributos) {
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
}
