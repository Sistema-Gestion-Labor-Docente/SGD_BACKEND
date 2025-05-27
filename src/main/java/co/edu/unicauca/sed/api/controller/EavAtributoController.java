package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.EavAtributo;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.service.EavAtributoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

/**
 * Controlador REST para gestionar EAV Atributos.
 */
@RestController
@RequestMapping("/api/eavatributo")
@RequiredArgsConstructor
public class EavAtributoController {

    private final EavAtributoService eavAtributoService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EavAtributo>>> listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return eavAtributoService.obtenerEavAtributos(page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EavAtributo>> buscarPorId(@PathVariable Integer id) {
        return eavAtributoService.obtenerEavAtributoPorId(id);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EavAtributo>> crear(@RequestBody EavAtributo atributo) {
        return eavAtributoService.crearEavAtributo(atributo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EavAtributo>> actualizar(@PathVariable Integer id,
            @RequestBody EavAtributo atributo) {
        return eavAtributoService.actualizarEavAtributo(id, atributo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        return eavAtributoService.eliminarEavAtributo(id);
    }
}
