package co.edu.unicauca.sed.api.service.fuente;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.EstadoFuente;
import co.edu.unicauca.sed.api.domain.Fuente;
import co.edu.unicauca.sed.api.domain.Usuario;
import co.edu.unicauca.sed.api.dto.FuenteCreateDTO;
import co.edu.unicauca.sed.api.repository.FuenteRepository;
import co.edu.unicauca.sed.api.service.notificacion.NotificacionDocumentoService;
import co.edu.unicauca.sed.api.utils.StringUtils;
import co.edu.unicauca.sed.api.repository.ActividadRepository;
import co.edu.unicauca.sed.api.repository.EstadoFuenteRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del servicio para manejar la lógica de negocio relacionada con
 * fuentes.
 */
@Service
public class FuenteBusinessServiceImpl implements FuenteBusinessService {

    private static final Logger logger = LoggerFactory.getLogger(FuenteBusinessServiceImpl.class);

    @Autowired
    private FuenteRepository fuenteRepository;

    @Autowired
    @Lazy
    private ActividadRepository actividadRepository;

    @Autowired
    private FuenteFileService fileService;

    @Autowired
    private EstadoFuenteService estadoFuenteService;

    @Autowired
    private NotificacionDocumentoService notificacionDocumentoService;

    @Autowired
    private EstadoFuenteRepository estadoFuenteRepository;

    @Override
    public void procesarFuente(FuenteCreateDTO fuenteDTO, MultipartFile informeFuente, String observacion, Map<String, MultipartFile> archivosEjecutivos) {
        try {
            String mensajeTipoFuente;
            Actividad actividad = actividadRepository.findById(fuenteDTO.getOidActividad())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una actividad con el ID: " + fuenteDTO.getOidActividad()));

            String periodoAcademico = actividad.getProceso().getOidPeriodoAcademico().getIdPeriodo();
            String nombres = StringUtils.formatearCadena(actividad.getProceso().getEvaluado().getNombres());
            String apellidos = StringUtils.formatearCadena(actividad.getProceso().getEvaluado().getApellidos());
            String nombreEvaluado = nombres + "_" + apellidos;
            String contratacion = actividad.getProceso().getEvaluado().getUsuarioDetalle().getContratacion();
            String departamento = actividad.getProceso().getEvaluado().getUsuarioDetalle().getDepartamento();
            String nombreActividad = StringUtils.formatearCadena(actividad.getNombreActividad());
            String idEvaluador = actividad.getProceso().getEvaluador().getIdentificacion();

            // Garantizar que el repositorio no devuelva null
            Optional<Fuente> fuenteOpcional = Optional.ofNullable(
                fuenteRepository.findByActividadAndTipoFuente(actividad, fuenteDTO.getTipoFuente())).orElse(Optional.empty());

            Fuente fuente = fuenteOpcional.orElse(new Fuente());

            // Manejo según tipo de fuente
            Path rutaArchivoComun = null;
            Path rutaInformeEjecutivo = null;
            Usuario evaluador = actividad.getProceso().getEvaluador();
            Usuario evaluado = actividad.getProceso().getEvaluado();

            if ("2".equals(fuenteDTO.getTipoFuente())) {
                mensajeTipoFuente = "Fuente 2";
                rutaArchivoComun = fileService.manejarArchivoFuente(fuenteOpcional, informeFuente, periodoAcademico,
                        nombreEvaluado, contratacion, departamento, nombreActividad, idEvaluador, "fuente");
                notificacionDocumentoService.notificarEvaluadoPorFuente(mensajeTipoFuente, fuente);
            } else if ("1".equals(fuenteDTO.getTipoFuente())) {
                mensajeTipoFuente = "Fuente 1 (Autoevaluación)";
                rutaArchivoComun = fileService.manejarArchivoFuente(fuenteOpcional, informeFuente, periodoAcademico,
                        nombreEvaluado, contratacion, departamento, null, null, null);
                rutaInformeEjecutivo = fileService.manejarInformeEjecutivo(fuenteOpcional, fuenteDTO,
                        archivosEjecutivos, periodoAcademico, nombreEvaluado, contratacion, departamento);
            } else {
                logger.warn("Tipo de fuente desconocido: {}", fuenteDTO.getTipoFuente());
                throw new IllegalArgumentException("Tipo de fuente desconocido: " + fuenteDTO.getTipoFuente());
            }

            // Determinar y asignar estado
            EstadoFuente estadoFuente = determinarEstadoFuente(fuente);

            // Asignar valores a la fuente
            asignarValoresFuente(fuente, fuenteDTO, rutaArchivoComun, observacion, estadoFuente, actividad, rutaInformeEjecutivo);
            fuenteRepository.save(fuente);
        } catch (Exception e) {
            logger.error("Error inesperado al procesar la fuente: {}", fuenteDTO, e);
            throw new RuntimeException("Error inesperado al procesar la fuente: " + e.getMessage(), e);
        }
    }

    /**
     * Determina el estado de la fuente basado en su estado actual.
     *
     * @param fuente La fuente cuyo estado será determinado.
     * @return El estado actualizado o conservado de la fuente.
     */
    @Override
    public EstadoFuente determinarEstadoFuente(Fuente fuente) {
        try {
            EstadoFuente estadoFuentePendiente = estadoFuenteRepository.findByNombreEstado("PENDIENTE")
                    .orElseThrow(() -> new IllegalArgumentException("Estado de fuente no válido."));
            if (fuente.getEstadoFuente() != null && fuente.getEstadoFuente().getOidEstadoFuente() == estadoFuentePendiente.getOidEstadoFuente()) {
                EstadoFuente estadoFuenteDiligenciado = estadoFuenteRepository.findByNombreEstado("DILIGENCIADO")
                        .orElseThrow(() -> new IllegalArgumentException("Estado de fuente no válido."));
                return estadoFuenteService.createEstadoFuente(estadoFuenteDiligenciado.getOidEstadoFuente());
            } else if (fuente.getEstadoFuente() != null) {
                return fuente.getEstadoFuente();
            } else {
                return estadoFuenteService.createEstadoFuente(estadoFuentePendiente.getOidEstadoFuente());
            }
        } catch (Exception e) {
            logger.error("Error al determinar el estado de la fuente", e);
            throw new RuntimeException("Error al determinar el estado de la fuente: " + e.getMessage(), e);
        }
    }

    
    @Override
    public void actualizarFuente(Fuente fuente, float nuevaCalificacion, String tipoCalificacion, String observacion) {
        fuente.setCalificacion(nuevaCalificacion);
        fuente.setTipoCalificacion(tipoCalificacion != null ? tipoCalificacion.toUpperCase() : null);
        EstadoFuente estadoFuente = determinarEstadoFuente(fuente);
        fuente.setEstadoFuente(estadoFuente);
        fuente.setObservacion(observacion != null ? observacion.toUpperCase() : null);
        fuenteRepository.save(fuente);
    }

    /**
     * Asigna valores a una entidad Fuente, actualizando o configurando información
     * como el estado, actividad y archivos asociados.
     *
     * @param fuente               La entidad Fuente a modificar.
     * @param fuenteDTO            Datos provenientes del DTO.
     * @param rutaArchivoComun     Ruta del archivo común.
     * @param observacion          Observación a asociar.
     * @param estadoFuente         Estado de la fuente.
     * @param actividad            Actividad vinculada.
     * @param rutaInformeEjecutivo Ruta del informe ejecutivo.
     */
    private void asignarValoresFuente(Fuente fuente, FuenteCreateDTO fuenteDTO, Path rutaArchivoComun,
            String observacion, EstadoFuente estadoFuente, Actividad actividad,
            Path rutaInformeEjecutivo) {
        try {
            fuente.setTipoFuente(fuenteDTO.getTipoFuente());
            fuente.setCalificacion(fuenteDTO.getCalificacion());
            fuente.setNombreDocumentoFuente(rutaArchivoComun != null ? rutaArchivoComun.getFileName().toString()
                    : fuente.getNombreDocumentoFuente());
            fuente.setRutaDocumentoFuente(
                    rutaArchivoComun != null ? rutaArchivoComun.toString() : fuente.getRutaDocumentoFuente());
            fuente.setObservacion(observacion);
            fuente.setTipoCalificacion(fuenteDTO.getTipoCalificacion());
            fuente.setActividad(actividad);
            fuente.setEstadoFuente(estadoFuente);

            if (rutaInformeEjecutivo != null) {
                fuente.setRutaDocumentoInforme(rutaInformeEjecutivo.toString());
                fuente.setNombreDocumentoInforme(rutaInformeEjecutivo.getFileName().toString());
            }
        } catch (Exception e) {
            logger.error("Error al asignar valores a la fuente", e);
            throw new RuntimeException("Error al asignar valores a la fuente: " + e.getMessage(), e);
        }
    }
}
