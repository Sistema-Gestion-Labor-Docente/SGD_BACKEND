package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.UsuarioDetalle;
import co.edu.unicauca.sed.api.service.usuario.UsuarioDetalleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/usuario-detalle")
@Tag(name = "Usuario Detalle", description = "Gestión de detalles de usuario")
public class UsuarioDetalleController {

    @Autowired
    private UsuarioDetalleService usuarioDetalleService;

    @GetMapping
    @Operation(summary = "Listar detalles de usuario", description = "Obtiene todos los detalles de usuario registrados")
    public ResponseEntity<?> findAll() {
        try {
            List<UsuarioDetalle> list = usuarioDetalleService.obtenerTodos();
            if (list != null && !list.isEmpty()) {
                return ResponseEntity.ok().body(list);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar detalle por ID", description = "Consulta un detalle de usuario específico por su ID")
    public ResponseEntity<?> find(
            @Parameter(description = "ID del detalle de usuario") @PathVariable Integer oid) {
        UsuarioDetalle resultado = usuarioDetalleService.buscarPorOid(oid);
        if (resultado != null) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Guardar detalle de usuario", description = "Registra un nuevo detalle de usuario")
    public ResponseEntity<?> save(
            @RequestBody(description = "Objeto UsuarioDetalle a guardar", required = true)
            @org.springframework.web.bind.annotation.RequestBody UsuarioDetalle usuarioDetalle) {
        try {
            UsuarioDetalle resultado = usuarioDetalleService.guardar(usuarioDetalle);
            if (resultado != null) {
                return ResponseEntity.ok().body(resultado);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
        return ResponseEntity.internalServerError().body("Error: Resultado nulo");
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar detalle de usuario", description = "Elimina un detalle de usuario por su ID")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID del detalle de usuario a eliminar") @PathVariable Integer oid) {
        UsuarioDetalle usuarioDetalle = null;
        try {
            usuarioDetalle = usuarioDetalleService.buscarPorOid(oid);
            if (usuarioDetalle == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("UsuarioDetalle no encontrado");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("UsuarioDetalle no encontrado");
        }

        try {
            usuarioDetalleService.eliminar(oid);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("No se puede borrar por conflictos con otros datos");
        }
        return ResponseEntity.ok().build();
    }
}
