package co.edu.unicauca.sgd.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.domain.ObjetivoComponente;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.service.fuente.ObjetivoComponenteService;

@RestController
@RequestMapping("/api/objetivos-componente")
@RequiredArgsConstructor
public class ObjetivoComponenteController {

    private final ObjetivoComponenteService objetivoComponenteService;

    @PostMapping
    public ApiResponse<ObjetivoComponente> guardar(@RequestBody ObjetivoComponente objetivoComponente) {
        return new ApiResponse<>(200, "Objetivo de componente guardado correctamente.", objetivoComponenteService.guardar(objetivoComponente));
    }

    @GetMapping("/{id}")
    public ApiResponse<ObjetivoComponente> buscarPorId(@PathVariable Integer id) {
        return new ApiResponse<>(200, "Objetivo de componente encontrado.", objetivoComponenteService.buscarPorId(id));
    }

    @GetMapping
    public ApiResponse<?> listar(Pageable pageable) {
        return new ApiResponse<>(200, "Listado de objetivos de componente.", objetivoComponenteService.listar(pageable));
    }
}
