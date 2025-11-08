package co.edu.unicauca.sgd.api.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.DocenciaDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTOResponse;
import co.edu.unicauca.sgd.api.service.actividad.laborDocente.UsuarioActividadCalendarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/usuario-actividad-calendario")
@Tag(name = "UsuarioActividadCalendario", description = "Gestión de actividades, usuarios y calendario")
public class UsuarioActividadCalendarioController {

    private final UsuarioActividadCalendarioService usuarioActividadCalendarioService;

    public UsuarioActividadCalendarioController(UsuarioActividadCalendarioService usuarioActividadCalendarioService) {
        this.usuarioActividadCalendarioService = usuarioActividadCalendarioService;
    }

    @GetMapping
    @Operation(summary = "Listar actividades con relaciones", description = "Lista todas las actividades y sus relaciones usuario/calendario")
    public ResponseEntity<ApiResponse<Page<UsuarioActividadCalendarioDTOResponse>>> findAll(
            @RequestParam(name = "oidCalendario", required = true) Integer oidCalendario,
            @RequestParam(name = "oidDepartamento", required = true) Integer oidDepartamento,
            @RequestParam(name = "oidTipoActividad", required = false) Integer oidTipoActividad,
            @RequestParam(name = "oidEstadoActividad", required = false) Integer oidEstadoActividad,
            @RequestParam(name = "oidUsuarioResponsable", required = false) Integer oidUsuarioResponsable,
            Pageable pageable) {
        ApiResponse<Page<UsuarioActividadCalendarioDTOResponse>> response =
                usuarioActividadCalendarioService.listarActividadesConRelaciones(
                        oidCalendario, oidDepartamento, oidTipoActividad, oidEstadoActividad, oidUsuarioResponsable, pageable);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @GetMapping("/{oidActividad}")
    @Operation(summary = "Buscar actividad por ID con sus relaciones", description = "Obtiene una actividad con todos los usuarios y calendario relacionados")
    public ResponseEntity<ApiResponse<UsuarioActividadCalendarioDTOResponse>> findByOid(@PathVariable Integer oidActividad) {
        ApiResponse<UsuarioActividadCalendarioDTOResponse> response =
                usuarioActividadCalendarioService.obtenerActividadConRelaciones(oidActividad);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Crear actividad con usuarios y calendario", description = "Crea una nueva actividad y relaciones usuario/calendario")
    public ResponseEntity<ApiResponse<UsuarioActividadCalendarioDTOResponse>> save(@Valid @RequestBody UsuarioActividadCalendarioDTORequest dto) {
        ApiResponse<UsuarioActividadCalendarioDTOResponse> response =
                usuarioActividadCalendarioService.crearActividadConRelaciones(dto);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PutMapping("/{oidActividad}")
    @Operation(summary = "Actualizar actividad y relaciones", description = "Actualiza la actividad y todas sus relaciones usuario/calendario")
    public ResponseEntity<ApiResponse<UsuarioActividadCalendarioDTOResponse>> update(@PathVariable Integer oidActividad, @Valid @RequestBody UsuarioActividadCalendarioDTORequest dto) {
        ApiResponse<UsuarioActividadCalendarioDTOResponse> response =
                usuarioActividadCalendarioService.actualizarActividadConRelaciones(oidActividad, dto);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oidActividad}")
    @Operation(summary = "Eliminar actividad y todas sus relaciones", description = "Elimina la actividad y sus relaciones usuario/calendario")
    public ResponseEntity<ApiResponse<Map<String, Object>>> delete(@PathVariable Integer oidActividad) {
        ApiResponse<Void> response = usuarioActividadCalendarioService.eliminarActividad(oidActividad);
        if (response.getCodigo() >= 200 && response.getCodigo() < 300) {
            java.util.Map<String, Object> info = java.util.Map.of(
                "oidActividad", oidActividad,
                "mensaje", response.getMensaje()
            );
            return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo())
                    .body(new ApiResponse<>(response.getCodigo(), response.getMensaje(), info));
        }
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo())
                .body(new ApiResponse<>(response.getCodigo(), response.getMensaje(), null));
    }

    @DeleteMapping("/{oidActividad}/relacion")
    @Operation(summary = "Eliminar una relación usuario-actividad-calendario", description = "Elimina solo la relación sin borrar la actividad")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteRelacion(
            @PathVariable Integer oidActividad,
            @RequestParam Integer oidUsuario,
            @RequestParam Integer oidCalendario) {
        ApiResponse<Void> response = usuarioActividadCalendarioService.eliminarRelacion(oidActividad, oidUsuario, oidCalendario);
        if (response.getCodigo() >= 200 && response.getCodigo() < 300) {
            java.util.Map<String, Object> info = java.util.Map.of(
                "oidActividad", oidActividad,
                "oidUsuario", oidUsuario,
                "oidCalendario", oidCalendario,
                "mensaje", response.getMensaje()
            );
            return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo())
                    .body(new ApiResponse<>(response.getCodigo(), response.getMensaje(), info));
        }
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo())
                .body(new ApiResponse<>(response.getCodigo(), response.getMensaje(), null));
    }

    // Otros métodos específicos pueden ser añadidos aquí según sea necesario

    @GetMapping("/tipo/docencia")
    @Operation(summary = "Listar actividades de tipo Docencia", description = "Lista las actividades cuyo tipo es 'Docencia' con su DTO específico")
    public ResponseEntity<ApiResponse<Page<DocenciaDTOResponse>>> listarDocencia(Pageable pageable) {
        ApiResponse<Page<DocenciaDTOResponse>> response = usuarioActividadCalendarioService.listarPorTipoDocencia(pageable);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

}


