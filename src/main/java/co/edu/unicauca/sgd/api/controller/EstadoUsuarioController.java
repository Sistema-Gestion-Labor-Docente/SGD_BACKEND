package co.edu.unicauca.sgd.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.domain.EstadoUsuario;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.service.usuario.EstadoUsuarioService;

@RestController
@RequestMapping("api/estado-usuario")
public class EstadoUsuarioController {

    @Autowired
    private EstadoUsuarioService service;

    @PostMapping
    public ResponseEntity<ApiResponse<EstadoUsuario>> crear(@RequestBody EstadoUsuario estadoUsuario) {
        return ResponseEntity.ok(service.crear(estadoUsuario));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EstadoUsuario>> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> buscarTodos(Pageable pageable) {
        return ResponseEntity.ok(service.buscarTodos(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EstadoUsuario>> actualizar(@PathVariable Integer id, @RequestBody EstadoUsuario estadoUsuario) {
        return ResponseEntity.ok(service.actualizar(id, estadoUsuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.eliminar(id));
    }
}
