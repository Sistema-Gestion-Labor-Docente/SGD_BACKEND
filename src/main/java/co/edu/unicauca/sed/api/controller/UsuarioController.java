package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.Usuario;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.DocenteEvaluacionDTO;
import co.edu.unicauca.sed.api.service.fuente.DocenteEvaluacionService;
import co.edu.unicauca.sed.api.service.usuario.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.List;
import org.springframework.core.io.Resource;

@RestController
@RequestMapping("api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios y evaluación docente")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private DocenteEvaluacionService docenteEvaluacionService;

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Obtiene todos los usuarios registrados con filtros opcionales")
    public ResponseEntity<ApiResponse<Page<Usuario>>> findAll(
            @RequestParam(required = false) String identificacion,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String facultad,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String contratacion,
            @RequestParam(required = false) String dedicacion,
            @RequestParam(required = false) String estudios,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String programa,
            Pageable pageable) {
        ApiResponse<Page<Usuario>> response = usuarioService.obtenerTodos(identificacion, nombre, facultad,
                departamento, categoria, contratacion,
                dedicacion, estudios, rol, estado, programa, pageable);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar usuario por ID", description = "Consulta un usuario específico por su ID")
    public ResponseEntity<ApiResponse<Usuario>> findByOid(
            @Parameter(description = "ID del usuario") @PathVariable Integer oid) {
        ApiResponse<Usuario> response = usuarioService.buscarPorId(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Guardar usuarios", description = "Guarda una lista de nuevos usuarios")
    public ResponseEntity<ApiResponse<List<Usuario>>> save(
            @RequestBody(description = "Lista de usuarios a guardar", required = true)
            @org.springframework.web.bind.annotation.RequestBody List<Usuario> usuarios) {
        ApiResponse<List<Usuario>> response = usuarioService.guardar(usuarios);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PutMapping("/{idUsuario}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza un usuario existente")
    public ResponseEntity<ApiResponse<Usuario>> update(
            @Parameter(description = "ID del usuario a actualizar") @PathVariable Integer idUsuario,
            @RequestBody(description = "Datos actualizados del usuario", required = true)
            @org.springframework.web.bind.annotation.RequestBody Usuario usuarioActualizado) {
        ApiResponse<Usuario> response = usuarioService.actualizar(idUsuario, usuarioActualizado);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario por su ID")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID del usuario a eliminar") @PathVariable Integer oid) {
        ApiResponse<Void> response = usuarioService.eliminar(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/obtenerEvaluacionDocente")
    @Operation(summary = "Obtener evaluaciones docentes", description = "Consulta evaluaciones de docentes con filtros opcionales")
    public ResponseEntity<ApiResponse<Page<DocenteEvaluacionDTO>>> obtenerEvaluacionDocentes(
            @RequestParam(required = false) Integer idEvaluado,
            @RequestParam(required = false) Integer idPeriodoAcademico,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tipoContrato,
            @RequestParam(required = false) String identificacion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
    
        ApiResponse<Page<DocenteEvaluacionDTO>> response = docenteEvaluacionService.obtenerEvaluacionDocentes(
                idEvaluado, idPeriodoAcademico, departamento, nombre, tipoContrato, identificacion, PageRequest.of(page, size)
        );
    
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/logueado")
    @Operation(summary = "Obtener usuario actual", description = "Obtiene el usuario actualmente autenticado")
    public ResponseEntity<ApiResponse<Usuario>> obtenerUsuarioActual(Authentication authentication) {
        try {
            String correo = authentication.getName();
            Usuario usuario = usuarioService.obtenerUsuarioActual(correo);
            return ResponseEntity.ok(new ApiResponse<>(200, "Usuario obtenido correctamente", usuario));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al obtener el usuario: " + e.getMessage(), null));
        }
    }

    @GetMapping("/exportar-evaluacion-docente")
    public ResponseEntity<Resource> exportarEvaluacionDocenteExcel(
            @RequestParam(required = false) Integer idEvaluado,
            @RequestParam(required = false) Integer idPeriodoAcademico,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tipoContrato,
            @RequestParam(required = false) String identificacion) throws IOException {

        ByteArrayResource resource = docenteEvaluacionService.exportarEvaluacionDocenteExcel(
                idEvaluado, idPeriodoAcademico, departamento, nombre, tipoContrato, identificacion);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=EvaluacionesDocentes.xlsx")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }
}
