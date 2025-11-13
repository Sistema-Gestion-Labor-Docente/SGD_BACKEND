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
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.SeleccionadoDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.SeleccionadoDTOResponse;
import co.edu.unicauca.sgd.api.service.usuario.laborDocente.SeleccionadoService;

class SeleccionadoControllerTest {

    @Mock
    private SeleccionadoService seleccionadoService;

    private SeleccionadoController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new SeleccionadoController(seleccionadoService);
    }

    @Test
    void findAll_ShouldReturnServiceResponseStatusAndBody() {
        Page<SeleccionadoDTOResponse> page = new PageImpl<>(java.util.List.of(new SeleccionadoDTOResponse()));
        ApiResponse<Page<SeleccionadoDTOResponse>> serviceResponse = new ApiResponse<>(200, "ok", page);
        when(seleccionadoService.obtenerTodos(1, 2, "12345678", "Juan Perez", "correo@demo.com",
                "PLANTA", "TIEMPO COMPLETO", Pageable.unpaged())).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<SeleccionadoDTOResponse>>> result =
                controller.findAll(1, 2, "12345678", "Juan Perez", "correo@demo.com",
                        "PLANTA", "TIEMPO COMPLETO", Pageable.unpaged());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(seleccionadoService).obtenerTodos(1, 2, "12345678", "Juan Perez", "correo@demo.com",
                "PLANTA", "TIEMPO COMPLETO", Pageable.unpaged());
    }

    @Test
    void findAll_ShouldForceOkStatusWhenServiceReturns204() {
        ApiResponse<Page<SeleccionadoDTOResponse>> serviceResponse =
                new ApiResponse<>(204, "sin datos", Page.empty());
        when(seleccionadoService.obtenerTodos(null, null, null, null, null, null, null, Pageable.unpaged()))
                .thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<SeleccionadoDTOResponse>>> result =
                controller.findAll(null, null, null, null, null, null, null, Pageable.unpaged());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
    }

    @Test
    void findByOid_ShouldReturnServiceResponse() {
        SeleccionadoDTOResponse dto = new SeleccionadoDTOResponse();
        ApiResponse<SeleccionadoDTOResponse> serviceResponse = new ApiResponse<>(200, "ok", dto);
        when(seleccionadoService.buscarPorId(5)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<SeleccionadoDTOResponse>> result = controller.findByOid(5);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(seleccionadoService).buscarPorId(5);
    }

    @Test
    void save_ShouldReturnResponseStatusFromService() {
        SeleccionadoDTORequest request = new SeleccionadoDTORequest();
        ApiResponse<SeleccionadoDTOResponse> serviceResponse =
                new ApiResponse<>(201, "creado", new SeleccionadoDTOResponse());
        when(seleccionadoService.guardar(eq(request))).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<SeleccionadoDTOResponse>> result = controller.save(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(seleccionadoService).guardar(eq(request));
    }

    @Test
    void update_ShouldReturnServiceResponse() {
        SeleccionadoDTORequest request = new SeleccionadoDTORequest();
        ApiResponse<SeleccionadoDTOResponse> serviceResponse =
                new ApiResponse<>(200, "actualizado", new SeleccionadoDTOResponse());
        when(seleccionadoService.actualizar(9, request)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<SeleccionadoDTOResponse>> result = controller.update(9, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(serviceResponse);
        verify(seleccionadoService).actualizar(9, request);
    }

    @Test
    void delete_ShouldWrapSuccessPayloadWhenServiceReturns2xx() {
        ApiResponse<Void> serviceResponse = new ApiResponse<>(204, "eliminado", null);
        when(seleccionadoService.eliminar(7)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.delete(7);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCodigo()).isEqualTo(204);
        assertThat(result.getBody().getData()).isEqualTo(Map.of("oid", 7, "mensaje", "eliminado"));
        verify(seleccionadoService).eliminar(7);
    }

    @Test
    void delete_ShouldPropagateErrorWhenServiceFails() {
        ApiResponse<Void> serviceResponse = new ApiResponse<>(404, "no encontrado", null);
        when(seleccionadoService.eliminar(11)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.delete(11);

        assertThat(result.getStatusCodeValue()).isEqualTo(404);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isNull();
        verify(seleccionadoService).eliminar(11);
    }
}
