package co.edu.unicauca.sgd.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadBulkCreateRequest;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadBulkCreateResponse;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTORequest;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTOResponse;
import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;
import co.edu.unicauca.sgd.api.service.necesidad.NecesidadService;

@RestController
@RequestMapping("api/necesidades")
@Tag(name = "Necesidad", description = "Gestión de necesidades de docentes")
public class NecesidadController {

    private final NecesidadService necesidadService;

    public NecesidadController(NecesidadService necesidadService) {
        this.necesidadService = necesidadService;
    }

    @GetMapping
    @Operation(summary = "Listar necesidades", description = "Obtiene las necesidades filtrando por calendario y, opcionalmente, programa, materia, estado o departamento")
    public ResponseEntity<ApiResponse<Page<NecesidadDTOResponse>>> findAll(
            @RequestParam Integer oidCalendario,
            @RequestParam(required = false) Integer idMateria,
            @RequestParam(required = false) EstadoNecesidad estado,
            @RequestParam(required = false) Integer oidPrograma,
            @RequestParam(required = false) Integer oidDepartamento,
            @RequestParam(required = false) String nombreMateria,
            @RequestParam(required = false) Integer semestreMateria,
            @RequestParam(required = false) String codigoMateria,
            Pageable pageable) {
        ApiResponse<Page<NecesidadDTOResponse>> response =
                necesidadService.obtenerTodos(
                        oidCalendario,
                        idMateria,
                        estado,
                        oidPrograma,
                        oidDepartamento,
                        nombreMateria,
                        semestreMateria,
                        codigoMateria,
                        pageable);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar necesidad por ID", description = "Consulta una necesidad por su identificador")
    public ResponseEntity<ApiResponse<NecesidadDTOResponse>> findByOid(@PathVariable Integer oid) {
        ApiResponse<NecesidadDTOResponse> response = necesidadService.buscarPorId(oid);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Crear necesidad", description = "Registra una nueva necesidad para un calendario")
    public ResponseEntity<ApiResponse<NecesidadDTOResponse>> save(@Valid @RequestBody NecesidadDTORequest request) {
        ApiResponse<NecesidadDTOResponse> response = necesidadService.guardar(request);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PostMapping("/lote")
    @Operation(summary = "Crear necesidades por lote", description = "Crea múltiples necesidades para un calendario a partir de una lista de materias")
    public ResponseEntity<ApiResponse<NecesidadBulkCreateResponse>> saveBulk(
            @Valid @RequestBody NecesidadBulkCreateRequest request) {
        ApiResponse<NecesidadBulkCreateResponse> response = necesidadService.guardarMasivo(request);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PutMapping("/{oid}")
    @Operation(summary = "Actualizar necesidad", description = "Actualiza los datos principales de una necesidad")
    public ResponseEntity<ApiResponse<NecesidadDTOResponse>> update(
            @PathVariable Integer oid,
            @RequestBody NecesidadDTORequest request) {
        ApiResponse<NecesidadDTOResponse> response = necesidadService.actualizar(oid, request);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar necesidad", description = "Elimina una necesidad registrada")
    public ResponseEntity<ApiResponse<Map<String, Object>>> delete(@PathVariable Integer oid) {
        ApiResponse<Void> response = necesidadService.eliminar(oid);
        if (response.getCodigo() >= 200 && response.getCodigo() < 300) {
            java.util.Map<String, Object> info = java.util.Map.of(
                "oid", oid,
                "mensaje", response.getMensaje()
            );
            return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo())
                    .body(new ApiResponse<>(response.getCodigo(), response.getMensaje(), info));
        }
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo())
                .body(new ApiResponse<>(response.getCodigo(), response.getMensaje(), null));
    }
}

