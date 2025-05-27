package co.edu.unicauca.sed.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import co.edu.unicauca.sed.api.domain.Consolidado;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.ConsolidadoArchivoDTO;
import co.edu.unicauca.sed.api.dto.ConsolidadoDTO;
import co.edu.unicauca.sed.api.dto.HistoricoCalificacionesDTO;
import co.edu.unicauca.sed.api.dto.InformacionConsolidadoDTO;
import co.edu.unicauca.sed.api.dto.actividad.ActividadPaginadaDTO;
import co.edu.unicauca.sed.api.repository.ConsolidadoRepository;
import co.edu.unicauca.sed.api.service.consolidado.ConsolidadoService;
import co.edu.unicauca.sed.api.service.documento.ExcelService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("api/consolidado")
public class ConsolidadoController {

    private static final Logger logger = LoggerFactory.getLogger(ConsolidadoController.class);

    @Autowired
    private ConsolidadoService consolidadoService;

    @Autowired
    private ConsolidadoRepository consolidadoRepository;

    @Autowired
    private ExcelService excelService;

    @GetMapping("/obtener-todos")
    public ResponseEntity<ApiResponse<List<InformacionConsolidadoDTO>>> obtenerTodos() {
        ApiResponse<List<InformacionConsolidadoDTO>> response = consolidadoService.obtenerTodos();
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<InformacionConsolidadoDTO>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "true") Boolean ascendingOrder,
            @RequestParam(required = false) Integer idPeriodoAcademico,
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String identificacion,
            @RequestParam(required = false) String facultad,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String categoria) {

        ApiResponse<Page<InformacionConsolidadoDTO>> response = consolidadoService.findAll(PageRequest.of(page, size), ascendingOrder,
            idPeriodoAcademico,idUsuario, nombre, identificacion, facultad, departamento, categoria);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    public ResponseEntity<ApiResponse<Consolidado>> findById(@PathVariable Integer oid) {
        ApiResponse<Consolidado> response = consolidadoService.findByOid(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PutMapping("/{oidConsolidado}")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable Integer oidConsolidado,
            @RequestBody Consolidado consolidado) {
        ApiResponse<Void> response = consolidadoService.updateAllFromConsolidado(oidConsolidado, consolidado);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    /**
     * Endpoint para obtener solo la información general del consolidado sin
     * actividades.
     */
    @GetMapping("/informacion-general")
    public ResponseEntity<ApiResponse<ConsolidadoDTO>> obtenerInformacionGeneral(
            @RequestParam Integer idEvaluado,
            @RequestParam(required = false) Integer periodoAcademico) {
        ApiResponse<ConsolidadoDTO> response = consolidadoService.generarInformacionGeneral(idEvaluado, periodoAcademico);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    /**
     * Endpoint para obtener solo las actividades del consolidado paginadas.
     */
    @GetMapping("/actividades")
    public ResponseEntity<ApiResponse<ActividadPaginadaDTO>> obtenerActividadesPaginadas(
            @RequestParam Integer idEvaluado,
            @RequestParam(required = false) Integer periodoAcademico,
            @RequestParam(required = false) String nombreActividad,
            @RequestParam(required = false) String idTipoActividad,
            @RequestParam(required = false) String idTipoFuente,
            @RequestParam(required = false) String idEstadoFuente,
            Pageable pageable) {
        ApiResponse<ActividadPaginadaDTO> response = consolidadoService.filtrarActividadesPaginadas(
                idEvaluado, periodoAcademico, nombreActividad, idTipoActividad, idTipoFuente, idEstadoFuente, pageable);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    /**
     * Endpoint para aprobar el consolidado y generar el Excel.
     */
    @PostMapping("/aprobar")
    public ResponseEntity<ApiResponse<ConsolidadoArchivoDTO>> aprobarConsolidado(
            @RequestParam Integer idEvaluado,
            @RequestParam Integer idEvaluador,
            @RequestParam(required = false) Integer periodoAcademico,
            @RequestParam(required = false) String nota) {
        ApiResponse<ConsolidadoArchivoDTO> response = consolidadoService.aprobarConsolidado(idEvaluado, idEvaluador, periodoAcademico, nota);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/descargar-consolidado")
    public ResponseEntity<Resource> descargarConsolidado(@RequestParam Integer idConsolidado) {
        Optional<Consolidado> consolidadoOptional = consolidadoRepository.findById(idConsolidado);

        if (consolidadoOptional.isEmpty()) {
            throw new EntityNotFoundException("No se encontró el consolidado con ID: " + idConsolidado);
        }

        Consolidado consolidado = consolidadoOptional.get();
        Path archivoPath = Paths.get(consolidado.getRutaDocumento());

        if (!Files.exists(archivoPath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(archivoPath));

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + archivoPath.getFileName().toString() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
        } catch (IOException e) {
            logger.error("❌ [ERROR] No se pudo leer el archivo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/historico-calificaciones")
    public ResponseEntity<ApiResponse<Page<HistoricoCalificacionesDTO>>> obtenerHistoricoCalificaciones(
            @RequestParam(name = "periodos") List<Integer> periodos,
            @RequestParam(name = "idUsuario", required = false) Integer idUsuario,
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "identificacion", required = false) String identificacion,
            @RequestParam(name = "facultad", required = false) String facultad,
            @RequestParam(name = "departamento", required = false) String departamento,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
    
        Pageable pageable = PageRequest.of(page, size);
    
        ApiResponse<Page<HistoricoCalificacionesDTO>> response = consolidadoService.obtenerHistoricoCalificaciones(
                periodos, idUsuario, nombre, identificacion, facultad, departamento, categoria, pageable
        );
    
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/exportar-informacion-general")
    public ResponseEntity<Resource> exportarConsolidadoExcel(
            @RequestParam(defaultValue = "true") Boolean ascendingOrder,
            @RequestParam(required = false) Integer idPeriodoAcademico,
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String identificacion,
            @RequestParam(required = false) String facultad,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String categoria) throws IOException {

        ByteArrayResource excelResource = consolidadoService.generarExcel(ascendingOrder, idPeriodoAcademico, idUsuario, nombre, identificacion, facultad, departamento, categoria);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ConsolidadoResumen.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelResource);
    }

    @GetMapping("/exportar-historico")
    public ResponseEntity<ByteArrayResource> exportarHistoricoExcel(
            @RequestParam List<Integer> periodos,
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String identificacion,
            @RequestParam(required = false) String facultad,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String categoria) {
        try {
            ByteArrayResource recursoExcel = consolidadoService.generarExcelHistorico(
                periodos, idUsuario, nombre, identificacion, facultad, departamento, categoria
            );

            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=historico_calificaciones.xlsx")
                .contentLength(recursoExcel.contentLength())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(recursoExcel);

        } catch (IOException e) {
            logger.error("❌ [ERROR] No se pudo generar el archivo Excel: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
