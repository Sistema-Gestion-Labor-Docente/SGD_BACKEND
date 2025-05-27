package co.edu.unicauca.sed.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sed.api.domain.PeriodoAcademico;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.PeriodoExternoDTO;
import co.edu.unicauca.sed.api.service.periodo_academico.PeriodoAcademicoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;

/**
 * Controlador para la gestión de los períodos académicos.
 */
@Controller
@RequestMapping("api/periodos-academicos")
@Tag(name = "Períodos Académicos", description = "Operaciones para la gestión de los períodos académicos")
public class PeriodoAcademicoController {

    @Autowired
    private PeriodoAcademicoService periodoAcademicoService;

    @GetMapping
    @Operation(
        summary = "Listar períodos académicos",
        description = "Retorna todos los períodos académicos registrados con paginación."
    )
    public ResponseEntity<ApiResponse<Page<PeriodoAcademico>>> findAll(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size) {
        ApiResponse<Page<PeriodoAcademico>> response = periodoAcademicoService.obtenerTodos(PageRequest.of(page, size));
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    @Operation(
        summary = "Buscar período académico por ID",
        description = "Obtiene un período académico a partir de su identificador único."
    )
    public ResponseEntity<ApiResponse<PeriodoAcademico>> find(
            @Parameter(description = "ID del período académico") @PathVariable Integer oid) {
        ApiResponse<PeriodoAcademico> response = periodoAcademicoService.buscarPorId(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(
        summary = "Guardar período académico",
        description = "Registra un nuevo período académico en el sistema."
    )
    public ResponseEntity<ApiResponse<PeriodoAcademico>> save(
            @RequestBody(description = "Datos del período académico a registrar", required = true)
            @org.springframework.web.bind.annotation.RequestBody PeriodoAcademico periodoAcademico) {
        ApiResponse<PeriodoAcademico> response = periodoAcademicoService.guardar(periodoAcademico);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PutMapping("/{oid}")
    @Operation(
        summary = "Actualizar período académico",
        description = "Actualiza los datos de un período académico existente según su ID."
    )
    public ResponseEntity<ApiResponse<Void>> update(
            @Parameter(description = "ID del período académico a actualizar") @PathVariable Integer oid,
            @RequestBody(description = "Datos actualizados del período académico", required = true)
            @org.springframework.web.bind.annotation.RequestBody PeriodoAcademico periodoAcademico) {
        ApiResponse<Void> response = periodoAcademicoService.actualizar(oid, periodoAcademico);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oid}")
    @Operation(
        summary = "Eliminar período académico",
        description = "Elimina un período académico del sistema por su identificador."
    )
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID del período académico a eliminar") @PathVariable Integer oid) {
        ApiResponse<Void> response = periodoAcademicoService.eliminar(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/activo")
    @Operation(
        summary = "Obtener período académico activo",
        description = "Consulta el período académico que se encuentra actualmente activo."
    )

    public ResponseEntity<ApiResponse<PeriodoAcademico>> obtenerPeriodoAcademicoActivo() {
        ApiResponse<PeriodoAcademico> response = periodoAcademicoService.obtenerPeriodoAcademicoActivo();
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/kira")
    @Operation(
        summary = "Obtener períodos no registrados",
        description = "Consulta los períodos académicos disponibles en el sistema externo KIRA que aún no han sido registrados localmente."
    )
    public ResponseEntity<ApiResponse<List<PeriodoExternoDTO>>> obtenerNoRegistrados() {
        return ResponseEntity.ok(periodoAcademicoService.obtenerPeriodosNoRegistrados());
    }
}
