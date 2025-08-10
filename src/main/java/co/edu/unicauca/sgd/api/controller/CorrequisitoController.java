package co.edu.unicauca.sgd.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.CorrequisitoListaResponse;
import co.edu.unicauca.sgd.api.dto.materias.CorrequisitoPairDTO;
import co.edu.unicauca.sgd.api.service.materias.CorrequisitoService;

import java.util.List;

@RestController
@RequestMapping("api/materias/correquisitos")
@Tag(name = "Co-requisitos", description = "Gestión de co-requisitos entre materias")
public class CorrequisitoController {

    private CorrequisitoService service;

    public CorrequisitoController(@Autowired CorrequisitoService service) {
        this.service = service;
    }

    @GetMapping("/{oidMateria}")
    @Operation(summary = "Listar co-requisitos de una materia")
    public ResponseEntity<ApiResponse<CorrequisitoListaResponse>> listar(@PathVariable Integer oidMateria) {
        ApiResponse<CorrequisitoListaResponse> r = service.listar(oidMateria);
        return ResponseEntity.status(r.getCodigo()).body(r);
    }

    @PostMapping
    @Operation(summary = "Agregar co-requisito (par simétrico)")
    public ResponseEntity<ApiResponse<Void>> agregar(@Valid @RequestBody CorrequisitoPairDTO dto) {
        ApiResponse<Void> r = service.agregar(dto);
        return ResponseEntity.status(r.getCodigo()).body(r);
    }

    @DeleteMapping
    @Operation(summary = "Eliminar co-requisito (par)")
    public ResponseEntity<ApiResponse<Void>> eliminar(@Valid @RequestBody CorrequisitoPairDTO dto) {
        ApiResponse<Void> r = service.eliminar(dto);
        return ResponseEntity.status(r.getCodigo()).body(r);
    }

    @PutMapping("/{oidMateria}")
    @Operation(summary = "Reemplazar co-requisitos de una materia por un nuevo conjunto")
    public ResponseEntity<ApiResponse<CorrequisitoListaResponse>> reemplazar(
            @PathVariable Integer oidMateria,
            @RequestBody List<Integer> nuevosCorrequisitos) {
        ApiResponse<CorrequisitoListaResponse> r = service.reemplazar(oidMateria, nuevosCorrequisitos);
        return ResponseEntity.status(r.getCodigo()).body(r);
    }
}
