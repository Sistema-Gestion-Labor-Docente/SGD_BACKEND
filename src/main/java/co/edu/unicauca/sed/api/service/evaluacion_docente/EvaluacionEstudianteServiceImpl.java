package co.edu.unicauca.sed.api.service.evaluacion_docente;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.Encuesta;
import co.edu.unicauca.sed.api.domain.EncuestaRespuesta;
import co.edu.unicauca.sed.api.domain.EstadoEtapaDesarrollo;
import co.edu.unicauca.sed.api.domain.EvaluacionEstudiante;
import co.edu.unicauca.sed.api.domain.Fuente;
import co.edu.unicauca.sed.api.domain.PeriodoAcademico;
import co.edu.unicauca.sed.api.domain.Pregunta;
import co.edu.unicauca.sed.api.domain.Proceso;
import co.edu.unicauca.sed.api.domain.TipoActividad;
import co.edu.unicauca.sed.api.dto.ActividadDepartamentoDTO;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.EncuestaPreguntaDTO;
import co.edu.unicauca.sed.api.dto.EvaluacionDocenteDTO;
import co.edu.unicauca.sed.api.dto.FuenteEvaluadaDTO;
import co.edu.unicauca.sed.api.dto.PeriodoEvaluacionDTO;
import co.edu.unicauca.sed.api.dto.PreguntaCalificadaDTO;
import co.edu.unicauca.sed.api.dto.TipoActividadFuentesDTO;
import co.edu.unicauca.sed.api.mapper.EvaluacionMapperUtil;
import co.edu.unicauca.sed.api.repository.EncuestaRepository;
import co.edu.unicauca.sed.api.repository.EncuestaRespuestaRepository;
import co.edu.unicauca.sed.api.repository.EstadoEtapaDesarrolloRepository;
import co.edu.unicauca.sed.api.repository.EvaluacionEstudianteRepository;
import co.edu.unicauca.sed.api.repository.FuenteRepository;
import co.edu.unicauca.sed.api.repository.PreguntaRepository;
import co.edu.unicauca.sed.api.service.fuente.FuenteBusinessServiceImpl;
import co.edu.unicauca.sed.api.service.fuente.FuenteService;
import co.edu.unicauca.sed.api.service.notificacion.NotificacionDocumentoService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class EvaluacionEstudianteServiceImpl implements EvaluacionEstudianteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluacionEstudianteServiceImpl.class);

    private static final String PREFIJO_FUENTE_2 = "fuente-2";

    @Autowired
    private EvaluacionEstudianteRepository evaluacionEstudianteRepository;

    @Autowired
    private FuenteRepository fuenteRepository;

    @Autowired
    private EncuestaService encuestaService;

    @Autowired
    private EstadoEtapaDesarrolloRepository estadoEtapaDesarrolloRepository;

    @Autowired
    private FuenteService fuenteService;

    @Autowired
    private EncuestaRespuestaRepository encuestaRespuestaRepository;

    @Autowired
    private PreguntaRepository preguntaRepository;

    @Autowired
    private EncuestaRepository encuestaRepository;

    @Autowired
    private FuenteBusinessServiceImpl fuenteBussines;

    @Autowired
    private NotificacionDocumentoService notificacionDocumentoService;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<EvaluacionEstudiante>> buscarTodos(Pageable pageable) {
        try {
            Page<EvaluacionEstudiante> evaluaciones = evaluacionEstudianteRepository.findAll(pageable);
            return new ApiResponse<>(200, "Evaluaciones de estudiantes obtenidas correctamente.", evaluaciones);
        } catch (Exception e) {
            LOGGER.error("❌ Error al listar Evaluaciones de Estudiantes", e);
            return new ApiResponse<>(500, "Error al listar evaluaciones: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> guardarEvaluacionDocente(EvaluacionDocenteDTO dto, MultipartFile documentoFuente,
            MultipartFile firmaEstudiante) {
        try {

            // Obtener la fuente
            Fuente fuente = obtenerFuente(dto.getOidFuente());

            // Guardar o actualizar EvaluacionEstudiante
            EvaluacionEstudiante evaluacionEstudiante = guardarEvaluacionEstudiante(dto, fuente);

            // Guardar o actualizar Encuesta
            Encuesta encuesta = encuestaService.guardarEncuesta(dto, evaluacionEstudiante);

            // 🔄 Guardar respuestas y calcular la calificación
            float calificacionFinal = guardarRespuestasYCalcularNota(dto.getPreguntas(), encuesta);
            String timestamp = String.valueOf(System.currentTimeMillis());
            String prefijo = PREFIJO_FUENTE_2 + "-" + timestamp;
            String rutaDocumento = fuenteService.guardarDocumentoFuente(fuente, documentoFuente, prefijo);
            if (rutaDocumento != null) {
                fuente.setRutaDocumentoFuente(rutaDocumento);
                fuente.setNombreDocumentoFuente(Paths.get(rutaDocumento).getFileName().toString());
            }

            // Actualizar la calificación de la fuente
            fuenteBussines.actualizarFuente(fuente, calificacionFinal, dto.getTipoCalificacion(), dto.getObservacion());
            notificacionDocumentoService.notificarEvaluadoPorFuente(PREFIJO_FUENTE_2, fuente);
            return new ApiResponse<>(200, "Evaluación docente guardada correctamente.", null);
        } catch (Exception e) {
            LOGGER.error("❌ Error al guardar la evaluación docente.", e);
            return new ApiResponse<>(500, "Error inesperado al guardar la evaluación docente.", null);
        }
    }

    private Fuente obtenerFuente(Integer oidFuente) {
        return fuenteRepository.findById(oidFuente).orElseThrow(() -> new EntityNotFoundException("Fuente no encontrada con ID: " + oidFuente));
    }

    private EvaluacionEstudiante guardarEvaluacionEstudiante(EvaluacionDocenteDTO dto, Fuente fuente) {
        EvaluacionEstudiante evaluacionEstudiante = evaluacionEstudianteRepository.findByFuente(fuente).orElse(new EvaluacionEstudiante());

        evaluacionEstudiante.setFuente(fuente);
        evaluacionEstudiante.setObservacion(dto.getObservacion().toUpperCase());

        EstadoEtapaDesarrollo estado = estadoEtapaDesarrolloRepository.findById(dto.getOidEstadoEtapaDesarrollo())
            .orElseThrow(() -> new EntityNotFoundException("EstadoEtapaDesarrollo no encontrado con ID: " + dto.getOidEstadoEtapaDesarrollo()));

        evaluacionEstudiante.setEstadoEtapaDesarrollo(estado);
        return evaluacionEstudianteRepository.save(evaluacionEstudiante);
    }

    private float guardarRespuestasYCalcularNota(List<EncuestaPreguntaDTO> preguntasDTO, Encuesta encuesta) {
        float calificacionTotal = 0;
        float sumaPesos = 0;

        for (EncuestaPreguntaDTO preguntaDTO : preguntasDTO) {
            Pregunta pregunta = preguntaRepository.findById(preguntaDTO.getOidPregunta())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Pregunta con ID " + preguntaDTO.getOidPregunta() + " no encontrada."));

            EncuestaRespuesta encuestaRespuesta = encuestaRespuestaRepository.findByEncuestaAndPregunta(encuesta, pregunta).orElse(new EncuestaRespuesta());

            encuestaRespuesta.setEncuesta(encuesta);
            encuestaRespuesta.setPregunta(pregunta);
            encuestaRespuesta.setRespuesta(preguntaDTO.getRespuesta());
            encuestaRespuestaRepository.save(encuestaRespuesta);

            // Cálculo de la calificación basada en porcentaje de importancia
            float peso = pregunta.getPorcentajeImportancia();
            float valorRespuesta = Float.parseFloat(preguntaDTO.getRespuesta());

            calificacionTotal += valorRespuesta * peso;
            sumaPesos += peso;
        }

        return (sumaPesos > 0) ? calificacionTotal / sumaPesos : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Object> obtenerEvaluacionEstudiante(Integer oidFuente) {
        try {
            LOGGER.info("🔍 Buscando Evaluación de Estudiante por Fuente con ID: {}", oidFuente);

            Fuente fuente = fuenteService.obtenerFuente(oidFuente);
            EvaluacionEstudiante evaluacionEstudiante = obtenerEvaluacionEstudiantePorFuente(fuente, oidFuente);
            Encuesta encuesta = obtenerEncuestaPorEvaluacion(evaluacionEstudiante);
            List<Map<String, Object>> preguntas = obtenerPreguntasDeEncuesta(encuesta);

            Map<String, Object> resultado = construirResultado(fuente, evaluacionEstudiante, encuesta, preguntas);

            return new ApiResponse<>(200, "Encuesta encontrada correctamente.", resultado);
        } catch (EntityNotFoundException e) {
            LOGGER.warn("⚠️ {}", e.getMessage());
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            LOGGER.error("❌ Error al buscar Encuesta por Fuente", e);
            return new ApiResponse<>(500, "Error inesperado al buscar la encuesta.", null);
        }
    }

    private EvaluacionEstudiante obtenerEvaluacionEstudiantePorFuente(Fuente fuente, Integer oidFuente) {
        return evaluacionEstudianteRepository.findByFuente(fuente)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró Evaluación de Estudiante para la fuente con ID: " + oidFuente));
    }

    private Encuesta obtenerEncuestaPorEvaluacion(EvaluacionEstudiante evaluacionEstudiante) {
        return encuestaRepository.findByEvaluacionEstudiante(evaluacionEstudiante)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró Encuesta para la evaluación con ID: "
                        + evaluacionEstudiante.getOidEvaluacionEstudiante()));
    }

    private List<Map<String, Object>> obtenerPreguntasDeEncuesta(Encuesta encuesta) {
        List<EncuestaRespuesta> respuestas = encuestaRespuestaRepository.findByEncuesta(encuesta);
        
        return respuestas.stream()
            .sorted(Comparator.comparing(respuesta -> respuesta.getPregunta().getOidPregunta()))
            .map(respuesta -> {
                Map<String, Object> preguntaMap = new HashMap<>();
                preguntaMap.put("oidPregunta", respuesta.getPregunta().getOidPregunta());
                preguntaMap.put("respuesta", Integer.parseInt(respuesta.getRespuesta())); // Convertir a número
                return preguntaMap;
            })
            .collect(Collectors.toList());
    }

    private Map<String, Object> construirResultado(Fuente fuente, EvaluacionEstudiante evaluacionEstudiante, Encuesta encuesta, List<Map<String, Object>> preguntas) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        Map<String, Object> resumen = EvaluacionMapperUtil.construirInformacionFuente(
                fuente,
                evaluacionEstudiante.getFuente().getTipoCalificacion(),
                fuente.getObservacion(),
                fuente.getNombreDocumentoFuente());
        resultado.put("Fuente", resumen);
        resultado.put("encuesta", construirEncuesta(encuesta));
        resultado.put("estadoEtapaDesarrollo", construirEstadoEtapaDesarrollo(evaluacionEstudiante));
        resultado.put("preguntas", preguntas);
        return resultado;
    }

    // Construye el mapa de Encuesta
    private Map<String, Object> construirEncuesta(Encuesta encuesta) {
        Map<String, Object> encuestaMap = new LinkedHashMap<>();
        encuestaMap.put("nombre", encuesta.getNombre());
        return encuestaMap;
    }

    // Construye el mapa de EstadoEtapaDesarrollo
    private Map<String, Object> construirEstadoEtapaDesarrollo(EvaluacionEstudiante evaluacionEstudiante) {
        Map<String, Object> estadoEtapaDesarrolloMap = new LinkedHashMap<>();
        estadoEtapaDesarrolloMap.put("oidEstadoEtapaDesarrollo", evaluacionEstudiante.getEstadoEtapaDesarrollo().getOidEstadoEtapaDesarrollo());
        estadoEtapaDesarrolloMap.put("nombre", evaluacionEstudiante.getEstadoEtapaDesarrollo().getNombre());
        return estadoEtapaDesarrolloMap;
    }

    @Override
    public ApiResponse<List<PeriodoEvaluacionDTO>> obtenerEvaluacionesEstructuradas() {
        List<EvaluacionEstudiante> evaluaciones = evaluacionEstudianteRepository.findAll();
        Map<String, PeriodoEvaluacionDTO> periodosMap = new LinkedHashMap<>();

        for (EvaluacionEstudiante evaluacion : evaluaciones) {
            Fuente fuente = evaluacion.getFuente();
            Actividad actividad = fuente.getActividad();
            TipoActividad tipoActividad = actividad.getTipoActividad();
            Proceso proceso = actividad.getProceso();
            PeriodoAcademico periodo = proceso.getOidPeriodoAcademico();
            String departamento = proceso.getEvaluado().getUsuarioDetalle().getDepartamento();

            Encuesta encuesta = encuestaRepository.findByEvaluacionEstudiante(evaluacion).orElse(null);
            if (encuesta == null || encuesta.getEncuestaRespuestas() == null)
                continue;

            List<PreguntaCalificadaDTO> preguntas = encuesta.getEncuestaRespuestas().stream()
                    .map(r -> {
                        try {
                            return new PreguntaCalificadaDTO(r.getPregunta().getOidPregunta(),r.getPregunta().getPregunta(), Float.parseFloat(r.getRespuesta()));
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            if (preguntas.isEmpty())
                continue;

            // Clave para identificar periodo
            String keyPeriodo = periodo.getOidPeriodoAcademico() + "|" + periodo.getIdPeriodo();
            PeriodoEvaluacionDTO periodoDTO = periodosMap.computeIfAbsent(
                keyPeriodo, k -> new PeriodoEvaluacionDTO(periodo.getOidPeriodoAcademico(), periodo.getIdPeriodo(), new ArrayList<>())
            );

            // Buscar o crear departamento dentro del periodo
            ActividadDepartamentoDTO deptoDTO = periodoDTO.getDepartamentos().stream()
                    .filter(d -> d.getDepartamento().equals(departamento))
                    .findFirst()
                    .orElseGet(() -> {
                        ActividadDepartamentoDTO nuevo = new ActividadDepartamentoDTO(departamento, new ArrayList<>());
                        periodoDTO.getDepartamentos().add(nuevo);
                        return nuevo;
                    });

            // Buscar o crear tipo de actividad dentro del departamento
            TipoActividadFuentesDTO tipoDTO = deptoDTO.getTiposActividad().stream()
                    .filter(t -> t.getOidTipoActividad().equals(tipoActividad.getOidTipoActividad()))
                    .findFirst()
                    .orElseGet(() -> {
                        TipoActividadFuentesDTO nuevo = new TipoActividadFuentesDTO(tipoActividad.getOidTipoActividad(),
                                tipoActividad.getNombre(), new ArrayList<>());
                        deptoDTO.getTiposActividad().add(nuevo);
                        return nuevo;
                    });

            // Agregar fuente con preguntas
            tipoDTO.getFuentes().add(new FuenteEvaluadaDTO(fuente.getOidFuente(), preguntas));
        }

        return new ApiResponse<>(200, "Evaluaciones estructuradas correctamente",
                new ArrayList<>(periodosMap.values()));
    }
}
