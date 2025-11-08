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
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTOResponse;
import co.edu.unicauca.sgd.api.service.calendario.CalendarioService;

class CalendarioControllerTest {

    @Mock
    private CalendarioService calendarioService;

    private CalendarioController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new CalendarioController(calendarioService);
    }

    @Test
    void findAll_ShouldReturnServiceResponseStatusAndBody() {
        Page<CalendarioDTOResponse> page = new PageImpl<>(java.util.List.of(new CalendarioDTOResponse()));
        ApiResponse<Page<CalendarioDTOResponse>> serviceResponse = new ApiResponse<>(200, "ok", page);
        when(calendarioService.obtenerTodos(null, null, null, Pageable.unpaged())).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<CalendarioDTOResponse>>> result =
                controller.findAll(null, null, null, Pageable.unpaged());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(calendarioService).obtenerTodos(null, null, null, Pageable.unpaged());
    }

    @Test
    void findAll_ShouldReturnOkStatusWhenServiceReturns204() {
        ApiResponse<Page<CalendarioDTOResponse>> serviceResponse =
                new ApiResponse<>(204, "sin contenido", Page.empty());
        when(calendarioService.obtenerTodos(null, null, null, Pageable.unpaged())).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<CalendarioDTOResponse>>> result =
                controller.findAll(null, null, null, Pageable.unpaged());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
    }

    @Test
    void findByOid_ShouldReturnServiceResponse() {
        CalendarioDTOResponse dto = new CalendarioDTOResponse();
        ApiResponse<CalendarioDTOResponse> serviceResponse = new ApiResponse<>(200, "ok", dto);
        when(calendarioService.buscarPorId(10)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<CalendarioDTOResponse>> result = controller.findByOid(10);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(calendarioService).buscarPorId(10);
    }

    @Test
    void save_ShouldReturnCreatedStatusWhenServiceReturns201() {
        CalendarioDTORequest request = new CalendarioDTORequest();
        CalendarioDTOResponse dto = new CalendarioDTOResponse();
        ApiResponse<CalendarioDTOResponse> serviceResponse = new ApiResponse<>(201, "creado", dto);
        when(calendarioService.guardar(eq(request))).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<CalendarioDTOResponse>> result = controller.save(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(calendarioService).guardar(eq(request));
    }

    @Test
    void update_ShouldReturnServiceStatusAndBody() {
        CalendarioDTORequest request = new CalendarioDTORequest();
        CalendarioDTOResponse dto = new CalendarioDTOResponse();
        ApiResponse<CalendarioDTOResponse> serviceResponse = new ApiResponse<>(200, "actualizado", dto);
        when(calendarioService.actualizar(5, request)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<CalendarioDTOResponse>> result = controller.update(5, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(calendarioService).actualizar(5, request);
    }

    @Test
    void delete_ShouldWrapServiceMessageWhenSuccess() {
        ApiResponse<Void> serviceResponse = new ApiResponse<>(200, "eliminado", null);
        when(calendarioService.eliminar(8)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.delete(8);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCodigo()).isEqualTo(200);
        assertThat(result.getBody().getMensaje()).isEqualTo("eliminado");
        assertThat(result.getBody().getData()).isEqualTo(Map.of("oid", 8, "mensaje", "eliminado"));
        verify(calendarioService).eliminar(8);
    }

    @Test
    void delete_ShouldPropagateErrorStatusWhenServiceFails() {
        ApiResponse<Void> serviceResponse = new ApiResponse<>(404, "no encontrado", null);
        when(calendarioService.eliminar(99)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.delete(99);

        assertThat(result.getStatusCodeValue()).isEqualTo(404);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCodigo()).isEqualTo(404);
        assertThat(result.getBody().getData()).isNull();
        verify(calendarioService).eliminar(99);
    }
}

