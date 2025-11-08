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
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTOResponse;
import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;
import co.edu.unicauca.sgd.api.service.calendario.FechaService;

class FechaControllerTest {

    @Mock
    private FechaService fechaService;

    private FechaController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new FechaController(fechaService);
    }

    @Test
    void findAll_ShouldReturnServiceResponse() {
        Page<FechaDTOResponse> page = new PageImpl<>(java.util.List.of(new FechaDTOResponse()));
        ApiResponse<Page<FechaDTOResponse>> serviceResponse = new ApiResponse<>(200, "ok", page);
        when(fechaService.obtenerTodas(TipoFechaEnum.RESALTADAS, Pageable.unpaged())).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<FechaDTOResponse>>> result =
                controller.findAll(TipoFechaEnum.RESALTADAS, Pageable.unpaged());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(fechaService).obtenerTodas(TipoFechaEnum.RESALTADAS, Pageable.unpaged());
    }

    @Test
    void findAll_ShouldReturnOkStatusWhenServiceReturns204() {
        ApiResponse<Page<FechaDTOResponse>> serviceResponse =
                new ApiResponse<>(204, "sin datos", Page.empty());
        when(fechaService.obtenerTodas(null, Pageable.unpaged())).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<FechaDTOResponse>>> result = controller.findAll(null, Pageable.unpaged());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
    }

    @Test
    void findByOid_ShouldReturnServiceResponse() {
        FechaDTOResponse dto = new FechaDTOResponse();
        ApiResponse<FechaDTOResponse> serviceResponse = new ApiResponse<>(200, "ok", dto);
        when(fechaService.buscarPorId(4)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<FechaDTOResponse>> result = controller.findByOid(4);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(fechaService).buscarPorId(4);
    }

    @Test
    void findByOid_ShouldReturnOkWhenServiceReturns204() {
        ApiResponse<FechaDTOResponse> serviceResponse = new ApiResponse<>(204, "sin contenido", null);
        when(fechaService.buscarPorId(9)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<FechaDTOResponse>> result = controller.findByOid(9);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(fechaService).buscarPorId(9);
    }

    @Test
    void save_ShouldReturnCreatedStatusWhenServiceReturns201() {
        FechaDTORequest request = new FechaDTORequest();
        FechaDTOResponse dto = new FechaDTOResponse();
        ApiResponse<FechaDTOResponse> serviceResponse = new ApiResponse<>(201, "creada", dto);
        when(fechaService.guardar(eq(request))).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<FechaDTOResponse>> result = controller.save(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(fechaService).guardar(eq(request));
    }

    @Test
    void update_ShouldReturnServiceResponse() {
        FechaDTORequest request = new FechaDTORequest();
        FechaDTOResponse dto = new FechaDTOResponse();
        ApiResponse<FechaDTOResponse> serviceResponse = new ApiResponse<>(200, "actualizada", dto);
        when(fechaService.actualizar(7, request)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<FechaDTOResponse>> result = controller.update(7, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(fechaService).actualizar(7, request);
    }

    @Test
    void update_ShouldReturnOkWhenServiceReturns204() {
        FechaDTORequest request = new FechaDTORequest();
        ApiResponse<FechaDTOResponse> serviceResponse = new ApiResponse<>(204, "sin cambios", null);
        when(fechaService.actualizar(11, request)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<FechaDTOResponse>> result = controller.update(11, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(fechaService).actualizar(11, request);
    }

    @Test
    void delete_ShouldReturnWrappedResponseWhenSuccess() {
        ApiResponse<Void> serviceResponse = new ApiResponse<>(200, "eliminada", null);
        when(fechaService.eliminar(2)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.delete(2);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCodigo()).isEqualTo(200);
        assertThat(result.getBody().getMensaje()).isEqualTo("eliminada");
        assertThat(result.getBody().getData()).isEqualTo(Map.of("oid", 2, "mensaje", "eliminada"));
        verify(fechaService).eliminar(2);
    }

    @Test
    void delete_ShouldReturnErrorWhenServiceFails() {
        ApiResponse<Void> serviceResponse = new ApiResponse<>(400, "error", null);
        when(fechaService.eliminar(3)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.delete(3);

        assertThat(result.getStatusCodeValue()).isEqualTo(400);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isNull();
        verify(fechaService).eliminar(3);
    }
}
