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
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.configuracion.ConfiguracionGeneralDTORequest;
import co.edu.unicauca.sgd.api.dto.configuracion.ConfiguracionGeneralDTOResponse;
import co.edu.unicauca.sgd.api.service.configuracion.ConfiguracionGeneralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/configuraciones")
@Tag(name = "ConfiguracionGeneral", description = "Gestión de configuraciones generales")
public class ConfiguracionGeneralController {

    private final ConfiguracionGeneralService configuracionGeneralService;

    public ConfiguracionGeneralController(ConfiguracionGeneralService configuracionGeneralService) {
        this.configuracionGeneralService = configuracionGeneralService;
    }

    @GetMapping
    @Operation(summary = "Listar configuraciones", description = "Obtiene todas las configuraciones")
    public ResponseEntity<ApiResponse<Page<ConfiguracionGeneralDTOResponse>>> findAll(Pageable pageable) {
        ApiResponse<Page<ConfiguracionGeneralDTOResponse>> response =
                configuracionGeneralService.obtenerTodos(pageable);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar configuración por ID", description = "Consulta una configuración por su ID")
    public ResponseEntity<ApiResponse<ConfiguracionGeneralDTOResponse>> findByOid(@PathVariable Integer oid) {
        ApiResponse<ConfiguracionGeneralDTOResponse> response = configuracionGeneralService.buscarPorId(oid);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Guardar configuración", description = "Crea una nueva configuración")
    public ResponseEntity<ApiResponse<ConfiguracionGeneralDTOResponse>> save(
            @Valid @RequestBody ConfiguracionGeneralDTORequest dto) {
        ApiResponse<ConfiguracionGeneralDTOResponse> response = configuracionGeneralService.guardar(dto);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PutMapping("/{oid}")
    @Operation(summary = "Actualizar configuración", description = "Actualiza una configuración existente")
    public ResponseEntity<ApiResponse<ConfiguracionGeneralDTOResponse>> update(
            @PathVariable Integer oid,
            @Valid @RequestBody ConfiguracionGeneralDTORequest dto) {
        ApiResponse<ConfiguracionGeneralDTOResponse> response = configuracionGeneralService.actualizar(oid, dto);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar configuración", description = "Elimina una configuración por su ID")
    public ResponseEntity<ApiResponse<Map<String, Object>>> delete(@PathVariable Integer oid) {
        ApiResponse<Void> response = configuracionGeneralService.eliminar(oid);
        if (response.getCodigo() >= 200 && response.getCodigo() < 300) {
            Map<String, Object> info = Map.of(
                "oid", oid,
                "mensaje", response.getMensaje()
            );
            return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo())
                    .body(new ApiResponse<>(response.getCodigo(), response.getMensaje(), info));
        }
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo())
                .body(new ApiResponse<>(response.getCodigo(), response.getMensaje(), null));
    }

    @GetMapping("/sed-url")
    @Operation(summary = "Obtener URL de SED", description = "Obtiene la URL base de SED desde configuración")
    public ResponseEntity<ApiResponse<String>> obtenerSedUrl() {
        ApiResponse<String> response = configuracionGeneralService.obtenerSedUrl();
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }
}
