package co.edu.unicauca.sgd.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.List;

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
import co.edu.unicauca.sgd.api.dto.materias.DepartamentoDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.DepartamentoDTOResponse;
import co.edu.unicauca.sgd.api.service.materias.DepartamentoService;

@ExtendWith(MockitoExtension.class)
class DepartamentoControllerTest {

    @Mock
    private DepartamentoService service;

    private DepartamentoController controller;

    @BeforeEach
    void setUp() {
        controller = new DepartamentoController(service);
    }

    @Test
    void findAll_delegaEnServicioYRetornaRespuesta() {
        Pageable pageable = Pageable.unpaged();
        Page<DepartamentoDTOResponse> page = new PageImpl<>(List.of());
        ApiResponse<Page<DepartamentoDTOResponse>> serviceResponse =
                new ApiResponse<>(200, "ok", page);

        when(service.obtenerTodos(eq("nombre"), eq(pageable))).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<DepartamentoDTOResponse>>> response =
                controller.findAll("nombre", pageable);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).obtenerTodos(eq("nombre"), eq(pageable));
    }

    @Test
    void findByOid_delegaEnServicio() {
        ApiResponse<DepartamentoDTOResponse> serviceResponse =
                new ApiResponse<>(200, "ok", new DepartamentoDTOResponse());
        when(service.buscarPorId(5)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<DepartamentoDTOResponse>> response = controller.findByOid(5);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).buscarPorId(5);
    }

    @Test
    void save_invocaServicioConDto() {
        DepartamentoDTORequest dto = new DepartamentoDTORequest();
        ApiResponse<DepartamentoDTOResponse> serviceResponse =
                new ApiResponse<>(201, "creado", new DepartamentoDTOResponse());
        when(service.guardar(dto)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<DepartamentoDTOResponse>> response = controller.save(dto);

        assertEquals(201, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).guardar(dto);
    }

    @Test
    void update_invocaServicioYReenviaRespuesta() {
        DepartamentoDTORequest dto = new DepartamentoDTORequest();
        ApiResponse<DepartamentoDTOResponse> serviceResponse =
                new ApiResponse<>(200, "actualizado", new DepartamentoDTOResponse());
        when(service.actualizar(9, dto)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<DepartamentoDTOResponse>> response = controller.update(9, dto);

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

