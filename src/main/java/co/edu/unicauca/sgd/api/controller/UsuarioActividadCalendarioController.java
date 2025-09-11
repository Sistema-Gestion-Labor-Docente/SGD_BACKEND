package co.edu.unicauca.sgd.api.controller;

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
    public ResponseEntity<ApiResponse<Page<UsuarioActividadCalendarioDTOResponse>>> findAll(Pageable pageable) {
        ApiResponse<Page<UsuarioActividadCalendarioDTOResponse>> response =
                usuarioActividadCalendarioService.listarActividadesConRelaciones(pageable);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/{oidActividad}")
    @Operation(summary = "Buscar actividad por ID con sus relaciones", description = "Obtiene una actividad con todos los usuarios y calendario relacionados")
    public ResponseEntity<ApiResponse<UsuarioActividadCalendarioDTOResponse>> findByOid(@PathVariable Integer oidActividad) {
        ApiResponse<UsuarioActividadCalendarioDTOResponse> response =
                usuarioActividadCalendarioService.obtenerActividadConRelaciones(oidActividad);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Crear actividad con usuarios y calendario", description = "Crea una nueva actividad y relaciones usuario/calendario")
    public ResponseEntity<ApiResponse<UsuarioActividadCalendarioDTOResponse>> save(@Valid @RequestBody UsuarioActividadCalendarioDTORequest dto) {
        ApiResponse<UsuarioActividadCalendarioDTOResponse> response =
                usuarioActividadCalendarioService.crearActividadConRelaciones(dto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PutMapping("/{oidActividad}")
    @Operation(summary = "Actualizar actividad y relaciones", description = "Actualiza la actividad y todas sus relaciones usuario/calendario")
    public ResponseEntity<ApiResponse<UsuarioActividadCalendarioDTOResponse>> update(@PathVariable Integer oidActividad, @Valid @RequestBody UsuarioActividadCalendarioDTORequest dto) {
        ApiResponse<UsuarioActividadCalendarioDTOResponse> response =
                usuarioActividadCalendarioService.actualizarActividadConRelaciones(oidActividad, dto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oidActividad}")
    @Operation(summary = "Eliminar actividad y todas sus relaciones", description = "Elimina la actividad y sus relaciones usuario/calendario")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer oidActividad) {
        ApiResponse<Void> response = usuarioActividadCalendarioService.eliminarActividad(oidActividad);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oidActividad}/relacion")
    @Operation(summary = "Eliminar una relación usuario-actividad-calendario", description = "Elimina solo la relación sin borrar la actividad")
    public ResponseEntity<ApiResponse<Void>> deleteRelacion(
            @PathVariable Integer oidActividad,
            @RequestParam Integer oidUsuario,
            @RequestParam Integer oidCalendario) {
        ApiResponse<Void> response = usuarioActividadCalendarioService.eliminarRelacion(oidActividad, oidUsuario, oidCalendario);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }
}

