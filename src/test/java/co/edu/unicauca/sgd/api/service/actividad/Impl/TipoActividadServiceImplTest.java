package co.edu.unicauca.sgd.api.service.actividad.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.domain.TipoActividad;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.repository.TipoActividadRepository;

@ExtendWith(MockitoExtension.class)
class TipoActividadServiceImplTest {

    @Mock
    private TipoActividadRepository repository;

    @InjectMocks
    private TipoActividadServiceImpl service;

    @Test
    void listarTodos_devuelvePaginaEnApiResponse() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<TipoActividad> page = new PageImpl<>(java.util.List.of(), pageable, 0);
        when(repository.findAll(pageable)).thenReturn(page);

        ApiResponse<Page<TipoActividad>> response = service.listarTodos(pageable);

        assertEquals(200, response.getCodigo());
        assertThat(response.getData()).isSameAs(page);
        verify(repository).findAll(pageable);
    }

    @Test
    void buscarPorOid_encontrado_devuelveEntidad() {
        TipoActividad tipo = new TipoActividad();
        tipo.setOidTipoActividad(1);
        when(repository.findById(1)).thenReturn(Optional.of(tipo));

        TipoActividad result = service.buscarPorOid(1);

        assertThat(result).isSameAs(tipo);
    }

    @Test
    void buscarPorOid_noEncontrado_devuelveNull() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        TipoActividad result = service.buscarPorOid(1);

        assertNull(result);
    }

    @Test
    void guardar_convierteNombreYDescripcionAMayusculas() {
        TipoActividad tipo = new TipoActividad();
        tipo.setNombre("docencia");
        tipo.setDescripcion("desc");

        TipoActividad saved = new TipoActividad();
        saved.setNombre("DOCENCIA");
        saved.setDescripcion("DESC");

        when(repository.save(any(TipoActividad.class))).thenReturn(saved);

        ApiResponse<TipoActividad> response = service.guardar(tipo);

        assertEquals(200, response.getCodigo());
        assertEquals("DOCENCIA", response.getData().getNombre());
        assertEquals("DESC", response.getData().getDescripcion());
        verify(repository).save(any(TipoActividad.class));
    }

    @Test
    void guardar_conErrorDevuelve500() {
        TipoActividad tipo = new TipoActividad();
        tipo.setNombre("x");
        tipo.setDescripcion("y");
        when(repository.save(any(TipoActividad.class))).thenThrow(new RuntimeException("db error"));

        ApiResponse<TipoActividad> response = service.guardar(tipo);

        assertEquals(500, response.getCodigo());
        assertNull(response.getData());
    }

    @Test
    void actualizar_encontrado_actualizaYRetornaEntidad() {
        TipoActividad existente = new TipoActividad();
        existente.setOidTipoActividad(1);

        TipoActividad update = new TipoActividad();
        update.setNombre("nuevo");
        update.setDescripcion("desc");

        TipoActividad saved = new TipoActividad();
        saved.setOidTipoActividad(1);
        saved.setNombre("NUEVO");
        saved.setDescripcion("DESC");

        when(repository.findById(1)).thenReturn(Optional.of(existente));
        when(repository.save(any(TipoActividad.class))).thenReturn(saved);

        TipoActividad result = service.actualizar(1, update);

        assertEquals(1, result.getOidTipoActividad());
        assertEquals("NUEVO", result.getNombre());
        assertEquals("DESC", result.getDescripcion());
        verify(repository).save(any(TipoActividad.class));
    }

    @Test
    void actualizar_noEncontrado_lanzaRuntimeException() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.actualizar(1, new TipoActividad()));
    }

    @Test
    void eliminar_invocaDeleteById() {
        service.eliminar(5);
        verify(repository).deleteById(5);
    }
}
