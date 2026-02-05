package co.edu.unicauca.sgd.api.service.calendario.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unicauca.sgd.api.client.ClienteNotificacion;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.EstadoUsuario;
import co.edu.unicauca.sgd.api.domain.Fecha;
import co.edu.unicauca.sgd.api.domain.Rol;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioDetalle;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTOResponse;
import co.edu.unicauca.sgd.api.exception.calendario.CalendarioOperacionNoPermitidaException;
import co.edu.unicauca.sgd.api.mapper.CalendarioMapper;
import co.edu.unicauca.sgd.api.mapper.FechaMapper;
import co.edu.unicauca.sgd.api.repository.ActividadCalendarioRepository;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.DepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.FechaRepository;
import co.edu.unicauca.sgd.api.repository.NecesidadRepository;
import co.edu.unicauca.sgd.api.repository.SeleccionadoRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioDepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioRepository;
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
    private UsuarioRepository usuarioRepository;
    @Mock
    private ActividadCalendarioRepository actividadCalendarioRepository;
    @Mock
    private NecesidadRepository necesidadRepository;
    @Mock
    private CalendarioPdfService calendarioPdfService;
    @Mock
    private ClienteNotificacion clienteNotificacion;

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
                usuarioRepository,
                actividadCalendarioRepository,
                necesidadRepository,
                fechaMapper,
                calendarioPdfService,
                clienteNotificacion);

        lenient().when(fechaRepository.findByCalendario_OidcalendarioOrderByFechaInicialAsc(any()))
                .thenReturn(List.of());
        lenient().when(departamentoRepository.findAll()).thenReturn(List.of());
        lenient().when(fechaService.guardar(any())).thenReturn(new ApiResponse<>(200, "ok", null));
        lenient().when(actividadCalendarioRepository.countByCalendario_Oidcalendario(any())).thenReturn(0L);
        lenient().when(necesidadRepository.countByCalendario_Oidcalendario(any())).thenReturn(0L);
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

    @Test
    void eliminar_conActividadesRegistradas_retorna400() {
        when(calendarioRepository.existsById(15)).thenReturn(true);
        when(actividadCalendarioRepository.countByCalendario_Oidcalendario(15)).thenReturn(2L);

        ApiResponse<Void> response = calendarioService.eliminar(15);

        assertEquals(400, response.getCodigo());
        verify(calendarioRepository, never()).deleteById(15);
    }

    @Test
    void eliminar_conNecesidadesRegistradas_retorna400() {
        when(calendarioRepository.existsById(18)).thenReturn(true);
        when(actividadCalendarioRepository.countByCalendario_Oidcalendario(18)).thenReturn(0L);
        when(necesidadRepository.countByCalendario_Oidcalendario(18)).thenReturn(5L);

        ApiResponse<Void> response = calendarioService.eliminar(18);

        assertEquals(400, response.getCodigo());
        verify(calendarioRepository, never()).deleteById(18);
    }

    @Test
    void actualizar_pendienteAAprobado_enviaNotificacion() {
        Calendario existente = new Calendario();
        existente.setOidcalendario(1);
        existente.setAnioCalendario("2024");
        existente.setNumeroCalendario(1);
        existente.setEstado("PENDIENTE");
        existente.setUsuarioCreacion("docente@unicauca.edu.co");

        CalendarioDTORequest request = new CalendarioDTORequest();
        request.setAnioCalendario("2024");
        request.setNumeroCalendario(1);
        request.setEstado("APROBADO");

        when(calendarioRepository.findById(1)).thenReturn(Optional.of(existente));
        when(calendarioRepository.save(existente)).thenAnswer(invocation -> {
            Calendario saved = invocation.getArgument(0);
            saved.setEstado("APROBADO");
            return saved;
        });

        Usuario creador = new Usuario();
        creador.setCorreo("docente@unicauca.edu.co");
        creador.setEstadoUsuario(estadoUsuarioActivo());
        creador.setUsuarioDetalle(detalleFacultad("INGENIERIA"));
        when(usuarioRepository.findByCorreo("docente@unicauca.edu.co"))
                .thenReturn(Optional.of(creador));

        Usuario coordinador = new Usuario();
        coordinador.setCorreo("coord@unicauca.edu.co");
        coordinador.setEstadoUsuario(estadoUsuarioActivo());
        coordinador.setUsuarioDetalle(detalleFacultad("INGENIERIA"));
        coordinador.setRoles(List.of(rol("COORDINADOR")));

        when(usuarioRepository.findAll()).thenReturn(List.of(coordinador));

        // habilitar notificaciones
        ReflectionTestUtils.setField(calendarioService, "notificacionHabilitada", true);

        calendarioService.actualizar(1, request);

        verify(clienteNotificacion, atLeastOnce()).enviarNotificacion(
                any(), any(String.class), any(String.class));
    }

    @Test
    void cron_aprobadoDentroDeRango_cambiaAActivoYNotifica() {
        Calendario calendario = new Calendario();
        calendario.setOidcalendario(2);
        calendario.setAnioCalendario("2024");
        calendario.setNumeroCalendario(1);
        calendario.setEstado("APROBADO");
        calendario.setUsuarioCreacion("profesor@unicauca.edu.co");

        when(calendarioRepository.findAll()).thenReturn(List.of(calendario));
        when(calendarioRepository.save(calendario)).thenReturn(calendario);

        LocalDateTime ahora = LocalDateTime.now();

        Fecha fechaInicio = new Fecha();
        fechaInicio.setFechaInicial(ahora.minusDays(1));

        when(fechaRepository.findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(2, 1))
                .thenReturn(Optional.of(fechaInicio));

        Fecha fechaFin = new Fecha();
        fechaFin.setFechaInicial(ahora.plusDays(1));

        when(fechaRepository.findByCalendario_Oidcalendario(2))
                .thenReturn(List.of(fechaInicio, fechaFin));

        Usuario creador = new Usuario();
        creador.setCorreo("profesor@unicauca.edu.co");
        creador.setEstadoUsuario(estadoUsuarioActivo());
        creador.setUsuarioDetalle(detalleFacultad("INGENIERIA"));
        when(usuarioRepository.findByCorreo("profesor@unicauca.edu.co"))
                .thenReturn(Optional.of(creador));

        Usuario jefe = new Usuario();
        jefe.setCorreo("jefe@unicauca.edu.co");
        jefe.setEstadoUsuario(estadoUsuarioActivo());
        jefe.setUsuarioDetalle(detalleFacultad("INGENIERIA"));
        jefe.setRoles(List.of(rol("JEFE DE DEPARTAMENTO")));

        when(usuarioRepository.findAll()).thenReturn(List.of(jefe));

        ReflectionTestUtils.setField(calendarioService, "notificacionHabilitada", true);

        calendarioService.actualizarEstadosCalendariosPorFechas();

        verify(calendarioRepository, atLeastOnce()).save(calendario);
        assertEquals("ACTIVO", calendario.getEstado());
        verify(clienteNotificacion, atLeastOnce()).enviarNotificacion(
                any(), any(String.class), any(String.class));
    }

    private EstadoUsuario estadoUsuarioActivo() {
        EstadoUsuario estado = new EstadoUsuario();
        estado.setNombre("ACTIVO");
        return estado;
    }

    private UsuarioDetalle detalleFacultad(String facultad) {
        UsuarioDetalle detalle = new UsuarioDetalle();
        detalle.setFacultad(facultad);
        return detalle;
    }

    private Rol rol(String nombre) {
        Rol rol = new Rol();
        rol.setNombre(nombre);
        return rol;
    }

    @Test
    void cron_activoFueraDeRango_cambiaADeshabilitadoSinNotificar() {
        Calendario calendario = new Calendario();
        calendario.setOidcalendario(3);
        calendario.setAnioCalendario("2024");
        calendario.setNumeroCalendario(1);
        calendario.setEstado("ACTIVO");

        when(calendarioRepository.findAll()).thenReturn(List.of(calendario));

        LocalDateTime ahora = LocalDateTime.now();

        Fecha fechaInicio = new Fecha();
        fechaInicio.setFechaInicial(ahora.minusDays(10));

        when(fechaRepository.findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(3, 1))
                .thenReturn(Optional.of(fechaInicio));

        Fecha fechaFin = new Fecha();
        fechaFin.setFechaInicial(ahora.minusDays(1));

        when(fechaRepository.findByCalendario_Oidcalendario(3))
                .thenReturn(List.of(fechaInicio, fechaFin));

        ReflectionTestUtils.setField(calendarioService, "notificacionHabilitada", true);

        calendarioService.actualizarEstadosCalendariosPorFechas();

        verify(calendarioRepository, atLeastOnce()).save(calendario);
        assertEquals("DESHABILITADO", calendario.getEstado());

        // no debe notificar en transicion ACTIVO -> DESHABILITADO
        verifyNoInteractions(clienteNotificacion);
    }
}
