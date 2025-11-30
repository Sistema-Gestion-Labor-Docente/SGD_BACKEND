package co.edu.unicauca.sgd.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

import co.edu.unicauca.sgd.api.domain.EstadoActividad;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.service.actividad.EstadoActividadService;

@ExtendWith(MockitoExtension.class)
class EstadoActividadControllerTest {

    @Mock
    private EstadoActividadService service;

    private EstadoActividadController controller;

    @BeforeEach
    void setUp() {
        controller = new EstadoActividadController(service);
    }

    @Test
    void create_delegaEnServicio() {
        EstadoActividad estado = new EstadoActividad();
        ApiResponse<EstadoActividad> body = new ApiResponse<>(201, "creado", estado);
        ResponseEntity<ApiResponse<EstadoActividad>> responseEntity = ResponseEntity.status(201).body(body);

        when(service.guardar(eq(estado))).thenReturn(responseEntity);

        ResponseEntity<ApiResponse<EstadoActividad>> response = controller.create(estado);

        assertEquals(201, response.getStatusCodeValue());
        assertSame(body, response.getBody());
        verify(service).guardar(estado);
    }

    @Test
    void findById_delegaEnServicio() {
        ApiResponse<EstadoActividad> body = new ApiResponse<>(200, "ok", new EstadoActividad());
        ResponseEntity<ApiResponse<EstadoActividad>> responseEntity = ResponseEntity.ok(body);

        when(service.buscarPorOid(5)).thenReturn(responseEntity);

        ResponseEntity<ApiResponse<EstadoActividad>> response = controller.findById(5);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(body, response.getBody());
        verify(service).buscarPorOid(5);
    }

    @Test
    void findAll_delegaEnServicio() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EstadoActividad> page = new PageImpl<>(List.of(), pageable, 0);
        ApiResponse<Page<EstadoActividad>> body = new ApiResponse<>(200, "ok", page);
        ResponseEntity<ApiResponse<Page<EstadoActividad>>> responseEntity = ResponseEntity.ok(body);

        when(service.obtenerTodos(any(Pageable.class))).thenReturn(responseEntity);

        ResponseEntity<ApiResponse<Page<EstadoActividad>>> response = controller.findAll(pageable);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(body, response.getBody());
        verify(service).obtenerTodos(pageable);
    }

    @Test
    void update_delegaEnServicio() {
        EstadoActividad estado = new EstadoActividad();
        ApiResponse<EstadoActividad> body = new ApiResponse<>(200, "ok", estado);
        ResponseEntity<ApiResponse<EstadoActividad>> responseEntity = ResponseEntity.ok(body);

        when(service.actualizar(eq(3), eq(estado))).thenReturn(responseEntity);

        ResponseEntity<ApiResponse<EstadoActividad>> response = controller.update(3, estado);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(body, response.getBody());
        verify(service).actualizar(3, estado);
    }

    @Test
    void delete_delegaEnServicio() {
        ApiResponse<Void> body = new ApiResponse<>(200, "ok", null);
        ResponseEntity<ApiResponse<Void>> responseEntity = ResponseEntity.ok(body);

        when(service.eliminar(4)).thenReturn(responseEntity);

        ResponseEntity<ApiResponse<Void>> response = controller.delete(4);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(body, response.getBody());
        verify(service).eliminar(4);
    }
}

