package co.edu.unicauca.sgd.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.NombreFechaDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.NombreFechaDTOResponse;
import co.edu.unicauca.sgd.api.service.calendario.NombreFechaService;

class NombreFechaControllerTest {

    @Mock
    private NombreFechaService nombreFechaService;

    private NombreFechaController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new NombreFechaController(nombreFechaService);
    }

    @Test
    void listar_ShouldReturnServiceResponse() {
        Page<NombreFechaDTOResponse> page = new PageImpl<>(java.util.List.of(new NombreFechaDTOResponse()));
        ApiResponse<Page<NombreFechaDTOResponse>> serviceResponse = new ApiResponse<>(200, "ok", page);
        when(nombreFechaService.obtenerTodas("test", Pageable.unpaged())).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<NombreFechaDTOResponse>>> result =
                controller.listar("test", Pageable.unpaged());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(nombreFechaService).obtenerTodas("test", Pageable.unpaged());
    }

    @Test
    void listar_ShouldReturnOkWhenServiceReturns204() {
        ApiResponse<Page<NombreFechaDTOResponse>> serviceResponse =
                new ApiResponse<>(204, "sin contenido", Page.empty());
        when(nombreFechaService.obtenerTodas(null, Pageable.unpaged())).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<NombreFechaDTOResponse>>> result =
                controller.listar(null, Pageable.unpaged());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
    }

    @Test
    void buscarPorId_ShouldDelegateToService() {
        NombreFechaDTOResponse dto = new NombreFechaDTOResponse();
        ApiResponse<NombreFechaDTOResponse> serviceResponse = new ApiResponse<>(200, "ok", dto);
        when(nombreFechaService.buscarPorId(11)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<NombreFechaDTOResponse>> result = controller.buscarPorId(11);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(nombreFechaService).buscarPorId(11);
    }

    @Test
    void crear_ShouldReturnCreatedStatusWhenServiceReturns201() {
        NombreFechaDTORequest request = new NombreFechaDTORequest();
        NombreFechaDTOResponse dto = new NombreFechaDTOResponse();
        ApiResponse<NombreFechaDTOResponse> serviceResponse = new ApiResponse<>(201, "creado", dto);
        when(nombreFechaService.guardar(eq(request))).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<NombreFechaDTOResponse>> result = controller.crear(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(nombreFechaService).guardar(eq(request));
    }

    @Test
    void actualizar_ShouldReturnServiceResponse() {
        NombreFechaDTORequest request = new NombreFechaDTORequest();
        NombreFechaDTOResponse dto = new NombreFechaDTOResponse();
        ApiResponse<NombreFechaDTOResponse> serviceResponse = new ApiResponse<>(200, "actualizado", dto);
        when(nombreFechaService.actualizar(6, request)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<NombreFechaDTOResponse>> result = controller.actualizar(6, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(nombreFechaService).actualizar(6, request);
    }

    @Test
    void eliminar_ShouldWrapResponseWhenSuccess() {
        ApiResponse<Void> serviceResponse = new ApiResponse<>(200, "eliminado", null);
        when(nombreFechaService.eliminar(3)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.eliminar(3);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCodigo()).isEqualTo(200);
        assertThat(result.getBody().getMensaje()).isEqualTo("eliminado");
        assertThat(result.getBody().getData()).isEqualTo(Map.of("oid", 3, "mensaje", "eliminado"));
        verify(nombreFechaService).eliminar(3);
    }

    @Test
    void eliminar_ShouldReturnErrorWhenServiceFails() {
        ApiResponse<Void> serviceResponse = new ApiResponse<>(500, "error interno", null);
        when(nombreFechaService.eliminar(15)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.eliminar(15);

        assertThat(result.getStatusCodeValue()).isEqualTo(500);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCodigo()).isEqualTo(500);
        assertThat(result.getBody().getData()).isNull();
        verify(nombreFechaService).eliminar(15);
    }
}

