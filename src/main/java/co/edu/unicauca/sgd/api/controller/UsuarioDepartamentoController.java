package co.edu.unicauca.sgd.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.UsuarioDepartamentoDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.UsuarioDepartamentoDTOResponse;
import co.edu.unicauca.sgd.api.service.materias.UsuarioDepartamentoService;

@RestController
@RequestMapping("api/usuario-departamentos")
@Tag(name = "Usuario-Departamento", description = "Gestión de la asignación de usuario a departamento")
public class UsuarioDepartamentoController {

    private UsuarioDepartamentoService service;

    public UsuarioDepartamentoController(@Autowired UsuarioDepartamentoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar asignaciones", description = "Lista asignaciones con filtros opcionales")
    public ResponseEntity<ApiResponse<Page<UsuarioDepartamentoDTOResponse>>> findAll(
            @RequestParam(required = false) Integer oidUsuario,
            @RequestParam(required = false) Integer oidDepartamento,
            Pageable pageable) {

        ApiResponse<Page<UsuarioDepartamentoDTOResponse>> response =
                service.obtenerTodos(oidUsuario, oidDepartamento, pageable);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/{oidUsuario}")
    @Operation(summary = "Buscar por usuario", description = "Obtiene la asignación del usuario")
    public ResponseEntity<ApiResponse<UsuarioDepartamentoDTOResponse>> findByUsuario(@PathVariable Integer oidUsuario) {
        ApiResponse<UsuarioDepartamentoDTOResponse> response = service.buscarPorUsuario(oidUsuario);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Crear asignación", description = "Asigna un departamento a un usuario")
    public ResponseEntity<ApiResponse<UsuarioDepartamentoDTOResponse>> save(@Valid @RequestBody UsuarioDepartamentoDTORequest dto) {
        ApiResponse<UsuarioDepartamentoDTOResponse> response = service.guardar(dto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PutMapping("/{oidUsuario}")
    @Operation(summary = "Actualizar asignación", description = "Reasigna el departamento de un usuario")
    public ResponseEntity<ApiResponse<UsuarioDepartamentoDTOResponse>> update(
            @PathVariable Integer oidUsuario,
            @Valid @RequestBody UsuarioDepartamentoDTORequest dto) {

        ApiResponse<UsuarioDepartamentoDTOResponse> response = service.actualizar(oidUsuario, dto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oidUsuario}")
    @Operation(summary = "Eliminar asignación", description = "Elimina la asignación del usuario")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer oidUsuario) {
        ApiResponse<Void> response = service.eliminar(oidUsuario);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }
}
