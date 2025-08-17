package co.edu.unicauca.sgd.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTOResponse;
import co.edu.unicauca.sgd.api.service.calendario.CalendarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/calendarios")
@Tag(name = "Calendario", description = "Gestión del calendario académico")
public class CalendarioController {

    private CalendarioService calendarioService;

    public CalendarioController(CalendarioService calendarioService) {
        this.calendarioService = calendarioService;
    }

    @GetMapping
    @Operation(summary = "Listar calendarios", description = "Filtros opcionales: anioCalendario, numeroCalendario, estado")
    public ResponseEntity<ApiResponse<Page<CalendarioDTOResponse>>> findAll(
            @RequestParam(required = false) String anioCalendario,
            @RequestParam(required = false) Integer numeroCalendario,
            @RequestParam(required = false) String estado,
            Pageable pageable) {
        ApiResponse<Page<CalendarioDTOResponse>> response =
                calendarioService.obtenerTodos(anioCalendario, numeroCalendario, estado, pageable);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar calendario por ID", description = "Consulta un calendario específico por su ID")
    public ResponseEntity<ApiResponse<CalendarioDTOResponse>> findByOid(@PathVariable Integer oid) {
        ApiResponse<CalendarioDTOResponse> response = calendarioService.buscarPorId(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Guardar calendario", description = "Guarda un nuevo calendario")
    public ResponseEntity<ApiResponse<CalendarioDTOResponse>> save(@Valid @RequestBody CalendarioDTORequest dto) {
        ApiResponse<CalendarioDTOResponse> response = calendarioService.guardar(dto);
        // Si tu ApiResponse ya pone 201, esto lo respeta:
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PutMapping("/{oid}")
    @Operation(summary = "Actualizar calendario", description = "Actualiza un calendario existente")
    public ResponseEntity<ApiResponse<CalendarioDTOResponse>> update(@PathVariable Integer oid, @Valid @RequestBody CalendarioDTORequest dto) {
        ApiResponse<CalendarioDTOResponse> response = calendarioService.actualizar(oid, dto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar calendario", description = "Elimina un calendario por su ID")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer oid) {
        ApiResponse<Void> response = calendarioService.eliminar(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }
}
