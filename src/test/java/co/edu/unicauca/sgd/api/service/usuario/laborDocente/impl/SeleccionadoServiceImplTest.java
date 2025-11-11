package co.edu.unicauca.sgd.api.service.usuario.laborDocente.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.SeleccionadoDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.SeleccionadoDTORequest;
import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.Seleccionado;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioDepartamento;
import co.edu.unicauca.sgd.api.domain.UsuarioDetalle;
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

    @Test
    void guardar_conDedicacionExplicitaDebePersistirla() {
        SeleccionadoDTORequest request = SeleccionadoDTORequest.builder()
                .oidCalendario(1)
                .oidUsuario(2)
                .dedicacion("DEDICACION-MANUAL")
                .build();

        Usuario usuario = new Usuario();
        usuario.setOidUsuario(2);

        when(calendarioRepository.existsById(1)).thenReturn(true);
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuario));
        when(seleccionadoRepository.existsByCalendarioOidcalendarioAndUsuarioOidUsuario(1, 2)).thenReturn(false);
        when(seleccionadoMapper.convertToEntity(request)).thenReturn(new Seleccionado());
        when(seleccionadoRepository.save(any(Seleccionado.class))).thenAnswer(invocation -> {
            Seleccionado saved = invocation.getArgument(0);
            saved.setOidSeleccionado(15);
            return saved;
        });
        SeleccionadoDTOResponse dtoResponse = SeleccionadoDTOResponse.builder().oidSeleccionado(15).build();
        when(seleccionadoMapper.toResponse(any(Seleccionado.class))).thenReturn(dtoResponse);

        ApiResponse<SeleccionadoDTOResponse> response = service.guardar(request);

        assertThat(response.getCodigo()).isEqualTo(201);
        assertThat(response.getData()).isEqualTo(dtoResponse);

        ArgumentCaptor<Seleccionado> captor = ArgumentCaptor.forClass(Seleccionado.class);
        verify(seleccionadoRepository).save(captor.capture());
        assertThat(captor.getValue().getUsuario()).isSameAs(usuario);
        assertThat(captor.getValue().getDedicacion()).isEqualTo("DEDICACION-MANUAL");
        verify(usuarioDepartamentoRepository, never()).save(any(UsuarioDepartamento.class));
    }

    @Test
    void guardar_sinDedicacionDebeUsarDetalleYCrearRelacionDepartamento() {
        SeleccionadoDTORequest request = SeleccionadoDTORequest.builder()
                .oidCalendario(3)
                .oidUsuario(4)
                .oidDepartamento(8)
                .build();

        UsuarioDetalle detalle = new UsuarioDetalle();
        detalle.setDedicacion("DEDICACION-DETALLE");
        Usuario usuario = new Usuario();
        usuario.setOidUsuario(4);
        usuario.setUsuarioDetalle(detalle);

        when(calendarioRepository.existsById(3)).thenReturn(true);
        when(usuarioRepository.findById(4)).thenReturn(Optional.of(usuario));
        when(seleccionadoRepository.existsByCalendarioOidcalendarioAndUsuarioOidUsuario(3, 4)).thenReturn(false);
        when(usuarioDepartamentoRepository.existsByUsuarioOidUsuarioAndDepartamentoOidDepartamento(4, 8)).thenReturn(false);
        Departamento departamento = new Departamento();
        departamento.setOidDepartamento(8);
        when(departamentoRepository.findById(8)).thenReturn(Optional.of(departamento));
        when(seleccionadoMapper.convertToEntity(request)).thenReturn(new Seleccionado());
        when(seleccionadoRepository.save(any(Seleccionado.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(seleccionadoMapper.toResponse(any(Seleccionado.class)))
                .thenReturn(SeleccionadoDTOResponse.builder().build());

        ApiResponse<SeleccionadoDTOResponse> response = service.guardar(request);

        assertThat(response.getCodigo()).isEqualTo(201);

        ArgumentCaptor<Seleccionado> captor = ArgumentCaptor.forClass(Seleccionado.class);
        verify(seleccionadoRepository).save(captor.capture());
        assertThat(captor.getValue().getDedicacion()).isEqualTo("DEDICACION-DETALLE");
        verify(usuarioDepartamentoRepository).save(any(UsuarioDepartamento.class));
    }

    @Test
    void buscarPorId_cuandoNoExisteDebeRetornar404() {
        when(seleccionadoRepository.findById(20)).thenReturn(Optional.empty());

        ApiResponse<SeleccionadoDTOResponse> response = service.buscarPorId(20);

        assertThat(response.getCodigo()).isEqualTo(404);
        assertThat(response.getData()).isNull();
        verify(seleccionadoMapper, never()).toResponse(any());
    }

    @Test
    void guardar_conCalendarioInexistenteRetorna404() {
        SeleccionadoDTORequest request = SeleccionadoDTORequest.builder()
                .oidCalendario(9)
                .oidUsuario(3)
                .build();

        when(calendarioRepository.existsById(9)).thenReturn(false);

        ApiResponse<SeleccionadoDTOResponse> response = service.guardar(request);

        assertThat(response.getCodigo()).isEqualTo(404);
        verify(usuarioRepository, never()).findById(any());
        verify(seleccionadoRepository, never()).save(any());
    }

    @Test
    void actualizar_cambiandoCalendarioRetorna400() {
        Seleccionado existente = new Seleccionado();
        existente.setOidSeleccionado(7);
        existente.setCalendario(new co.edu.unicauca.sgd.api.domain.Calendario());
        existente.getCalendario().setOidcalendario(5);
        existente.setUsuario(new Usuario(4));

        SeleccionadoDTORequest request = SeleccionadoDTORequest.builder()
                .oidCalendario(8)
                .build();

        when(seleccionadoRepository.findById(7)).thenReturn(Optional.of(existente));

        ApiResponse<SeleccionadoDTOResponse> response = service.actualizar(7, request);

        assertThat(response.getCodigo()).isEqualTo(400);
        verify(seleccionadoRepository, never()).save(any());
    }

    @Test
    void eliminar_cuandoNoExisteRetorna404() {
        when(seleccionadoRepository.existsById(30)).thenReturn(false);

        ApiResponse<Void> response = service.eliminar(30);

        assertThat(response.getCodigo()).isEqualTo(404);
    }
}
