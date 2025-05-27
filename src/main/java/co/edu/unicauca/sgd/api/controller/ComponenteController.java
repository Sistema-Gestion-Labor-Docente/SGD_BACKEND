package co.edu.unicauca.sgd.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.domain.Componente;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.service.fuente.ComponenteService;

@RestController
@RequestMapping("/api/componentes")
@RequiredArgsConstructor
public class ComponenteController {

    private final ComponenteService componenteService;

    @PostMapping
    public ApiResponse<Componente> guardar(@RequestBody Componente componente) {
        return new ApiResponse<>(200, "Componente guardado correctamente.", componenteService.guardar(componente));
    }

    @GetMapping("/{id}")
    public ApiResponse<Componente> buscarPorId(@PathVariable Integer id) {
        return new ApiResponse<>(200, "Componente encontrado.", componenteService.buscarPorId(id));
    }

    @GetMapping
    public ApiResponse<?> listar(Pageable pageable) {
        return new ApiResponse<>(200, "Listado de componentes.", componenteService.listar(pageable));
    }
}
