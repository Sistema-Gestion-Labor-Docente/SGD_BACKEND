package co.edu.unicauca.sgd.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

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
import co.edu.unicauca.sgd.api.dto.necesidades.AsignacionDTORequest;
import co.edu.unicauca.sgd.api.dto.necesidades.AsignacionDTOResponse;
import co.edu.unicauca.sgd.api.service.necesidad.AsignacionService;

class AsignacionControllerTest {

    @Mock
    private AsignacionService asignacionService;

    private AsignacionController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new AsignacionController(asignacionService);
    }

    @Test
    void listar_DeberiaDelegarEnServicio() {
        Page<AsignacionDTOResponse> page = new PageImpl<>(Collections.singletonList(AsignacionDTOResponse.builder().build()));
        ApiResponse<Page<AsignacionDTOResponse>> response = new ApiResponse<>(200, "ok", page);
        when(asignacionService.listar(10, 20, 1, 2, "Calculo", 4, "MAT101", Pageable.unpaged())).thenReturn(response);

        ResponseEntity<ApiResponse<Page<AsignacionDTOResponse>>> result =
                controller.listar(10, 20, 1, 2, "Calculo", 4, "MAT101", Pageable.unpaged());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
        verify(asignacionService).listar(10, 20, 1, 2, "Calculo", 4, "MAT101", Pageable.unpaged());
    }

    @Test
    void listar_DeberiaRetornarOkSiServicioRetorna204() {
        ApiResponse<Page<AsignacionDTOResponse>> response = new ApiResponse<>(204, "sin datos", Page.empty());
        when(asignacionService.listar(10, 20, null, null, null, null, null, Pageable.unpaged())).thenReturn(response);

        ResponseEntity<ApiResponse<Page<AsignacionDTOResponse>>> result =
                controller.listar(10, 20, null, null, null, null, null, Pageable.unpaged());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void buscarPorId_DeberiaRetornarRespuestaDelServicio() {
        AsignacionDTOResponse dto = AsignacionDTOResponse.builder().build();
        ApiResponse<AsignacionDTOResponse> response = new ApiResponse<>(200, "ok", dto);
        when(asignacionService.buscarPorId(5)).thenReturn(response);

        ResponseEntity<ApiResponse<AsignacionDTOResponse>> result = controller.buscarPorId(5);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
        verify(asignacionService).buscarPorId(5);
    }

    @Test
    void crear_DeberiaRetornarStatusSegunServicio() {
        AsignacionDTORequest request = new AsignacionDTORequest();
        AsignacionDTOResponse dto = AsignacionDTOResponse.builder().build();
        ApiResponse<AsignacionDTOResponse> response = new ApiResponse<>(201, "creada", dto);
        when(asignacionService.crear(request)).thenReturn(response);

        ResponseEntity<ApiResponse<AsignacionDTOResponse>> result = controller.crear(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
        verify(asignacionService).crear(request);
    }

    @Test
    void actualizar_DeberiaRetornarRespuestaDelServicio() {
        AsignacionDTORequest request = new AsignacionDTORequest();
        AsignacionDTOResponse dto = AsignacionDTOResponse.builder().build();
        ApiResponse<AsignacionDTOResponse> response = new ApiResponse<>(200, "ok", dto);
        when(asignacionService.actualizar(3, request)).thenReturn(response);

        ResponseEntity<ApiResponse<AsignacionDTOResponse>> result = controller.actualizar(3, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
        verify(asignacionService).actualizar(3, request);
    }

    @Test
    void eliminar_DeberiaRetornarStatusDelServicio() {
        ApiResponse<Void> response = new ApiResponse<>(204, "eliminada", null);
        when(asignacionService.eliminar(8)).thenReturn(response);

        ResponseEntity<ApiResponse<Void>> result = controller.eliminar(8);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
        verify(asignacionService).eliminar(8);
    }
}
