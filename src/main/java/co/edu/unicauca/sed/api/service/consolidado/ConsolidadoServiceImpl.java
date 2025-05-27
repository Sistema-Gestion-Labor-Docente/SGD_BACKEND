package co.edu.unicauca.sed.api.service.consolidado;

import co.edu.unicauca.sed.api.domain.*;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.ConsolidadoArchivoDTO;
import co.edu.unicauca.sed.api.dto.ConsolidadoDTO;
import co.edu.unicauca.sed.api.dto.HistoricoCalificacionesDTO;
import co.edu.unicauca.sed.api.dto.BaseConsolidadoDataDTO;
import co.edu.unicauca.sed.api.dto.CalificacionPorPeriodoDTO;
import co.edu.unicauca.sed.api.dto.InformacionConsolidadoDTO;
import co.edu.unicauca.sed.api.dto.actividad.ActividadPaginadaDTO;
import co.edu.unicauca.sed.api.repository.ActividadRepository;
import co.edu.unicauca.sed.api.repository.ConsolidadoRepository;
import co.edu.unicauca.sed.api.repository.ProcesoRepository;
import co.edu.unicauca.sed.api.repository.UsuarioRepository;
import co.edu.unicauca.sed.api.service.actividad.*;
import co.edu.unicauca.sed.api.service.documento.ExcelService;
import co.edu.unicauca.sed.api.service.notificacion.NotificacionDocumentoService;
import co.edu.unicauca.sed.api.service.periodo_academico.PeriodoAcademicoService;
import co.edu.unicauca.sed.api.service.proceso.ProcesoService;
import co.edu.unicauca.sed.api.specification.ConsolidadoSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsolidadoServiceImpl implements ConsolidadoService {

    private static final Logger logger = LoggerFactory.getLogger(ConsolidadoServiceImpl.class);

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ProcesoRepository procesoRepository;
    @Autowired private ActividadRepository actividadRepository;
    @Autowired private ActividadCalculoService calculoService;
    @Autowired private ActividadTransformacionService transformacionService;
    @Autowired private PeriodoAcademicoService periodoAcademicoService;
    @Autowired private ActividadQueryService actividadQueryService;
    @Autowired private ExcelService excelService;
    @Autowired private ConsolidadoRepository consolidadoRepository;
    @Autowired private ProcesoService procesoService;
    @Autowired private NotificacionDocumentoService notificacionDocumentoService;
    @Autowired private ConsolidadoHelper consolidadoHelper;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<InformacionConsolidadoDTO>> findAll(Pageable pageable, Boolean ascendingOrder,
            Integer idPeriodoAcademico, Integer idUsuario, String nombre, String identificacion,
            String facultad, String departamento, String categoria) {
        try {
            boolean order = (ascendingOrder != null) ? ascendingOrder : true;
            Sort sort = order ? Sort.by("fechaCreacion").ascending() : Sort.by("fechaCreacion").descending();
            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

            if (idPeriodoAcademico == null) {
                idPeriodoAcademico = periodoAcademicoService.obtenerIdPeriodoAcademicoActivo();
            }

            ConsolidadoSpecification specBuilder = new ConsolidadoSpecification(periodoAcademicoService);
            Specification<Consolidado> specification = specBuilder.byFilters(idUsuario, nombre, identificacion, facultad, departamento, categoria, idPeriodoAcademico);

            Page<Consolidado> consolidadoPage = consolidadoRepository.findAll(specification, sortedPageable);

            if (consolidadoPage.isEmpty()) {
                return new ApiResponse<>(204, "No se encontraron consolidados con los filtros aplicados.", Page.empty());
            }

            Page<InformacionConsolidadoDTO> dtoPage = consolidadoPage.map(consolidadoHelper::convertirAInformacionDTO);
            return new ApiResponse<>(200, "Consolidados obtenidos correctamente.", dtoPage);

        } catch (Exception e) {
            logger.error("❌ [ERROR] Error al obtener la lista de consolidados: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error inesperado al obtener los consolidados.", Page.empty());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Consolidado> findByOid(Integer oid) {
        try {
            Consolidado consolidado = consolidadoRepository.findById(oid).orElseThrow(() -> new EntityNotFoundException("Consolidado con ID " + oid + " no encontrado."));
            return new ApiResponse<>(200, "Consolidado encontrado correctamente.", consolidado);
        } catch (EntityNotFoundException e) {
            logger.warn("⚠️ [ERROR] {}", e.getMessage());
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("❌ [ERROR] Error al buscar consolidado con ID {}: {}", oid, e.getMessage(), e);
            return new ApiResponse<>(500, "Error inesperado al buscar el consolidado.", null);
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> updateAllFromConsolidado(Integer oidConsolidado, Consolidado datosActualizar) {
        try {
            logger.info("🔄 Actualizando consolidado con OID: {}", oidConsolidado);
            Consolidado consolidadoBase = consolidadoRepository.findById(oidConsolidado)
                .orElseThrow(() -> new EntityNotFoundException("Consolidado con ID " + oidConsolidado + " no encontrado."));

            Proceso procesoBase = Optional.ofNullable(consolidadoBase.getProceso()).orElseThrow(() -> new IllegalStateException("Proceso no asociado al consolidado base."));

            Usuario evaluado = Optional.ofNullable(procesoBase.getEvaluado()).orElseThrow(() -> new IllegalStateException("Evaluado no asociado al proceso base."));

            List<Proceso> procesosEvaluado = procesoRepository.findByEvaluado(evaluado);
            if (procesosEvaluado.isEmpty()) {
                return new ApiResponse<>(404, "No hay procesos asociados al evaluado.", null);
            }

            int actualizados = 0;
            for (Proceso proceso : procesosEvaluado) {
                consolidadoRepository.findByProceso(proceso).ifPresent(consolidado -> {
                    consolidadoHelper.actualizarDatosConsolidado(consolidado, datosActualizar.getNombredocumento(), datosActualizar.getRutaDocumento(), datosActualizar.getNota(), datosActualizar.getCalificacion());
                });
                actualizados++;
            }

            if (actualizados == 0) {
                return new ApiResponse<>(404, "No se actualizaron consolidado(s) porque no se encontraron registros.",null);
            }

            return new ApiResponse<>(200, "Se actualizaron " + actualizados + " consolidado(s) correctamente.", null);

        } catch (EntityNotFoundException e) {
            logger.warn("⚠️ [ERROR] {}", e.getMessage());
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (IllegalStateException e) {
            logger.warn("⚠️ [ERROR] {}", e.getMessage());
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("❌ [ERROR] Error inesperado al actualizar consolidado(s): {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error inesperado al actualizar consolidado(s).", null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<ConsolidadoDTO> generarInformacionGeneral(Integer idEvaluado, Integer idPeriodoAcademico) {
        try {
            BaseConsolidadoDataDTO baseData = consolidadoHelper.obtenerBaseConsolidado(idEvaluado, idPeriodoAcademico);

            List<Actividad> actividades = baseData.getProcesos().stream().flatMap(proceso -> proceso.getActividades().stream()).collect(Collectors.toList());

            float totalHoras = calculoService.calcularTotalHoras(actividades);

            Map<String, List<Map<String, Object>>> actividadesPorTipo = transformacionService.agruparActividadesPorTipo(actividades, totalHoras);

            double totalPorcentaje = calculoService.calcularTotalPorcentaje(actividadesPorTipo);
            double totalAcumulado = calculoService.calcularTotalAcumulado(actividadesPorTipo);
            ConsolidadoDTO consolidadoDTO = consolidadoHelper.construirConsolidado(baseData.getEvaluado(), baseData.getDetalleUsuario(), baseData.getPeriodoAcademico(),
                null, totalHoras, totalPorcentaje, totalAcumulado);
            return new ApiResponse<>(200, "Información general obtenida correctamente.", consolidadoDTO);
        } catch (EntityNotFoundException e) {
            logger.warn("⚠️ [ERROR] {}", e.getMessage());
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("❌ [ERROR] Error inesperado al obtener información general: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error inesperado al obtener la información general del consolidado.", null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<ActividadPaginadaDTO> filtrarActividadesPaginadas(Integer idEvaluado, Integer idPeriodoAcademico,
            String nombreActividad, String idTipoActividad, String idTipoFuente, String idEstadoFuente, Pageable pageable) {
        try {
            Specification<Actividad> spec = actividadQueryService.filtrarActividades(null, idEvaluado, nombreActividad, idTipoActividad, null, null,
                idTipoFuente, idEstadoFuente, true, idPeriodoAcademico, false);

            Page<Actividad> actividadPage = actividadRepository.findAll(spec, pageable);
            ActividadPaginadaDTO actividadPaginadaDTO = transformacionService.construirActividadPaginadaDTO(actividadPage);
            return new ApiResponse<>(200, "Actividades obtenidas correctamente.", actividadPaginadaDTO);

        } catch (Exception e) {
            logger.error("❌ [ERROR] Error inesperado al obtener actividades paginadas: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error inesperado al obtener actividades paginadas.", null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<ConsolidadoArchivoDTO> aprobarConsolidado(Integer idEvaluado, Integer idEvaluador,
            Integer idPeriodoAcademico, String nota) {
        try {
            if (idPeriodoAcademico == null) {
                idPeriodoAcademico = periodoAcademicoService.obtenerIdPeriodoAcademicoActivo();
            }

            ConsolidadoDTO consolidadoDTO = consolidadoHelper.generarConsolidadoConActividades(idEvaluado, idPeriodoAcademico, Pageable.unpaged());

            if (nota != null) {
                nota = nota.toUpperCase();
            }

            String nombreDocumento = consolidadoHelper.generarNombreDocumento(consolidadoDTO);
            Path excelPath = excelService.generarExcelConsolidado(consolidadoDTO, nombreDocumento, nota);

            Usuario evaluador = usuarioRepository.findById(idEvaluador)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el evaluador con ID: " + idEvaluador));

            Usuario evaluado = usuarioRepository.findById(idEvaluado)
                    .orElseThrow(() -> new EntityNotFoundException("No se encontró el evaluado con ID: " + idEvaluado));

            Proceso procesoExistente = procesoService.buscarProcesoExistente(idEvaluador, idEvaluado, idPeriodoAcademico, procesoService.TIPO_CONSOLIDADO);

            if (procesoExistente == null) {
                procesoExistente = procesoService.crearNuevoProceso(idEvaluador, idEvaluado, idPeriodoAcademico);
            }

            Consolidado consolidadoExistente = consolidadoRepository.findByProceso(procesoExistente).orElse(null);
            if (consolidadoExistente == null) {
                consolidadoExistente = new Consolidado(procesoExistente);
                logger.info("✅ [CONSOLIDADO] Creando un nuevo consolidado para el proceso ID: {}", procesoExistente.getOidProceso());
            }

            Integer oidConsolidado = consolidadoHelper.guardarConsolidado(consolidadoExistente, nombreDocumento, excelPath.toString(), nota, consolidadoDTO.getTotalAcumulado());
            notificacionDocumentoService.notificarJefeDepartamento("consolidado", evaluador, evaluado);
            ConsolidadoArchivoDTO archivoDTO = new ConsolidadoArchivoDTO(nombreDocumento, oidConsolidado);
            return new ApiResponse<>(200, "Consolidado aprobado correctamente.", archivoDTO);
        } catch (IOException e) {
            logger.error("❌ [ERROR] Error al generar el archivo de consolidado: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error al generar el archivo de consolidado.", null);
        } catch (EntityNotFoundException e) {
            logger.warn("⚠️ [ERROR] {}", e.getMessage());
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("❌ [ERROR] Error inesperado en aprobarConsolidado: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error inesperado al aprobar el consolidado.", null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<InformacionConsolidadoDTO>> obtenerTodos() {
        try {
            List<Consolidado> consolidadoList = consolidadoRepository.findAll();

            if (consolidadoList.isEmpty()) {
                return new ApiResponse<>(204, "No se encontraron consolidados.", List.of());
            }

            List<InformacionConsolidadoDTO> dtoList = consolidadoList.stream().map(consolidadoHelper::convertirAInformacionDTO).toList();

            return new ApiResponse<>(200, "Consolidados obtenidos correctamente.", dtoList);

        } catch (Exception e) {
            logger.error("❌ [ERROR] Error al obtener todos los consolidados: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error inesperado al obtener los consolidados.", List.of());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<HistoricoCalificacionesDTO>> obtenerHistoricoCalificaciones(
            List<Integer> periodos, Integer idUsuario, String nombre, String identificacion,
            String facultad, String departamento, String categoria, Pageable pageable) {

        try {
            ConsolidadoSpecification specBuilder = new ConsolidadoSpecification(periodoAcademicoService);
            Specification<Consolidado> spec = specBuilder.byMultiplePeriodos(idUsuario, periodos, nombre, identificacion, facultad, departamento, categoria);

            List<Consolidado> consolidadoList = consolidadoRepository.findAll(spec);

            if (consolidadoList.isEmpty()) {
                return new ApiResponse<>(204, "No se encontraron consolidados con los filtros aplicados.", Page.empty());
            }

            // Agrupar por usuario
            Map<Integer, List<Consolidado>> agrupadoPorUsuario = consolidadoList.stream().collect(Collectors.groupingBy(c -> c.getProceso().getEvaluado().getOidUsuario()));

            List<HistoricoCalificacionesDTO> todos = agrupadoPorUsuario.entrySet().stream()
                .map(entry -> consolidadoHelper.construirHistoricoDTO(entry.getKey(), entry.getValue())).filter(Objects::nonNull).toList();

            // Paginación manual
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), todos.size());
            List<HistoricoCalificacionesDTO> paginados = (start < end) ? todos.subList(start, end) : List.of();

            Page<HistoricoCalificacionesDTO> pageResult = new PageImpl<>(paginados, pageable, todos.size());

            return new ApiResponse<>(200, "Histórico de calificaciones obtenido correctamente.", pageResult);

        } catch (Exception e) {
            logger.error("❌ [ERROR] Error al obtener histórico de calificaciones: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error inesperado al obtener el histórico de calificaciones.", Page.empty());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayResource generarExcel(Boolean ascendingOrder,
            Integer idPeriodoAcademico, Integer idUsuario, String nombre,
            String identificacion, String facultad, String departamento,
            String categoria) throws IOException {

        List<InformacionConsolidadoDTO> data = buscarConsolidadosSinPaginacion(ascendingOrder, idPeriodoAcademico, idUsuario, nombre, identificacion, facultad, departamento, categoria);

        String[] headers = { "Nombre", "Identificación", "Facultad", "Departamento", "Categoría", "Calificación" };

        ByteArrayOutputStream excelStream = excelService.generarExcelInformacionConsolidado(data, headers);
        return new ByteArrayResource(excelStream.toByteArray());
    }

    @Transactional(readOnly = true)
    public List<InformacionConsolidadoDTO> buscarConsolidadosSinPaginacion(Boolean ascendingOrder, Integer idPeriodoAcademico, Integer idUsuario, String nombre, String identificacion, String facultad, String departamento, String categoria) {

        boolean order = (ascendingOrder != null) ? ascendingOrder : true;
        Sort sort = order ? Sort.by("fechaCreacion").ascending() : Sort.by("fechaCreacion").descending();

        if (idPeriodoAcademico == null) {
            idPeriodoAcademico = periodoAcademicoService.obtenerIdPeriodoAcademicoActivo();
        }

        ConsolidadoSpecification specBuilder = new ConsolidadoSpecification(periodoAcademicoService);
        Specification<Consolidado> specification = specBuilder.byFilters(idUsuario, nombre, identificacion, facultad, departamento, categoria, idPeriodoAcademico);

        List<Consolidado> consolidadoList = consolidadoRepository.findAll(specification, sort);

        return consolidadoList.stream().map(consolidadoHelper::convertirAInformacionDTO).toList();
    }

    public List<HistoricoCalificacionesDTO> obtenerHistoricoSinPaginacion(List<Integer> periodos, Integer idUsuario, String nombre, String identificacion, String facultad, String departamento, String categoria) {

        ConsolidadoSpecification specBuilder = new ConsolidadoSpecification(periodoAcademicoService);
        Specification<Consolidado> spec = specBuilder.byMultiplePeriodos(idUsuario, periodos, nombre, identificacion, facultad, departamento, categoria);


        List<Consolidado> consolidadoList = consolidadoRepository.findAll(spec);
        if (consolidadoList.isEmpty())
            return List.of();

        // Agrupar por usuario
        Map<Integer, List<Consolidado>> agrupadoPorUsuario = consolidadoList.stream().collect(Collectors.groupingBy(c -> c.getProceso().getEvaluado().getOidUsuario()));

        return agrupadoPorUsuario.entrySet().stream().map(entry -> consolidadoHelper.construirHistoricoDTO(entry.getKey(), entry.getValue())).filter(Objects::nonNull).toList();
    }


    @Override
    @Transactional(readOnly = true)
    public ByteArrayResource generarExcelHistorico(List<Integer> periodos, Integer idUsuario, String nombre, String identificacion, String facultad, String departamento, String categoria) throws IOException {

        List<HistoricoCalificacionesDTO> data = obtenerHistoricoSinPaginacion(periodos, idUsuario, nombre, identificacion, facultad, departamento, categoria);

        ByteArrayOutputStream stream = excelService.generarExcelHistorico(data);
        return new ByteArrayResource(stream.toByteArray());
    }
}
