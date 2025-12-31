package co.edu.unicauca.sgd.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadEstadoMasivoRequest;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadEstadoPorOidRequest;
import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;
import co.edu.unicauca.sgd.api.service.necesidad.NecesidadEstadoService;

@RestController
@RequestMapping("api/necesidades/estado")
@Tag(name = "Necesidad - Estados", description = "Transiciones de estado de necesidades")
public class NecesidadEstadoController {

    private final NecesidadEstadoService necesidadEstadoService;

    public NecesidadEstadoController(NecesidadEstadoService necesidadEstadoService) {
        this.necesidadEstadoService = necesidadEstadoService;
    }

    @PatchMapping("/borrador/en-revision-secretario")
    @Operation(summary = "Enviar necesidades a revisión de secretario", description = "Cambia de BORRADOR a EN REVISION SECRETARIO las necesidades del calendario y programa indicados")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toRevisionSecretario(
            @RequestParam Integer oidCalendario,
            @RequestParam Integer oidPrograma,
            @RequestBody(required = false) NecesidadEstadoMasivoRequest request) {
        ApiResponse<Map<String, Object>> response = necesidadEstadoService.cambiarEstadoMasivo(
                oidCalendario,
                EstadoNecesidad.BORRADOR,
                EstadoNecesidad.EN_REVISION_SECRETARIO,
                oidPrograma,
                null,
                request != null ? request.getOidNecesidades() : null);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PatchMapping("/en-revision-secretario/borrador")
    @Operation(summary = "Regresar necesidades a borrador", description = "Cambia de EN REVISION SECRETARIO a BORRADOR las necesidades del calendario y programa indicados")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toBorradorDesdeSecretario(
            @RequestParam Integer oidCalendario,
            @RequestParam Integer oidPrograma,
            @RequestBody(required = false) NecesidadEstadoMasivoRequest request) {
        ApiResponse<Map<String, Object>> response = necesidadEstadoService.cambiarEstadoMasivo(
                oidCalendario,
                EstadoNecesidad.EN_REVISION_SECRETARIO,
                EstadoNecesidad.BORRADOR,
                oidPrograma,
                null,
                request != null ? request.getOidNecesidades() : null);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PatchMapping("/en-revision-secretario/en-revision-jefe")
    @Operation(summary = "Enviar necesidades a revisión de jefe", description = "Cambia de EN REVISION SECRETARIO a EN REVISION JEFE las necesidades del calendario")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toRevisionJefe(
            @RequestParam Integer oidCalendario,
            @RequestParam(required = false) Integer oidPrograma,
            @RequestParam(required = false) Integer oidDepartamento,
            @RequestBody(required = false) NecesidadEstadoMasivoRequest request) {
        ApiResponse<Map<String, Object>> response = necesidadEstadoService.cambiarEstadoMasivo(
                oidCalendario,
                EstadoNecesidad.EN_REVISION_SECRETARIO,
                EstadoNecesidad.EN_REVISION_JEFE,
                oidPrograma,
                oidDepartamento,
                request != null ? request.getOidNecesidades() : null);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PatchMapping("/en-revision-jefe/en-revision-secretario")
    @Operation(summary = "Regresar necesidades a revisión de secretario", description = "Cambia de EN REVISION JEFE a EN REVISION SECRETARIO las necesidades del calendario")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toRevisionSecretarioDesdeJefe(
            @RequestParam Integer oidCalendario,
            @RequestParam(required = false) Integer oidPrograma,
            @RequestParam(required = false) Integer oidDepartamento,
            @RequestBody(required = false) NecesidadEstadoMasivoRequest request) {
        ApiResponse<Map<String, Object>> response = necesidadEstadoService.cambiarEstadoMasivo(
                oidCalendario,
                EstadoNecesidad.EN_REVISION_JEFE,
                EstadoNecesidad.EN_REVISION_SECRETARIO,
                oidPrograma,
                oidDepartamento,
                request != null ? request.getOidNecesidades() : null);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PatchMapping("/en-revision-jefe/no-asignada")
    @Operation(summary = "Cerrar revisión de jefe", description = "Cambia de EN REVISION JEFE a NO ASIGNADA las necesidades del calendario")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toNoAsignada(
            @RequestParam Integer oidCalendario,
            @RequestParam Integer oidDepartamento,
            @RequestBody(required = false) NecesidadEstadoMasivoRequest request) {
        ApiResponse<Map<String, Object>> response = necesidadEstadoService.cambiarEstadoMasivo(
                oidCalendario,
                EstadoNecesidad.EN_REVISION_JEFE,
                EstadoNecesidad.NO_ASIGNADA,
                null,
                oidDepartamento,
                request != null ? request.getOidNecesidades() : null);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PatchMapping("/por-oid")
    @Operation(summary = "Cambiar estado por necesidades específicas",
            description = "Actualiza el estado de un conjunto de necesidades identificadas por sus OID")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cambiarEstadoPorOid(
            @Valid @RequestBody NecesidadEstadoPorOidRequest request) {
        ApiResponse<Map<String, Object>> response = necesidadEstadoService.cambiarEstadoPorOids(
                request.getOidNecesidades(),
                request.getEstadoOrigen(),
                request.getEstadoDestino());
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }
}
