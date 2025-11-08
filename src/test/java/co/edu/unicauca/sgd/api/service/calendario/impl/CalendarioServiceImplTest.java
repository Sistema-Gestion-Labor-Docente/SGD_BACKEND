package co.edu.unicauca.sgd.api.service.calendario.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTOResponse;
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
    private CalendarioMapper calendarioMapper;
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
    private FechaMapper fechaMapper;
    @Mock
    private CalendarioPdfService calendarioPdfService;

    private CalendarioServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CalendarioServiceImpl(
                calendarioRepository,
                calendarioMapper,
                fechaService,
                fechaRepository,
                seleccionadoRepository,
                departamentoRepository,
                usuarioDepartamentoRepository,
                fechaMapper,
                calendarioPdfService);
    }

    @Test
    void obtenerTodos_sinResultadosDevuelveMensajeAdecuado() {
        Page<Calendario> empty = Page.empty(PageRequest.of(0, 5));
        when(calendarioRepository.findAll(any(Specification.class), eq(empty.getPageable())))
                .thenReturn(empty);

        ApiResponse<Page<CalendarioDTOResponse>> response =
                service.obtenerTodos(null, null, null, empty.getPageable());

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(response.getMensaje()).isEqualTo("No se encontraron calendarios.");
        assertThat(response.getData().getContent()).isEmpty();
    }
}
