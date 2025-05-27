package co.edu.unicauca.sed.api.service.evaluacion_docente;

import co.edu.unicauca.sed.api.domain.*;
import co.edu.unicauca.sed.api.dto.*;
import co.edu.unicauca.sed.api.dto.actividad.InformacionActividadDTO;
import co.edu.unicauca.sed.api.mapper.EvaluacionMapperUtil;
import co.edu.unicauca.sed.api.repository.*;
import co.edu.unicauca.sed.api.service.fuente.FuenteBusinessService;
import co.edu.unicauca.sed.api.service.fuente.FuenteService;
import co.edu.unicauca.sed.api.utils.ArchivoUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AutoevaluacionServiceImpl implements AutoevaluacionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoevaluacionServiceImpl.class);

    private static final String MSG_AUTOEVALUACION_OK = "Autoevaluación guardada correctamente.";
    private static final String MSG_AUTOEVALUACION_ERROR = "Error inesperado al guardar la autoevaluación.";
    private static final String MSG_BUSQUEDA_ERROR = "Error inesperado al buscar la autoevaluación.";
    private static final String MSG_NO_EVALUACION = "No se encontró autoevaluación para la fuente con ID: ";
    private static final String PREFIJO_SCREENSHOT = "screenshot";
    private static final String PREFIJO_FUENTE_1 = "fuente-1";
    private final AutoevaluacionRepository autoevaluacionRepository;
    private final OportunidadMejoraRepository oportunidadMejoraRepository;
    private final FuenteService fuenteService;
    private final FuenteBusinessService fuenteBussines;
    private final LeccionAprendidaService leccionAprendidaService;
    private final OportunidadMejoraService oportunidadMejoraService;
    private final AutoevaluacionOdsService autoevaluacionOdsService;

    @Override
    @Transactional
    public ApiResponse<Void> guardarAutoevaluacion(AutoevaluacionDTO dto, MultipartFile firma,
            MultipartFile screenshotSimca, MultipartFile documentoAutoevaluacion, List<MultipartFile> archivosOds) {
        try {
            Fuente fuente = fuenteService.obtenerFuente(dto.getOidFuente());

            // Verificar si ya existe autoevaluación
            Optional<Autoevaluacion> autoevaluacionOpt = autoevaluacionRepository.findByFuente(fuente);
            Autoevaluacion objAutoevaluacion = obtenerAutoevaluacion(autoevaluacionOpt, fuente);

            Autoevaluacion autoevaluacion = procesarAutoevaluacion(objAutoevaluacion, fuente, firma, screenshotSimca, dto.getDescripcion());

            // Guardar ODS con sus evidencias
            autoevaluacionOdsService.guardarOds(dto.getOdsSeleccionados(), autoevaluacion, archivosOds, fuente);

            // Guardar lecciones y mejoras
            leccionAprendidaService.guardar(dto.getLeccionesAprendidas(), autoevaluacion);

            oportunidadMejoraService.guardar(dto.getOportunidadesMejora(), autoevaluacion);

            // Guardar documento de notas
            String timestamp = String.valueOf(System.currentTimeMillis());
            String prefijo = PREFIJO_FUENTE_1 + "-" + timestamp;
            String rutaNotas = fuenteService.guardarDocumentoFuente(fuente, documentoAutoevaluacion, prefijo);
            if (rutaNotas != null) {
                fuente.setRutaDocumentoFuente(rutaNotas);
                String nombreArchivo = ArchivoUtils.extraerNombreArchivo(rutaNotas);
                fuente.setNombreDocumentoFuente(nombreArchivo);
            }

            // Actualizar información de la fuente
            fuenteBussines.actualizarFuente(fuente, dto.getCalificacion(), dto.getTipoCalificacion(), dto.getObservacion());
            return new ApiResponse<>(200, MSG_AUTOEVALUACION_OK, null);
        } catch (Exception e) {
            LOGGER.error("❌ " + MSG_AUTOEVALUACION_ERROR, e);
            return new ApiResponse<>(500, MSG_AUTOEVALUACION_ERROR, null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Object> listarAutoevaluacion(Integer oidFuente) {
        try {
            Fuente fuente = fuenteService.obtenerFuente(oidFuente);
            Autoevaluacion autoevaluacion = autoevaluacionRepository.findByFuente(fuente).orElseThrow(() -> new NoSuchElementException(MSG_NO_EVALUACION + oidFuente));
            Map<String, Object> resultado = new LinkedHashMap<>();
            Map<String, Object> informacionFuente = EvaluacionMapperUtil.construirInformacionFuente(
                fuente, fuente.getTipoCalificacion(), fuente.getObservacion(), fuente.getNombreDocumentoFuente()
            );
            resultado.put("Fuente", informacionFuente);
            resultado.put("actividad", obtenerInformacionActividad(fuente.getActividad()));
            resultado.put("Descripcion", autoevaluacion.getDescripcion());
            resultado.put("screenshotSimca", autoevaluacion.getScreenshotSimca());
            resultado.put("odsSeleccionados", autoevaluacionOdsService.obtenerOds(autoevaluacion));
            resultado.put("leccionesAprendidas", leccionAprendidaService.obtenerDescripcionesLecciones(autoevaluacion));
            resultado.put("oportunidadesMejora", obtenerDescripcionesMejoras(autoevaluacion));

            return new ApiResponse<>(200, "Autoevaluación encontrada correctamente.", resultado);
        } catch (NoSuchElementException e) {
            LOGGER.warn("⚠️ {}", e.getMessage());
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            LOGGER.error("❌ " + MSG_BUSQUEDA_ERROR, e);
            return new ApiResponse<>(500, MSG_BUSQUEDA_ERROR, null);
        }
    }

    private List<OportunidadMejoraDTO> obtenerDescripcionesMejoras(Autoevaluacion autoevaluacion) {
        return oportunidadMejoraRepository.findByAutoevaluacion(autoevaluacion)
            .stream()
            .map(oportunidadMejora -> new OportunidadMejoraDTO(oportunidadMejora.getOidOportunidadMejora(), oportunidadMejora.getDescripcion()))
            .collect(Collectors.toList());
    }

    private Autoevaluacion obtenerAutoevaluacion(Optional<Autoevaluacion> autoevaluacionOpt, Fuente fuente) {
        return autoevaluacionOpt.orElseGet(() -> {
            Autoevaluacion nuevaAutoevaluacion = new Autoevaluacion();
            nuevaAutoevaluacion.setFuente(fuente);
            return nuevaAutoevaluacion;
        });
    }

    private Autoevaluacion procesarAutoevaluacion(Autoevaluacion autoevaluacion, Fuente fuente, MultipartFile firma, MultipartFile screenshotSimca, String descripcion) {
        try {
            if (screenshotSimca != null) {
                String timestamp = String.valueOf(System.currentTimeMillis());
                String prefijo = PREFIJO_SCREENSHOT + "-" + timestamp;
                String rutaScreenshot = fuenteService.guardarDocumentoFuente(fuente, screenshotSimca, prefijo);
                if (rutaScreenshot != null) {
                    autoevaluacion.setRutaDocumentoSc(rutaScreenshot);
                    String nombreArchivo = ArchivoUtils.extraerNombreArchivo(rutaScreenshot);
                    autoevaluacion.setScreenshotSimca(nombreArchivo);
                }
            }

            autoevaluacion.setDescripcion(descripcion);
            return autoevaluacionRepository.save(autoevaluacion);

        } catch (IOException e) {
            LOGGER.error("❌ Error al guardar los documentos de la autoevaluación: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar los documentos de la autoevaluación", e);
        }
    }

    private InformacionActividadDTO obtenerInformacionActividad(Actividad actividad) {
        InformacionActividadDTO dto = new InformacionActividadDTO();
        dto.setIdActividad(actividad.getOidActividad());
        dto.setNombreActividad(actividad.getNombreActividad());
        dto.setTipoActividad(actividad.getTipoActividad().getNombre());
        dto.setPeriodoAcademico(actividad.getProceso().getOidPeriodoAcademico().getIdPeriodo());
        dto.setHorasTotales(actividad.getHoras() * actividad.getSemanas());
        return dto;
    }
}
