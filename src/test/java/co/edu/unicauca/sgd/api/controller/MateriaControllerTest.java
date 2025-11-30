package co.edu.unicauca.sgd.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
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
import co.edu.unicauca.sgd.api.dto.materias.MateriaDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.MateriaDTOResponse;
import co.edu.unicauca.sgd.api.exception.materias.MateriaValidationException;
import co.edu.unicauca.sgd.api.service.materias.MateriaService;

@ExtendWith(MockitoExtension.class)
class MateriaControllerTest {

    @Mock
    private MateriaService service;

    private MateriaController controller;

    @BeforeEach
    void setUp() {
        controller = new MateriaController(service);
    }

    @Test
    void findAll_delegaEnServicioYRetornaRespuesta() {
        Pageable pageable = Pageable.unpaged();
        Page<MateriaDTOResponse> page = new PageImpl<>(List.of());
        ApiResponse<Page<MateriaDTOResponse>> serviceResponse =
                new ApiResponse<>(200, "ok", page);

        when(service.obtenerTodos(eq("1"), eq("COD"), eq("nombre"), eq(1), eq(2), eq(3), same(pageable)))
                .thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<MateriaDTOResponse>>> response =
                controller.findAll("1", "COD", "nombre", 1, 2, 3, pageable);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).obtenerTodos(eq("1"), eq("COD"), eq("nombre"), eq(1), eq(2), eq(3), same(pageable));
    }

    @Test
    void findByOid_delegaEnServicio() {
        ApiResponse<MateriaDTOResponse> serviceResponse =
                new ApiResponse<>(200, "ok", new MateriaDTOResponse());
        when(service.buscarPorId(5)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<MateriaDTOResponse>> response = controller.findByOid(5);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).buscarPorId(5);
    }

    @Test
    void save_invocaServicioConDto() {
        MateriaDTORequest dto = new MateriaDTORequest();
        ApiResponse<MateriaDTOResponse> serviceResponse =
                new ApiResponse<>(201, "creado", new MateriaDTOResponse());
        when(service.guardar(dto)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<MateriaDTOResponse>> response = controller.save(dto);

        assertEquals(201, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).guardar(dto);
    }

    @Test
    void update_invocaServicioYReenviaRespuesta() {
        MateriaDTORequest dto = new MateriaDTORequest();
        ApiResponse<MateriaDTOResponse> serviceResponse =
                new ApiResponse<>(200, "actualizado", new MateriaDTOResponse());
        when(service.actualizar(9, dto)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<MateriaDTOResponse>> response = controller.update(9, dto);

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

    @Test
    void findFreeSubjects_delegaEnServicio() {
        Pageable pageable = Pageable.unpaged();
        Page<MateriaDTOResponse> page = new PageImpl<>(List.of());
        ApiResponse<Page<MateriaDTOResponse>> serviceResponse =
                new ApiResponse<>(200, "ok", page);

        when(service.obtenerMateriasSinCorrequisitoNiReferencias(eq(1), eq(2), same(pageable)))
                .thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<MateriaDTOResponse>>> response =
                controller.findFreeSubjects(1, 2, pageable);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).obtenerMateriasSinCorrequisitoNiReferencias(eq(1), eq(2), same(pageable));
    }

    @Test
    void buscarDisponiblesParaPlan_sinFiltrosLanzaExcepcionValidacion() {
        Pageable pageable = Pageable.unpaged();

        assertThrows(MateriaValidationException.class,
                () -> controller.buscarDisponiblesParaPlan(null, null, null, 1, pageable));
    }

    @Test
    void buscarDisponiblesParaPlan_conFiltrosDelegaEnServicio() {
        Pageable pageable = Pageable.unpaged();
        Page<MateriaDTOResponse> page = new PageImpl<>(List.of());
        ApiResponse<Page<MateriaDTOResponse>> serviceResponse =
                new ApiResponse<>(200, "ok", page);

        when(service.buscarPorIdentificadoresExcluyendoPlan(eq("1"), isNull(), isNull(), eq(2), same(pageable)))
                .thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<Page<MateriaDTOResponse>>> response =
                controller.buscarDisponiblesParaPlan("1", null, null, 2, pageable);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(serviceResponse, response.getBody());
        verify(service).buscarPorIdentificadoresExcluyendoPlan(eq("1"), isNull(), isNull(), eq(2), same(pageable));
    }
}

