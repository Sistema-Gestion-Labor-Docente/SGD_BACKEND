package co.edu.unicauca.sgd.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sgd.api.service.actividad.ActividadService;

@ExtendWith(MockitoExtension.class)
class ActividadControllerTest {

    @Mock
    private ActividadService actividadService;

    private ActividadController controller;

    @BeforeEach
    void setUp() {
        controller = new ActividadController(actividadService);
    }

    @Test
    void findAll_delegaEnServicioYRetornaRespuesta() {
        int page = 0;
        int size = 10;
        boolean ascending = true;
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ActividadBaseDTO> pageResult = new PageImpl<>(List.of());
        ApiResponse<Page<ActividadBaseDTO>> serviceResponse =
                new ApiResponse<>(200, "ok", pageResult);

        when(actividadService.obtenerTodos(eq(pageRequest), eq(ascending))).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<ActividadBaseDTO>>> response =
                controller.findAll(page, size, ascending);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(actividadService).obtenerTodos(eq(pageRequest), eq(ascending));
    }

    @Test
    void findById_delegaEnServicio() {
        ApiResponse<ActividadBaseDTO> serviceResponse =
                new ApiResponse<>(200, "ok", new ActividadBaseDTO());

        when(actividadService.buscarDTOPorId(5)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<ActividadBaseDTO>> response = controller.findById(5);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(actividadService).buscarDTOPorId(5);
    }

    @Test
    void save_invocaServicioConListaDto() {
        List<ActividadBaseDTO> dtos = List.of(new ActividadBaseDTO());
        List<Actividad> actividades = List.of(new Actividad());
        ApiResponse<List<Actividad>> serviceResponse =
                new ApiResponse<>(201, "creado", actividades);

        when(actividadService.guardar(dtos)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<List<Actividad>>> response = controller.save(dtos);

        assertEquals(201, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(actividadService).guardar(dtos);
    }

    @Test
    void update_invocaServicioYRetornaRespuesta() {
        ActividadBaseDTO dto = new ActividadBaseDTO();
        ApiResponse<Actividad> serviceResponse =
                new ApiResponse<>(200, "actualizado", new Actividad());

        when(actividadService.actualizar(10, dto)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Actividad>> response = controller.update(10, dto);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(actividadService).actualizar(10, dto);
    }

    @Test
    void delete_invocaServicioYReenviaCodigo() {
        ApiResponse<Void> serviceResponse = new ApiResponse<>(200, "eliminado", null);
        when(actividadService.eliminar(7)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Void>> response = controller.delete(7);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(actividadService).eliminar(7);
    }
}

