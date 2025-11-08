package co.edu.unicauca.sgd.api.service.usuario.laborDocente.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.SeleccionadoDTOResponse;
import co.edu.unicauca.sgd.api.mapper.SeleccionadoMapper;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.DepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.SeleccionadoRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioDepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class SeleccionadoServiceImplTest {

    @Mock
    private SeleccionadoRepository seleccionadoRepository;
    @Mock
    private SeleccionadoMapper seleccionadoMapper;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private UsuarioDepartamentoRepository usuarioDepartamentoRepository;
    @Mock
    private DepartamentoRepository departamentoRepository;
    @Mock
    private CalendarioRepository calendarioRepository;

    private SeleccionadoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SeleccionadoServiceImpl(
                seleccionadoRepository,
                seleccionadoMapper,
                usuarioRepository,
                usuarioDepartamentoRepository,
                departamentoRepository,
                calendarioRepository);
    }

    @Test
    void obtenerTodos_sinFiltrosNiResultadosDevuelveMensajeAmigable() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(seleccionadoRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

        ApiResponse<Page<SeleccionadoDTOResponse>> response = service.obtenerTodos(null, null, pageable);

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(response.getMensaje()).isEqualTo("No se encontraron seleccionados.");
        assertThat(response.getData().getContent()).isEmpty();
    }
}
