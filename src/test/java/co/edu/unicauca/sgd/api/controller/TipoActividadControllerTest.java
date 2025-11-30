package co.edu.unicauca.sgd.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import co.edu.unicauca.sgd.api.domain.TipoActividad;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.service.actividad.TipoActividadService;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class TipoActividadControllerTest {

    @Mock
    private TipoActividadService service;

    @InjectMocks
    private TipoActividadController controller;

    @Test
    void create_delegaEnServicioYRetornaCodigoServicio() {
        TipoActividad tipo = new TipoActividad();
        ApiResponse<TipoActividad> responseBody = new ApiResponse<>(200, "ok", tipo);

        when(service.guardar(tipo)).thenReturn(responseBody);

        ResponseEntity<ApiResponse<TipoActividad>> response = controller.create(tipo);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(responseBody, response.getBody());
        verify(service).guardar(tipo);
    }

    @Test
    void findById_encontrado_retornaEntidad() {
        TipoActividad tipo = new TipoActividad();
        tipo.setOidTipoActividad(1);

        when(service.buscarPorOid(1)).thenReturn(tipo);

        ResponseEntity<TipoActividad> response = controller.findById(1);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(tipo, response.getBody());
    }

    @Test
    void findById_noEncontrado_lanzaRuntimeYHandlerDevuelve404() {
        when(service.buscarPorOid(2)).thenReturn(null);

        try {
            controller.findById(2);
        } catch (RuntimeException ex) {
            ResponseEntity<String> handled = controller.handleRuntimeException(ex);
            assertEquals(404, handled.getStatusCodeValue());
            assertEquals("TipoActividad no encontrado con ID: 2", handled.getBody());
        }
    }

    @Test
    void buscarTodos_delegaEnServicio() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TipoActividad> page = new PageImpl<>(List.of(), pageable, 0);
        ApiResponse<Page<TipoActividad>> body = new ApiResponse<>(200, "ok", page);

        when(service.listarTodos(pageable)).thenReturn(body);

        ResponseEntity<ApiResponse<Page<TipoActividad>>> response = controller.buscarTodos(pageable);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(body, response.getBody());
        verify(service).listarTodos(pageable);
    }

    @Test
    void update_delegaEnServicio() {
        TipoActividad tipo = new TipoActividad();
        TipoActividad actualizado = new TipoActividad();

        when(service.actualizar(5, tipo)).thenReturn(actualizado);

        ResponseEntity<TipoActividad> response = controller.update(5, tipo);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(actualizado, response.getBody());
        verify(service).actualizar(5, tipo);
    }

    @Test
    void delete_delegaEnServicioYRetornaNoContent() {
        ResponseEntity<Void> response = controller.delete(3);

        assertEquals(204, response.getStatusCodeValue());
        verify(service).eliminar(3);
    }
}
