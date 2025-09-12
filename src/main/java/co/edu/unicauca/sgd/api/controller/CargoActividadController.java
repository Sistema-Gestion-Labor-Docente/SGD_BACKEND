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
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.CargoActividadDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.CargoActividadDTOResponse;
import co.edu.unicauca.sgd.api.service.actividad.laborDocente.CargoActividadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/cargos-actividad")
@Tag(name = "CargoActividad", description = "Gestión de cargos de actividad")
public class CargoActividadController {

    private final CargoActividadService cargoActividadService;

    public CargoActividadController(CargoActividadService cargoActividadService) {
        this.cargoActividadService = cargoActividadService;
    }

    @GetMapping
    @Operation(summary = "Listar cargos de actividad", description = "Filtros opcionales: nombre, tipo, oidTipoActividad")
    public ResponseEntity<ApiResponse<Page<CargoActividadDTOResponse>>> findAll(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer oidTipoActividad,
            Pageable pageable) {
        ApiResponse<Page<CargoActividadDTOResponse>> response =
                cargoActividadService.obtenerTodos(nombre, tipo, oidTipoActividad, pageable);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar cargo de actividad por ID")
    public ResponseEntity<ApiResponse<CargoActividadDTOResponse>> findByOid(@PathVariable Integer oid) {
        ApiResponse<CargoActividadDTOResponse> response = cargoActividadService.buscarPorId(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Guardar cargo de actividad")
    public ResponseEntity<ApiResponse<CargoActividadDTOResponse>> save(@Valid @RequestBody CargoActividadDTORequest dto) {
        ApiResponse<CargoActividadDTOResponse> response = cargoActividadService.guardar(dto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PutMapping("/{oid}")
    @Operation(summary = "Actualizar cargo de actividad")
    public ResponseEntity<ApiResponse<CargoActividadDTOResponse>> update(@PathVariable Integer oid, @Valid @RequestBody CargoActividadDTORequest dto) {
        ApiResponse<CargoActividadDTOResponse> response = cargoActividadService.actualizar(oid, dto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar cargo de actividad")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer oid) {
        ApiResponse<Void> response = cargoActividadService.eliminar(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }
}

