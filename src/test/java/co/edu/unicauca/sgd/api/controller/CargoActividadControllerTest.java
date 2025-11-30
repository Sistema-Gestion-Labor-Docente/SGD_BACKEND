package co.edu.unicauca.sgd.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import java.util.List;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.CargoActividadDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.CargoActividadDTOResponse;
import co.edu.unicauca.sgd.api.service.actividad.laborDocente.CargoActividadService;

@ExtendWith(MockitoExtension.class)
class CargoActividadControllerTest {

    @Mock
    private CargoActividadService service;

    private CargoActividadController controller;

    @BeforeEach
    void setUp() {
        controller = new CargoActividadController(service);
    }

    @Test
    void findAll_delegaEnServicioYRetornaRespuesta() {
        Pageable pageable = Pageable.unpaged();
        Page<CargoActividadDTOResponse> page = new PageImpl<>(List.of());
        ApiResponse<Page<CargoActividadDTOResponse>> serviceResponse =
                new ApiResponse<>(200, "ok", page);

        when(service.obtenerTodos(eq("nombre"), eq("tipo"), isNull(), same(pageable)))
                .thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<CargoActividadDTOResponse>>> response =
                controller.findAll("nombre", "tipo", null, pageable);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).obtenerTodos(eq("nombre"), eq("tipo"), isNull(), same(pageable));
    }

    @Test
    void findByOid_delegaEnServicio() {
        ApiResponse<CargoActividadDTOResponse> serviceResponse =
                new ApiResponse<>(200, "ok", new CargoActividadDTOResponse());
        when(service.buscarPorId(5)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<CargoActividadDTOResponse>> response = controller.findByOid(5);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).buscarPorId(5);
    }

    @Test
    void save_invocaServicioConDto() {
        CargoActividadDTORequest dto = new CargoActividadDTORequest();
        ApiResponse<CargoActividadDTOResponse> serviceResponse =
                new ApiResponse<>(201, "creado", new CargoActividadDTOResponse());
        when(service.guardar(dto)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<CargoActividadDTOResponse>> response = controller.save(dto);

        assertEquals(201, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).guardar(dto);
    }

    @Test
    void update_invocaServicioYReenviaRespuesta() {
        CargoActividadDTORequest dto = new CargoActividadDTORequest();
        ApiResponse<CargoActividadDTOResponse> serviceResponse =
                new ApiResponse<>(200, "actualizado", new CargoActividadDTOResponse());
        when(service.actualizar(9, dto)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<CargoActividadDTOResponse>> response = controller.update(9, dto);

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

