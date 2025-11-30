package co.edu.unicauca.sgd.api.service.actividad.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.List;

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

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.EstadoActividad;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.repository.EstadoActividadRepository;
import co.edu.unicauca.sgd.api.utils.StringUtils;

@ExtendWith(MockitoExtension.class)
class EstadoActividadServiceImplTest {

    @Mock
    private EstadoActividadRepository repository;

    @Mock
    private StringUtils stringUtils;

    private EstadoActividadServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EstadoActividadServiceImpl(repository, stringUtils);
    }

    @Test
    void obtenerTodos_okRetornaPagina() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<EstadoActividad> page = new PageImpl<>(List.of(), pageable, 0);
        when(repository.findAll(pageable)).thenReturn(page);

        ResponseEntity<ApiResponse<Page<EstadoActividad>>> response = service.obtenerTodos(pageable);

        assertEquals(200, response.getStatusCodeValue());
        assertThat(response.getBody().getData()).isSameAs(page);
        verify(repository).findAll(pageable);
    }

    @Test
    void buscarPorOid_encontrado_devuelve200() {
        EstadoActividad estado = new EstadoActividad();
        when(repository.findById(1)).thenReturn(Optional.of(estado));

        ResponseEntity<ApiResponse<EstadoActividad>> response = service.buscarPorOid(1);

        assertEquals(200, response.getStatusCodeValue());
        assertThat(response.getBody().getData()).isSameAs(estado);
    }

    @Test
    void buscarPorOid_noEncontrado_devuelve404() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<ApiResponse<EstadoActividad>> response = service.buscarPorOid(1);

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody().getData());
    }

    @Test
    void guardar_ok_convierteNombreYDevuelve201() {
        EstadoActividad estado = new EstadoActividad();
        estado.setNombre("activo");

        EstadoActividad saved = new EstadoActividad();
        saved.setNombre("ACTIVO");

        when(stringUtils.safeToUpperCase("activo")).thenReturn("ACTIVO");
        when(repository.save(any(EstadoActividad.class))).thenReturn(saved);

        ResponseEntity<ApiResponse<EstadoActividad>> response = service.guardar(estado);

        // HTTP status del servicio es 200; el código de negocio (201) va en el body.
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(201, response.getBody().getCodigo());
        assertEquals("ACTIVO", response.getBody().getData().getNombre());
        verify(repository).save(any(EstadoActividad.class));
    }

    @Test
    void actualizar_noExiste_devuelve404() {
        when(repository.existsById(1)).thenReturn(false);

        ResponseEntity<ApiResponse<EstadoActividad>> response = service.actualizar(1, new EstadoActividad());

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody().getData());
    }

    @Test
    void actualizar_ok_actualizaYDevuelve200() {
        EstadoActividad estado = new EstadoActividad();
        estado.setNombre("activo");

        EstadoActividad saved = new EstadoActividad();
        saved.setOidEstadoActividad(1);
        saved.setNombre("ACTIVO");

        when(repository.existsById(1)).thenReturn(true);
        when(stringUtils.safeToUpperCase("activo")).thenReturn("ACTIVO");
        when(repository.save(any(EstadoActividad.class))).thenReturn(saved);

        ResponseEntity<ApiResponse<EstadoActividad>> response = service.actualizar(1, estado);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("ACTIVO", response.getBody().getData().getNombre());
        verify(repository).save(any(EstadoActividad.class));
    }

    @Test
    void eliminar_noExiste_devuelve404() {
        when(repository.existsById(1)).thenReturn(false);

        ResponseEntity<ApiResponse<Void>> response = service.eliminar(1);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void eliminar_ok_eliminaYDevuelve200() {
        when(repository.existsById(1)).thenReturn(true);

        ResponseEntity<ApiResponse<Void>> response = service.eliminar(1);

        assertEquals(200, response.getStatusCodeValue());
        verify(repository).deleteById(1);
    }

    @Test
    void asignarEstadoActividad_ok_asignaYDevuelve200() {
        EstadoActividad estado = new EstadoActividad();
        when(repository.findById(2)).thenReturn(Optional.of(estado));
        Actividad actividad = new Actividad();

        ResponseEntity<ApiResponse<Void>> response = service.asignarEstadoActividad(actividad, 2);

        assertEquals(200, response.getStatusCodeValue());
        assertThat(actividad.getEstadoActividad()).isSameAs(estado);
    }
}
