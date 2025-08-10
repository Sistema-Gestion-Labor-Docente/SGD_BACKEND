package co.edu.unicauca.sgd.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.DepartamentoDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.DepartamentoDTOResponse;
import co.edu.unicauca.sgd.api.service.materias.DepartamentoService;

@RestController
@RequestMapping("api/departamentos")
@Tag(name = "Departamento", description = "Gestión de departamentos académicos")
public class DepartamentoController {

    private DepartamentoService departamentoService;

    public DepartamentoController(@Autowired DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

    @GetMapping
    @Operation(summary = "Listar departamentos", description = "Obtiene todos los departamentos con filtros opcionales")
    public ResponseEntity<ApiResponse<Page<DepartamentoDTOResponse>>> findAll(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        ApiResponse<Page<DepartamentoDTOResponse>> response =
                departamentoService.obtenerTodos(nombre, pageable);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar departamento por ID", description = "Consulta un departamento por su ID")
    public ResponseEntity<ApiResponse<DepartamentoDTOResponse>> findByOid(@PathVariable Integer oid) {
        ApiResponse<DepartamentoDTOResponse> response = departamentoService.buscarPorId(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Guardar departamento", description = "Crea un nuevo departamento")
    public ResponseEntity<ApiResponse<DepartamentoDTOResponse>> save(@Valid @RequestBody DepartamentoDTORequest dto) {
        ApiResponse<DepartamentoDTOResponse> response = departamentoService.guardar(dto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar departamento", description = "Actualiza un departamento existente")
    public ResponseEntity<ApiResponse<DepartamentoDTOResponse>> update(
            @PathVariable Integer id, @Valid @RequestBody DepartamentoDTORequest dto) {
        ApiResponse<DepartamentoDTOResponse> response = departamentoService.actualizar(id, dto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar departamento", description = "Elimina un departamento por su ID")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer oid) {
        ApiResponse<Void> response = departamentoService.eliminar(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }
}
