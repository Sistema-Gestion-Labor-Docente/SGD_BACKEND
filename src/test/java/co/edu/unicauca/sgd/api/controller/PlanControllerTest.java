package co.edu.unicauca.sgd.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.PlanDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.PlanDTOResponse;
import co.edu.unicauca.sgd.api.service.materias.PlanService;

@ExtendWith(MockitoExtension.class)
class PlanControllerTest {

    @Mock
    private PlanService service;

    private PlanController controller;

    @BeforeEach
    void setUp() {
        controller = new PlanController(service);
    }

    @Test
    void findAll_delegaEnServicioYRetornaRespuesta() {
        Pageable pageable = Pageable.unpaged();
        Page<PlanDTOResponse> page = new PageImpl<>(List.of());
        ApiResponse<Page<PlanDTOResponse>> serviceResponse =
                new ApiResponse<>(200, "ok", page);

        when(service.obtenerTodos(eq("1"), eq("ACTIVO"), eq(2), eq(pageable)))
                .thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<PlanDTOResponse>>> response =
                controller.findAll("1", "ACTIVO", 2, pageable);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).obtenerTodos(eq("1"), eq("ACTIVO"), eq(2), eq(pageable));
    }

    @Test
    void findByOid_delegaEnServicio() {
        ApiResponse<PlanDTOResponse> serviceResponse =
                new ApiResponse<>(200, "ok", new PlanDTOResponse());
        when(service.buscarPorId(5)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<PlanDTOResponse>> response = controller.findByOid(5);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).buscarPorId(5);
    }

    @Test
    void save_invocaServicioConDto() {
        PlanDTORequest dto = new PlanDTORequest();
        ApiResponse<PlanDTOResponse> serviceResponse =
                new ApiResponse<>(201, "creado", new PlanDTOResponse());
        when(service.guardar(dto)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<PlanDTOResponse>> response = controller.save(dto);

        assertEquals(201, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).guardar(dto);
    }

    @Test
    void update_invocaServicioYReenviaRespuesta() {
        PlanDTORequest dto = new PlanDTORequest();
        ApiResponse<PlanDTOResponse> serviceResponse =
                new ApiResponse<>(200, "actualizado", new PlanDTOResponse());
        when(service.actualizar(9, dto)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<PlanDTOResponse>> response = controller.update(9, dto);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).actualizar(9, dto);
    }

    @Test
    void delete_exito_retornarMapaConInfo() {
        ApiResponse<Void> serviceResponse = new ApiResponse<>(200, "eliminado", null);
        when(service.eliminar(11)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = controller.delete(11);

        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> data = response.getBody().getData();
        assertEquals(11, data.get("oid"));
        assertEquals("eliminado", data.get("mensaje"));
        verify(service).eliminar(11);
    }

    @Test
    void delete_error_retornarRespuestaSinDatos() {
        ApiResponse<Void> serviceResponse = new ApiResponse<>(400, "error", null);
        when(service.eliminar(22)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = controller.delete(22);

        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody().getData());
        verify(service).eliminar(22);
    }
}

