package co.edu.unicauca.sgd.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadEstadoPorOidRequest;
import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;
import co.edu.unicauca.sgd.api.service.necesidad.NecesidadEstadoService;

class NecesidadEstadoControllerTest {

    @Mock
    private NecesidadEstadoService necesidadEstadoService;

    private NecesidadEstadoController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new NecesidadEstadoController(necesidadEstadoService);
    }

    @Test
    void toRevisionSecretario_shouldInvokeService() {
        ApiResponse<Map<String, Object>> serviceResponse = new ApiResponse<>(200, "ok", Map.of());
        when(necesidadEstadoService.cambiarEstadoMasivo(
                1,
                EstadoNecesidad.BORRADOR,
                EstadoNecesidad.EN_REVISION_SECRETARIO,
                2,
                null,
                null)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.toRevisionSecretario(1, 2, null);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(necesidadEstadoService).cambiarEstadoMasivo(
                1,
                EstadoNecesidad.BORRADOR,
                EstadoNecesidad.EN_REVISION_SECRETARIO,
                2,
                null,
                null);
    }

    @Test
    void toBorradorDesdeSecretario_shouldInvokeService() {
        ApiResponse<Map<String, Object>> serviceResponse = new ApiResponse<>(200, "ok", Map.of());
        when(necesidadEstadoService.cambiarEstadoMasivo(
                3,
                EstadoNecesidad.EN_REVISION_SECRETARIO,
                EstadoNecesidad.BORRADOR,
                4,
                null,
                null)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.toBorradorDesdeSecretario(3, 4, null);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(necesidadEstadoService).cambiarEstadoMasivo(
                3,
                EstadoNecesidad.EN_REVISION_SECRETARIO,
                EstadoNecesidad.BORRADOR,
                4,
                null,
                null);
    }

    @Test
    void toRevisionJefe_shouldAllowProgramaYDepartamento() {
        ApiResponse<Map<String, Object>> serviceResponse = new ApiResponse<>(200, "ok", Map.of());
        when(necesidadEstadoService.cambiarEstadoMasivo(
                5,
                EstadoNecesidad.EN_REVISION_SECRETARIO,
                EstadoNecesidad.EN_REVISION_JEFE,
                4,
                6,
                null)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.toRevisionJefe(5, 4, 6, null);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(necesidadEstadoService).cambiarEstadoMasivo(
                5,
                EstadoNecesidad.EN_REVISION_SECRETARIO,
                EstadoNecesidad.EN_REVISION_JEFE,
                4,
                6,
                null);
    }

    @Test
    void toRevisionSecretarioDesdeJefe_shouldSupportOptionalDepartamento() {
        ApiResponse<Map<String, Object>> serviceResponse = new ApiResponse<>(200, "ok", Map.of());
        when(necesidadEstadoService.cambiarEstadoMasivo(
                7,
                EstadoNecesidad.EN_REVISION_JEFE,
                EstadoNecesidad.EN_REVISION_SECRETARIO,
                4,
                null,
                null)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.toRevisionSecretarioDesdeJefe(7, 4, null, null);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(necesidadEstadoService).cambiarEstadoMasivo(
                7,
                EstadoNecesidad.EN_REVISION_JEFE,
                EstadoNecesidad.EN_REVISION_SECRETARIO,
                4,
                null,
                null);
    }

    @Test
    void toNoAsignada_shouldInvokeService() {
        ApiResponse<Map<String, Object>> serviceResponse = new ApiResponse<>(200, "ok", Map.of());
        when(necesidadEstadoService.cambiarEstadoMasivo(
                8,
                EstadoNecesidad.EN_REVISION_JEFE,
                EstadoNecesidad.NO_ASIGNADA,
                null,
                9,
                null)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.toNoAsignada(8, 9, null);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(necesidadEstadoService).cambiarEstadoMasivo(
                8,
                EstadoNecesidad.EN_REVISION_JEFE,
                EstadoNecesidad.NO_ASIGNADA,
                null,
                9,
                null);
    }

    @Test
    void cambiarEstadoPorOid_shouldDelegateToService() {
        ApiResponse<Map<String, Object>> serviceResponse = new ApiResponse<>(200, "ok", Map.of());
        when(necesidadEstadoService.cambiarEstadoPorOids(List.of(1, 2),
                EstadoNecesidad.BORRADOR,
                EstadoNecesidad.EN_REVISION_SECRETARIO)).thenReturn(serviceResponse);

        NecesidadEstadoPorOidRequest request = new NecesidadEstadoPorOidRequest();
        request.setOidNecesidades(List.of(1, 2));
        request.setEstadoOrigen(EstadoNecesidad.BORRADOR);
        request.setEstadoDestino(EstadoNecesidad.EN_REVISION_SECRETARIO);

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.cambiarEstadoPorOid(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(necesidadEstadoService).cambiarEstadoPorOids(
                List.of(1, 2),
                EstadoNecesidad.BORRADOR,
                EstadoNecesidad.EN_REVISION_SECRETARIO);
    }
}
