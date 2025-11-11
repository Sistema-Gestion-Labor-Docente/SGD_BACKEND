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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadBulkCreateRequest;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTORequest;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTOResponse;
import co.edu.unicauca.sgd.api.service.necesidad.NecesidadService;

class NecesidadControllerTest {

    @Mock
    private NecesidadService necesidadService;

    private NecesidadController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new NecesidadController(necesidadService);
    }

    @Test
    void findAll_shouldReturnServiceBody() {
        Page<NecesidadDTOResponse> page = new PageImpl<>(List.of(new NecesidadDTOResponse()));
        ApiResponse<Page<NecesidadDTOResponse>> serviceResponse = new ApiResponse<>(200, "ok", page);
        Pageable pageable = Pageable.unpaged();

        when(necesidadService.obtenerTodos(1, null, null, 2, null, pageable)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<NecesidadDTOResponse>>> result =
                controller.findAll(1, null, null, 2, null, pageable);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(necesidadService).obtenerTodos(1, null, null, 2, null, pageable);
    }

    @Test
    void findByOid_shouldPropagateResponseStatus() {
        ApiResponse<NecesidadDTOResponse> serviceResponse =
                new ApiResponse<>(200, "ok", new NecesidadDTOResponse());
        when(necesidadService.buscarPorId(10)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<NecesidadDTOResponse>> result = controller.findByOid(10);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(necesidadService).buscarPorId(10);
    }

    @Test
    void save_shouldReturnCreatedWhenServiceReturns201() {
        NecesidadDTORequest request = new NecesidadDTORequest();
        ApiResponse<NecesidadDTOResponse> serviceResponse =
                new ApiResponse<>(201, "creada", new NecesidadDTOResponse());
        when(necesidadService.guardar(request)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<NecesidadDTOResponse>> result = controller.save(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(necesidadService).guardar(request);
    }

    @Test
    void saveBulk_shouldReturnServiceBody() {
        NecesidadBulkCreateRequest request = new NecesidadBulkCreateRequest();
        ApiResponse<List<NecesidadDTOResponse>> serviceResponse =
                new ApiResponse<>(201, "creadas", List.of(new NecesidadDTOResponse()));
        when(necesidadService.guardarMasivo(request)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<List<NecesidadDTOResponse>>> result = controller.saveBulk(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(necesidadService).guardarMasivo(request);
    }

    @Test
    void update_shouldReturnOkWhenServiceDoes() {
        NecesidadDTORequest request = new NecesidadDTORequest();
        ApiResponse<NecesidadDTOResponse> serviceResponse =
                new ApiResponse<>(200, "actualizada", new NecesidadDTOResponse());
        when(necesidadService.actualizar(5, request)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<NecesidadDTOResponse>> result = controller.update(5, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(necesidadService).actualizar(5, request);
    }

    @Test
    void delete_shouldWrapSuccessPayload() {
        ApiResponse<Void> serviceResponse = new ApiResponse<>(204, "eliminada", null);
        when(necesidadService.eliminar(9)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.delete(9);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCodigo()).isEqualTo(204);
        assertThat(result.getBody().getData()).isEqualTo(Map.of("oid", 9, "mensaje", "eliminada"));
        verify(necesidadService).eliminar(9);
    }

    @Test
    void delete_shouldPropagateErrorStatus() {
        ApiResponse<Void> serviceResponse = new ApiResponse<>(404, "no encontrada", null);
        when(necesidadService.eliminar(99)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.delete(99);

        assertThat(result.getStatusCodeValue()).isEqualTo(404);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isNull();
        verify(necesidadService).eliminar(99);
    }

}
