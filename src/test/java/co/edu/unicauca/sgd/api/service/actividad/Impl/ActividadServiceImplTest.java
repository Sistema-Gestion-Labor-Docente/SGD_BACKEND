package co.edu.unicauca.sgd.api.service.actividad.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.EavAtributo;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sgd.api.exception.ValidationException;
import co.edu.unicauca.sgd.api.mapper.ActividadMapper;
import co.edu.unicauca.sgd.api.repository.ActividadRepository;
import co.edu.unicauca.sgd.api.repository.EavAtributoRepository;
import co.edu.unicauca.sgd.api.repository.TipoActividadRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioRepository;
import co.edu.unicauca.sgd.api.service.EavAtributoService;
import co.edu.unicauca.sgd.api.service.actividad.ActividadDTOService;
import co.edu.unicauca.sgd.api.service.actividad.ActividadDetalleService;
import co.edu.unicauca.sgd.api.service.actividad.ActividadQueryService;
import co.edu.unicauca.sgd.api.service.actividad.EstadoActividadService;

@ExtendWith(MockitoExtension.class)
class ActividadServiceImplTest {

    @Mock
    private ActividadRepository actividadRepository;
    @Mock
    private ActividadDTOService actividadDTOService;
    @Mock
    private ActividadQueryService actividadQueryService;
    @Mock
    private ActividadMapper actividadMapper;
    @Mock
    private EstadoActividadService estadoActividadService;
    @Mock
    private EavAtributoService eavAtributoService;
    @Mock
    private EavAtributoRepository eavAtributoRepository;
    @Mock
    private ActividadDetalleService actividadDetalleService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TipoActividadRepository tipoActividadRepository;

    @InjectMocks
    private ActividadServiceImpl service;

    @Test
    void obtenerTodos_sinActividades_devuelve404() {
        Pageable pageable = PageRequest.of(0, 5);
        when(actividadRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

        ApiResponse<Page<ActividadBaseDTO>> response = service.obtenerTodos(pageable, true);

        assertEquals(404, response.getCodigo());
        assertThat(response.getData().getContent()).isEmpty();
    }

    @Test
    void obtenerTodos_conActividades_usaDTOServiceYOrdena() {
        Pageable pageable = PageRequest.of(0, 5);
        Actividad actividad = new Actividad();
        actividad.setOidActividad(1);
        Page<Actividad> page = new PageImpl<>(List.of(actividad), pageable, 1);
        ActividadBaseDTO dto = new ActividadBaseDTO();

        when(actividadRepository.findAll(pageable)).thenReturn(page);
        when(actividadDTOService.buildActividadBaseDTO(actividad)).thenReturn(dto);
        when(actividadQueryService.ordenarActividadesPorTipo(List.of(dto), true)).thenReturn(List.of(dto));

        ApiResponse<Page<ActividadBaseDTO>> response = service.obtenerTodos(pageable, true);

        assertEquals(200, response.getCodigo());
        assertEquals(1, response.getData().getTotalElements());
        verify(actividadDTOService).buildActividadBaseDTO(actividad);
        verify(actividadQueryService).ordenarActividadesPorTipo(List.of(dto), true);
    }

    @Test
    void buscarPorId_existente_devuelveEntidad() {
        Actividad actividad = new Actividad();
        actividad.setOidActividad(5);
        when(actividadRepository.findById(5)).thenReturn(Optional.of(actividad));

        Actividad result = service.buscarPorId(5);

        assertThat(result).isSameAs(actividad);
    }

    @Test
    void buscarPorId_noExistente_devuelveNull() {
        when(actividadRepository.findById(99)).thenReturn(Optional.empty());

        Actividad result = service.buscarPorId(99);

        assertNull(result);
    }

    @Test
    void buscarDTOPorId_existente_devuelve200() {
        Actividad actividad = new Actividad();
        actividad.setOidActividad(3);
        ActividadBaseDTO dto = new ActividadBaseDTO();

        when(actividadRepository.findById(3)).thenReturn(Optional.of(actividad));
        when(actividadDTOService.buildActividadBaseDTO(actividad)).thenReturn(dto);

        ApiResponse<ActividadBaseDTO> response = service.buscarDTOPorId(3);

        assertEquals(200, response.getCodigo());
        assertThat(response.getData()).isSameAs(dto);
    }

    @Test
    void buscarDTOPorId_noExistente_devuelve404() {
        when(actividadRepository.findById(10)).thenReturn(Optional.empty());

        ApiResponse<ActividadBaseDTO> response = service.buscarDTOPorId(10);

        assertEquals(404, response.getCodigo());
        assertNull(response.getData());
    }

    @Test
    void guardar_sinActividadesDevueltas_devuelve400() {
        ActividadBaseDTO dto = new ActividadBaseDTO();
        when(eavAtributoRepository.findAll()).thenReturn(Collections.<EavAtributo>emptyList());

        ApiResponse<List<Actividad>> response = service.guardar(List.of(dto));

        assertEquals(400, response.getCodigo());
        assertNull(response.getData());
    }

    @Test
    void actualizar_actividadNoExiste_devuelve404() {
        when(actividadRepository.findById(1)).thenReturn(Optional.empty());

        ApiResponse<Actividad> response = service.actualizar(1, new ActividadBaseDTO());

        assertEquals(404, response.getCodigo());
        assertNull(response.getData());
    }

    @Test
    void actualizar_conConflictoDeId_devuelve409() {
        Actividad existente = new Actividad();
        existente.setOidActividad(1);
        ActividadBaseDTO dto = new ActividadBaseDTO();
        dto.setOidActividad(2);

        when(actividadRepository.findById(1)).thenReturn(Optional.of(existente));
        when(actividadRepository.existsById(2)).thenReturn(true);

        ApiResponse<Actividad> response = service.actualizar(1, dto);

        assertEquals(409, response.getCodigo());
        assertNull(response.getData());
        verify(actividadRepository, never()).save(any(Actividad.class));
    }

    @Test
    void actualizar_ok_actualizaYRetorna200() {
        Actividad existente = new Actividad();
        existente.setOidActividad(1);
        ActividadBaseDTO dto = new ActividadBaseDTO();
        dto.setOidEstadoActividad(5);

        when(actividadRepository.findById(1)).thenReturn(Optional.of(existente));
        when(actividadDetalleService.generarNombreActividad(dto)).thenReturn("Nombre");
        when(eavAtributoRepository.findAll()).thenReturn(Collections.<EavAtributo>emptyList());
        when(actividadRepository.save(existente)).thenReturn(existente);

        ApiResponse<Actividad> response = service.actualizar(1, dto);

        assertEquals(200, response.getCodigo());
        assertThat(response.getData()).isSameAs(existente);
        verify(actividadMapper).actualizarCamposBasicos(existente, dto);
        verify(estadoActividadService).asignarEstadoActividad(existente, dto.getOidEstadoActividad());
        verify(eavAtributoService).actualizarAtributosDinamicos(eq(dto), eq(existente), any());
    }

    @Test
    void eliminar_noExiste_devuelve404() {
        when(actividadRepository.existsById(1)).thenReturn(false);

        ApiResponse<Void> response = service.eliminar(1);

        assertEquals(404, response.getCodigo());
        assertNull(response.getData());
        verify(actividadRepository, never()).deleteById(1);
    }

    @Test
    void eliminar_existe_eliminaYDevuelve200() {
        when(actividadRepository.existsById(1)).thenReturn(true);

        ApiResponse<Void> response = service.eliminar(1);

        assertEquals(200, response.getCodigo());
        assertNull(response.getData());
        verify(actividadRepository).deleteById(1);
    }
}
