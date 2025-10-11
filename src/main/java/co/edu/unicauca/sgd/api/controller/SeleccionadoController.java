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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.SeleccionadoDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.SeleccionadoDTOResponse;
import co.edu.unicauca.sgd.api.service.usuario.laborDocente.SeleccionadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/seleccionado")
@Tag(name = "Seleccionado", description = "Gestión de usuarios seleccionados por calendario")
public class SeleccionadoController {

    private final SeleccionadoService seleccionadoService;

    public SeleccionadoController(SeleccionadoService seleccionadoService) {
        this.seleccionadoService = seleccionadoService;
    }

    @GetMapping
    @Operation(summary = "Listar seleccionados", description = "Filtros opcionales: oidCalendario, oidDepartamento (filtra por departamento del usuario).")
    public ResponseEntity<ApiResponse<Page<SeleccionadoDTOResponse>>> findAll(
            @RequestParam(required = false) Integer oidCalendario,
            @RequestParam(required = false) Integer oidDepartamento,
            Pageable pageable) {

        ApiResponse<Page<SeleccionadoDTOResponse>> response =
                seleccionadoService.obtenerTodos(oidCalendario, oidDepartamento, pageable);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar seleccionado por ID", description = "Consulta un seleccionado por su ID")
    public ResponseEntity<ApiResponse<SeleccionadoDTOResponse>> findByOid(@PathVariable Integer oid) {
        ApiResponse<SeleccionadoDTOResponse> response = seleccionadoService.buscarPorId(oid);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Guardar seleccionado", description = "Agrega un usuario a la lista de seleccionados para un calendario")
    public ResponseEntity<ApiResponse<SeleccionadoDTOResponse>> save(@Valid @RequestBody SeleccionadoDTORequest dto) {
        ApiResponse<SeleccionadoDTOResponse> response = seleccionadoService.guardar(dto);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PutMapping("/{oid}")
    @Operation(summary = "Actualizar seleccionado", description = "Actualiza campos permitidos de un seleccionado (no se permite cambiar calendario/usuario)")
    public ResponseEntity<ApiResponse<SeleccionadoDTOResponse>> update(@PathVariable Integer oid,
                                                                       @Valid @RequestBody SeleccionadoDTORequest dto) {
        ApiResponse<SeleccionadoDTOResponse> response = seleccionadoService.actualizar(oid, dto);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar seleccionado", description = "Elimina un seleccionado por su ID")
    public ResponseEntity<ApiResponse<Map<String, Object>>> delete(@PathVariable Integer oid) {
        ApiResponse<Void> response = seleccionadoService.eliminar(oid);
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

