package co.edu.unicauca.sgd.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.CatalogoDTO;
import co.edu.unicauca.sgd.api.service.CatalogoService;

@RestController
@RequestMapping("api/catalogo")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/obtenerCatalogo")
    public ResponseEntity<ApiResponse<CatalogoDTO>> obtenerCatalogo() {
        try {
            ApiResponse<CatalogoDTO> response = catalogoService.obtenerCatalogo();
            return ResponseEntity.status(response.getCodigo()).body(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(500, "Error al obtener el catálogo: " + e.getMessage(), null));
        }
    }
}
