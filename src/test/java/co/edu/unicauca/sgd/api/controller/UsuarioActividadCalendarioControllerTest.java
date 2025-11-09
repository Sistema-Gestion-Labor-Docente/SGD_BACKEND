package co.edu.unicauca.sgd.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
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
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.DocenciaDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.ValidacionHorasCargoDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTOResponse;
import co.edu.unicauca.sgd.api.service.actividad.laborDocente.UsuarioActividadCalendarioService;

@ExtendWith(MockitoExtension.class)
class UsuarioActividadCalendarioControllerTest {

    @Mock
    private UsuarioActividadCalendarioService service;

    private UsuarioActividadCalendarioController controller;

    @BeforeEach
    void setUp() {
        controller = new UsuarioActividadCalendarioController(service);
    }

    @Test
    void findAll_delegaEnServicioYRetornaRespuesta() {
        Pageable pageable = Pageable.unpaged();
        Page<UsuarioActividadCalendarioDTOResponse> page = new PageImpl<>(List.of());
        ApiResponse<Page<UsuarioActividadCalendarioDTOResponse>> serviceResponse =
                new ApiResponse<>(200, "ok", page);

        when(service.listarActividadesConRelaciones(eq(1), eq(2), isNull(), isNull(), isNull(), same(pageable)))
                .thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<UsuarioActividadCalendarioDTOResponse>>> response = controller
                .findAll(1, 2, null, null, null, pageable);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(page, response.getBody().getData());
        verify(service).listarActividadesConRelaciones(eq(1), eq(2), isNull(), isNull(), isNull(), same(pageable));
    }

    @Test
    void findByOid_delegaEnServicio() {
        ApiResponse<UsuarioActividadCalendarioDTOResponse> serviceResponse =
                new ApiResponse<>(200, "ok", new UsuarioActividadCalendarioDTOResponse());
        when(service.obtenerActividadConRelaciones(5)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<UsuarioActividadCalendarioDTOResponse>> response = controller.findByOid(5);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).obtenerActividadConRelaciones(5);
    }

    @Test
    void save_invocaServicioConDto() {
        UsuarioActividadCalendarioDTORequest dto = new UsuarioActividadCalendarioDTORequest();
        ApiResponse<UsuarioActividadCalendarioDTOResponse> serviceResponse =
                new ApiResponse<>(201, "creado", new UsuarioActividadCalendarioDTOResponse());
        when(service.crearActividadConRelaciones(dto)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<UsuarioActividadCalendarioDTOResponse>> response = controller.save(dto);

        assertEquals(201, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).crearActividadConRelaciones(dto);
    }

    @Test
    void update_invocaServicioYReenviaRespuesta() {
        UsuarioActividadCalendarioDTORequest dto = new UsuarioActividadCalendarioDTORequest();
        ApiResponse<UsuarioActividadCalendarioDTOResponse> serviceResponse =
                new ApiResponse<>(200, "actualizado", new UsuarioActividadCalendarioDTOResponse());
        when(service.actualizarActividadConRelaciones(9, dto)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<UsuarioActividadCalendarioDTOResponse>> response = controller.update(9, dto);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).actualizarActividadConRelaciones(9, dto);
    }

    @Test
    void delete_cuandoServicioExitoso_retornarMapaConInfo() {
        ApiResponse<Void> serviceResponse = new ApiResponse<>(200, "eliminado", null);
        when(service.eliminarActividad(11)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = controller.delete(11);

        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> data = response.getBody().getData();
        assertEquals(11, data.get("oidActividad"));
        assertEquals("eliminado", data.get("mensaje"));
        verify(service).eliminarActividad(11);
    }

    @Test
    void delete_cuandoServicioFalla_retornarRespuestaSinDatos() {
        ApiResponse<Void> serviceResponse = new ApiResponse<>(400, "error", null);
        when(service.eliminarActividad(22)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = controller.delete(22);

        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody().getData());
        verify(service).eliminarActividad(22);
    }

    @Test
    void deleteRelacion_exito_retornaMapa() {
        ApiResponse<Void> serviceResponse = new ApiResponse<>(200, "ok", null);
        when(service.eliminarRelacion(1, 2, 3)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = controller.deleteRelacion(1, 2, 3);

        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> data = response.getBody().getData();
        assertEquals(1, data.get("oidActividad"));
        assertEquals(2, data.get("oidUsuario"));
        assertEquals(3, data.get("oidCalendario"));
        assertEquals("ok", data.get("mensaje"));
        verify(service).eliminarRelacion(1, 2, 3);
    }

    @Test
    void deleteRelacion_error_retornaNullData() {
        ApiResponse<Void> serviceResponse = new ApiResponse<>(404, "no existe", null);
        when(service.eliminarRelacion(5, 6, 7)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = controller.deleteRelacion(5, 6, 7);

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody().getData());
        verify(service).eliminarRelacion(5, 6, 7);
    }

    @Test
    void validarCupoUsuarios_devuelveRespuestaDelServicio() {
        ValidacionHorasCargoDTOResponse data = ValidacionHorasCargoDTOResponse.builder()
                .puedeAsignar(true)
                .horasMaximasCargo(12f)
                .horasDisponiblesUsuarioMenorCupo(6f)
                .oidUsuarioMenorCupo(10)
                .semanasMaximas(8f)
                .build();
        ApiResponse<ValidacionHorasCargoDTOResponse> serviceResponse =
                new ApiResponse<>(200, "ok", data);

        when(service.validarCupoUsuariosEnCargo(eq(1), eq(2), eq(3), eq(List.of(5, 7)))).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<ValidacionHorasCargoDTOResponse>> response =
                controller.validarCupoUsuarios(1, 2, 3, List.of(5, 7));

        assertEquals(200, response.getStatusCodeValue());
        assertSame(data, response.getBody().getData());

        verify(service).validarCupoUsuariosEnCargo(eq(1), eq(2), eq(3), eq(List.of(5, 7)));
    }

    @Test
    void listarDocencia_retornaRespuestaDelServicio() {
        Pageable pageable = Pageable.unpaged();
        Page<DocenciaDTOResponse> page = new PageImpl<>(List.of());
        ApiResponse<Page<DocenciaDTOResponse>> serviceResponse = new ApiResponse<>(200, "ok", page);
        when(service.listarPorTipoDocencia(pageable)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<DocenciaDTOResponse>>> response = controller.listarDocencia(pageable);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).listarPorTipoDocencia(pageable);
    }
}
