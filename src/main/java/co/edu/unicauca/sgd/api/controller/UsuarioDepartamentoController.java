package co.edu.unicauca.sgd.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.UsuarioDepartamentoDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.UsuarioDepartamentoDTOResponse;
import co.edu.unicauca.sgd.api.service.materias.UsuarioDepartamentoService;

@RestController
@RequestMapping("api/departamentos/usuarios")
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
            @RequestParam(required = false) String identificacion,
            @RequestParam(required = false) String nombreCompleto,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String contratacion,
            @RequestParam(required = false) String dedicacion,
            Pageable pageable) {

        ApiResponse<Page<UsuarioDepartamentoDTOResponse>> response =
                service.obtenerTodos(
                        oidUsuario,
                        oidDepartamento,
                        identificacion,
                        nombreCompleto,
                        correo,
                        contratacion,
                        dedicacion,
                        pageable);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @GetMapping("/{oidUsuario}")
    @Operation(summary = "Buscar por usuario", description = "Obtiene la asignación del usuario")
    public ResponseEntity<ApiResponse<UsuarioDepartamentoDTOResponse>> findByUsuario(@PathVariable Integer oidUsuario) {
        ApiResponse<UsuarioDepartamentoDTOResponse> response = service.buscarPorUsuario(oidUsuario);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Crear asignación", description = "Asigna un departamento a un usuario")
    public ResponseEntity<ApiResponse<UsuarioDepartamentoDTOResponse>> save(@Valid @RequestBody UsuarioDepartamentoDTORequest dto) {
        ApiResponse<UsuarioDepartamentoDTOResponse> response = service.guardar(dto);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PutMapping("/{oidUsuario}")
    @Operation(summary = "Actualizar asignación", description = "Reasigna el departamento de un usuario")
    public ResponseEntity<ApiResponse<UsuarioDepartamentoDTOResponse>> update(
            @PathVariable Integer oidUsuario,
            @Valid @RequestBody UsuarioDepartamentoDTORequest dto) {

        ApiResponse<UsuarioDepartamentoDTOResponse> response = service.actualizar(oidUsuario, dto);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oidUsuario}")
    @Operation(summary = "Eliminar asignación", description = "Elimina la asignación del usuario")
    public ResponseEntity<ApiResponse<Map<String, Object>>> delete(@PathVariable Integer oidUsuario) {
        ApiResponse<Void> response = service.eliminar(oidUsuario);
        if (response.getCodigo() >= 200 && response.getCodigo() < 300) {
            java.util.Map<String, Object> info = java.util.Map.of(
                "oidUsuario", oidUsuario,
                "mensaje", response.getMensaje()
            );
            return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo())
                    .body(new ApiResponse<>(response.getCodigo(), response.getMensaje(), info));
        }
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo())
                .body(new ApiResponse<>(response.getCodigo(), response.getMensaje(), null));
    }

    @GetMapping("/actividades")
    @Operation(summary = "Listar profesores por tipo de actividades",
        description = "Permite obtener profesores que tienen actividades de tipo DOCENCIA o actividades diferentes a DOCENCIA, según el parámetro indicado")
    public ResponseEntity<ApiResponse<List<UsuarioDepartamentoDTOResponse>>> listarProfesoresPorTipo(
            @RequestParam(defaultValue = "DOCENCIA") String filtro,
            @RequestParam Integer oidDepartamento) {
        ApiResponse<List<UsuarioDepartamentoDTOResponse>> response = service.obtenerProfesoresPorTipoActividad(filtro, oidDepartamento);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }
}
