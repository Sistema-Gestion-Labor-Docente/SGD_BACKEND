package co.edu.unicauca.sgd.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.MateriaDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.MateriaDTOResponse;
import co.edu.unicauca.sgd.api.exception.materias.MateriaValidationException;
import co.edu.unicauca.sgd.api.service.materias.MateriaService;

@RestController
@RequestMapping("api/materias")
@Tag(name = "Materia", description = "Gestión de materias")
public class MateriaController {

    private MateriaService materiaService;

    public MateriaController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }

    @GetMapping
    @Operation(summary = "Listar materias", description = "Obtiene todas las materias con filtros opcionales")
    public ResponseEntity<ApiResponse<Page<MateriaDTOResponse>>> findAll(
            @RequestParam(required = false) String oidmateria,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Integer semestre,
            @RequestParam(required = false) Integer oidDepartamento,
            @RequestParam(required = true) Integer oidPlan,
            Pageable pageable) {

        ApiResponse<Page<MateriaDTOResponse>> response =
                materiaService.obtenerTodos(oidmateria, codigo, nombre, semestre, oidDepartamento, oidPlan, pageable);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar materia por ID", description = "Consulta una materia por su ID")
    public ResponseEntity<ApiResponse<MateriaDTOResponse>> findByOid(@PathVariable Integer oid) {
        ApiResponse<MateriaDTOResponse> response = materiaService.buscarPorId(oid);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Guardar materia", description = "Crea una nueva materia")
    public ResponseEntity<ApiResponse<MateriaDTOResponse>> save(@Valid @RequestBody MateriaDTORequest dto) {
        ApiResponse<MateriaDTOResponse> response = materiaService.guardar(dto);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar materia", description = "Actualiza una materia existente")
    public ResponseEntity<ApiResponse<MateriaDTOResponse>> update(
            @PathVariable Integer id, @Valid @RequestBody MateriaDTORequest dto) {
        ApiResponse<MateriaDTOResponse> response = materiaService.actualizar(id, dto);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar materia", description = "Elimina una materia por su ID")
    public ResponseEntity<ApiResponse<Map<String, Object>>> delete(@PathVariable Integer oid) {
        ApiResponse<Void> response = materiaService.eliminar(oid);
        if (response.getCodigo() >= 200 && response.getCodigo() < 300) {
            java.util.Map<String, Object> info = java.util.Map.of(
                "oid", oid,
                "mensaje", response.getMensaje()
            );
            return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo())
                    .body(new ApiResponse<>(response.getCodigo(), response.getMensaje(), info));
        }
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo())
                .body(new ApiResponse<>(response.getCodigo(), response.getMensaje(), null));
    }

    @GetMapping("/libres")
    @Operation(
            summary = "Listar materias sin correquisito",
            description = "Obtiene las materias que no tienen correquisito ni son correquisito de otra materia")
    public ResponseEntity<ApiResponse<Page<MateriaDTOResponse>>> findFreeSubjects(
            @RequestParam(required = false) Integer oidDepartamento,
            @RequestParam(required = true) Integer oidPlan,
            Pageable pageable) {
        ApiResponse<Page<MateriaDTOResponse>> response =
                materiaService.obtenerMateriasSinCorrequisitoNiReferencias(oidDepartamento, oidPlan, pageable);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @GetMapping("/buscar-disponibles")
    @Operation(
            summary = "Buscar materias para agregar a un plan",
            description = "Busca materias por OID, código y/o nombre excluyendo el plan indicado.")
    public ResponseEntity<ApiResponse<Page<MateriaDTOResponse>>> buscarDisponiblesParaPlan(
            @RequestParam(required = false) String oidmateria,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = true) Integer oidPlan,
            Pageable pageable) {

        if ((oidmateria == null || oidmateria.isBlank())
                && (codigo == null || codigo.isBlank())
                && (nombre == null || nombre.isBlank())) {
            throw new MateriaValidationException("Debe enviar al menos uno de: oidMateria, código o nombre.");
        }

        ApiResponse<Page<MateriaDTOResponse>> response =
                materiaService.buscarPorIdentificadoresExcluyendoPlan(oidmateria, codigo, nombre, oidPlan, pageable);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }
}

