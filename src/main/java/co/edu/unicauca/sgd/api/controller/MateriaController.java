package co.edu.unicauca.sgd.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.MateriaDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.MateriaDTOResponse;
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
            @RequestParam(required = false) Integer oidPlan,
            Pageable pageable) {

        ApiResponse<Page<MateriaDTOResponse>> response =
                materiaService.obtenerTodos(oidmateria, codigo, nombre, semestre, oidDepartamento, oidPlan, pageable);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar materia por ID", description = "Consulta una materia por su ID")
    public ResponseEntity<ApiResponse<MateriaDTOResponse>> findByOid(@PathVariable Integer oid) {
        ApiResponse<MateriaDTOResponse> response = materiaService.buscarPorId(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Guardar materia", description = "Crea una nueva materia")
    public ResponseEntity<ApiResponse<MateriaDTOResponse>> save(@Valid @RequestBody MateriaDTORequest dto) {
        ApiResponse<MateriaDTOResponse> response = materiaService.guardar(dto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar materia", description = "Actualiza una materia existente")
    public ResponseEntity<ApiResponse<MateriaDTOResponse>> update(
            @PathVariable Integer id, @Valid @RequestBody MateriaDTORequest dto) {
        ApiResponse<MateriaDTOResponse> response = materiaService.actualizar(id, dto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar materia", description = "Elimina una materia por su ID")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer oid) {
        ApiResponse<Void> response = materiaService.eliminar(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }
}
