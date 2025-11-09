package co.edu.unicauca.sgd.api.service.calendario.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTOResponse;
import co.edu.unicauca.sgd.api.exception.calendario.CalendarioOperacionNoPermitidaException;
import co.edu.unicauca.sgd.api.mapper.CalendarioMapper;
import co.edu.unicauca.sgd.api.mapper.FechaMapper;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.DepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.FechaRepository;
import co.edu.unicauca.sgd.api.repository.SeleccionadoRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioDepartamentoRepository;
import co.edu.unicauca.sgd.api.service.calendario.CalendarioPdfService;
import co.edu.unicauca.sgd.api.service.calendario.FechaService;

@ExtendWith(MockitoExtension.class)
class CalendarioServiceImplTest {

    @Mock
    private CalendarioRepository calendarioRepository;
    @Mock
    private FechaService fechaService;
    @Mock
    private FechaRepository fechaRepository;
    @Mock
    private SeleccionadoRepository seleccionadoRepository;
    @Mock
    private DepartamentoRepository departamentoRepository;
    @Mock
    private UsuarioDepartamentoRepository usuarioDepartamentoRepository;
    @Mock
    private CalendarioPdfService calendarioPdfService;

    private final CalendarioMapper calendarioMapper = new CalendarioMapper();
    private final FechaMapper fechaMapper = new FechaMapper();

    private CalendarioServiceImpl calendarioService;

    @BeforeEach
    void setUp() {
        calendarioService = new CalendarioServiceImpl(
                calendarioRepository,
                calendarioMapper,
                fechaService,
                fechaRepository,
                seleccionadoRepository,
                departamentoRepository,
                usuarioDepartamentoRepository,
                fechaMapper,
                calendarioPdfService);

        lenient().when(fechaRepository.findByCalendario_OidcalendarioOrderByFechaInicialAsc(any()))
                .thenReturn(List.of());
        lenient().when(departamentoRepository.findAll()).thenReturn(List.of());
        lenient().when(fechaService.guardar(any())).thenReturn(new ApiResponse<>(200, "ok", null));
    }

    @Test
    void obtenerTodos_conResultados_retornaMensajeExitoso() {
        Pageable pageable = PageRequest.of(0, 5);
        Calendario calendario = new Calendario();
        calendario.setOidcalendario(1);
        calendario.setAnioCalendario("2024");
        Page<Calendario> page = new PageImpl<>(List.of(calendario), pageable, 1);

        when(calendarioRepository.findAll(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Calendario>>any(), eq(pageable))).thenReturn(page);

        ApiResponse<Page<CalendarioDTOResponse>> response =
                calendarioService.obtenerTodos("2024", 1, "ACTIVO", pageable);

        assertEquals(200, response.getCodigo());
        assertEquals("Calendarios encontrados correctamente.", response.getMensaje());
        assertEquals(1, response.getData().getTotalElements());
    }

    @Test
    void obtenerTodos_sinResultados_retornaMensajeVacio() {
        Pageable pageable = PageRequest.of(0, 5);
        when(calendarioRepository.findAll(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Calendario>>any(), eq(pageable))).thenReturn(Page.empty(pageable));

        ApiResponse<Page<CalendarioDTOResponse>> response =
                calendarioService.obtenerTodos(null, null, null, pageable);

        assertEquals(200, response.getCodigo());
        assertEquals("No se encontraron calendarios.", response.getMensaje());
        assertEquals(0, response.getData().getTotalElements());
    }

    @Test
    void buscarPorId_existente_retornarDto() {
        Calendario calendario = new Calendario();
        calendario.setOidcalendario(5);
        when(calendarioRepository.findById(5)).thenReturn(Optional.of(calendario));

        ApiResponse<CalendarioDTOResponse> response = calendarioService.buscarPorId(5);

        assertEquals(200, response.getCodigo());
        assertEquals("Calendario encontrado correctamente.", response.getMensaje());
        assertNotNull(response.getData());
    }

    @Test
    void buscarPorId_noExiste_retorna404() {
        when(calendarioRepository.findById(7)).thenReturn(Optional.empty());

        ApiResponse<CalendarioDTOResponse> response = calendarioService.buscarPorId(7);

        assertEquals(404, response.getCodigo());
        assertNull(response.getData());
    }

    @Test
    void guardar_creaCalendarioYCargaRelaciones() {
        CalendarioDTORequest request = new CalendarioDTORequest();
        request.setAnioCalendario("2024");
        request.setNumeroCalendario(1);

        when(calendarioRepository.save(any(Calendario.class))).thenAnswer(invocation -> {
            Calendario saved = invocation.getArgument(0);
            saved.setOidcalendario(10);
            return saved;
        });

        ApiResponse<CalendarioDTOResponse> response = calendarioService.guardar(request);

        assertEquals(201, response.getCodigo());
        assertEquals("Calendario guardado correctamente.", response.getMensaje());
        verify(calendarioRepository).save(any(Calendario.class));
        verify(fechaService, atLeastOnce()).guardar(any());
    }

    @Test
    void guardar_operacionNoPermitida_retorna400() {
        CalendarioDTORequest request = new CalendarioDTORequest();
        request.setAnioCalendario("2024");
        request.setNumeroCalendario(1);

        when(calendarioRepository.save(any(Calendario.class)))
                .thenThrow(new CalendarioOperacionNoPermitidaException("error"));

        ApiResponse<CalendarioDTOResponse> response = calendarioService.guardar(request);

        assertEquals(400, response.getCodigo());
        assertNull(response.getData());
    }

    @Test
    void actualizar_datosValidos_retorna200() {
        Calendario existente = new Calendario();
        existente.setOidcalendario(3);
        existente.setAnioCalendario("2024");
        existente.setNumeroCalendario(1);

        CalendarioDTORequest request = new CalendarioDTORequest();
        request.setAnioCalendario("2024");
        request.setNumeroCalendario(1);

        when(calendarioRepository.findById(3)).thenReturn(Optional.of(existente));
        when(calendarioRepository.save(existente)).thenReturn(existente);

        ApiResponse<CalendarioDTOResponse> response = calendarioService.actualizar(3, request);

        assertEquals(200, response.getCodigo());
        assertEquals("Calendario actualizado correctamente.", response.getMensaje());
    }

    @Test
    void actualizar_cambiaAnio_lanzaError() {
        Calendario existente = new Calendario();
        existente.setOidcalendario(3);
        existente.setAnioCalendario("2024");
        existente.setNumeroCalendario(1);

        CalendarioDTORequest request = new CalendarioDTORequest();
        request.setAnioCalendario("2025");
        request.setNumeroCalendario(1);

        when(calendarioRepository.findById(3)).thenReturn(Optional.of(existente));

        ApiResponse<CalendarioDTOResponse> response = calendarioService.actualizar(3, request);

        assertEquals(400, response.getCodigo());
        verify(calendarioRepository).findById(3);
        verifyNoInteractions(calendarioPdfService);
    }

    @Test
    void actualizar_noExiste_retorna404() {
        CalendarioDTORequest request = new CalendarioDTORequest();
        request.setAnioCalendario("2024");
        request.setNumeroCalendario(1);
        when(calendarioRepository.findById(9)).thenReturn(Optional.empty());

        ApiResponse<CalendarioDTOResponse> response = calendarioService.actualizar(9, request);

        assertEquals(404, response.getCodigo());
    }

    @Test
    void eliminar_existente_retorna204() {
        when(calendarioRepository.existsById(8)).thenReturn(true);

        ApiResponse<Void> response = calendarioService.eliminar(8);

        assertEquals(204, response.getCodigo());
        verify(calendarioRepository).deleteById(8);
    }

    @Test
    void eliminar_noExiste_retorna404() {
        when(calendarioRepository.existsById(12)).thenReturn(false);

        ApiResponse<Void> response = calendarioService.eliminar(12);

        assertEquals(404, response.getCodigo());
    }
}
