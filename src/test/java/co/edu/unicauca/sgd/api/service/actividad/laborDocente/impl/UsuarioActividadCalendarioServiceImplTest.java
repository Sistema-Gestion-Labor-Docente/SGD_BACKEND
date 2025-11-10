package co.edu.unicauca.sgd.api.service.actividad.laborDocente.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.DocenciaDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.ValidacionHorasCargoDTOResponse;
import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.ActividadCalendario;
import co.edu.unicauca.sgd.api.domain.Asignacion;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.CargoActividad;
import co.edu.unicauca.sgd.api.domain.EstadoActividad;
import co.edu.unicauca.sgd.api.domain.Fecha;
import co.edu.unicauca.sgd.api.domain.TipoActividad;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioActividadCalendario;
import co.edu.unicauca.sgd.api.domain.UsuarioDetalle;
import co.edu.unicauca.sgd.api.exception.RecursoNoEncontradoException;
import co.edu.unicauca.sgd.api.exception.ValidacionNegocioException;
import co.edu.unicauca.sgd.api.mapper.AsignacionMapper;
import co.edu.unicauca.sgd.api.mapper.MateriaMapper;
import co.edu.unicauca.sgd.api.mapper.NecesidadMapper;
import co.edu.unicauca.sgd.api.mapper.UsuarioActividadCalendarioMapper;
import co.edu.unicauca.sgd.api.repository.AsignacionRepository;
import co.edu.unicauca.sgd.api.repository.ActividadCalendarioRepository;
import co.edu.unicauca.sgd.api.repository.ActividadRepository;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.CargoActividadRepository;
import co.edu.unicauca.sgd.api.repository.EavAtributoRepository;
import co.edu.unicauca.sgd.api.repository.EstadoActividadRepository;
import co.edu.unicauca.sgd.api.repository.FechaRepository;
import co.edu.unicauca.sgd.api.repository.TipoActividadRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioActividadCalendarioRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioRepository;
import co.edu.unicauca.sgd.api.service.EavAtributoService;

@ExtendWith(MockitoExtension.class)
class UsuarioActividadCalendarioServiceImplTest {

    @Mock
    private ActividadRepository actividadRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CalendarioRepository calendarioRepository;
    @Mock
    private ActividadCalendarioRepository actividadCalendarioRepository;
    @Mock
    private UsuarioActividadCalendarioRepository usuarioActividadCalendarioRepository;
    @Mock
    private UsuarioActividadCalendarioMapper mapper;
    @Mock
    private CargoActividadRepository cargoActividadRepository;
    @Mock
    private EstadoActividadRepository estadoActividadRepository;
    @Mock
    private EavAtributoService eavAtributoService;
    @Mock
    private EavAtributoRepository eavAtributoRepository;
    @Mock
    private TipoActividadRepository tipoActividadRepository;
    @Mock
    private FechaRepository fechaRepository;
    @Mock
    private AsignacionRepository asignacionRepository;

    private AsignacionMapper asignacionMapper;
    private NecesidadMapper necesidadMapper;
    private MateriaMapper materiaMapper;

    private UsuarioActividadCalendarioServiceImpl service;

    @BeforeEach
    void setUp() {
        asignacionMapper = new AsignacionMapper();
        necesidadMapper = new NecesidadMapper();
        materiaMapper = new MateriaMapper();

        service = new UsuarioActividadCalendarioServiceImpl(
                actividadRepository,
                usuarioRepository,
                calendarioRepository,
                actividadCalendarioRepository,
                usuarioActividadCalendarioRepository,
                mapper,
                cargoActividadRepository,
                estadoActividadRepository,
                eavAtributoService,
                eavAtributoRepository,
                tipoActividadRepository,
                fechaRepository,
                asignacionRepository,
                asignacionMapper,
                necesidadMapper,
                materiaMapper,
                new ObjectMapper());
    }

    @Test
    void listarActividadesConRelaciones_parametrosObligatorios_falla() {
        assertThrows(ValidacionNegocioException.class,
                () -> service.listarActividadesConRelaciones(null, null, null, null, null, Pageable.unpaged()));
    }

    @Test
    void listarActividadesConRelaciones_sinResultados_devuelveMensajeSinActividades() {
        Pageable pageable = Pageable.unpaged();
        Page<Integer> idsPage = new PageImpl<>(List.of(), pageable, 0);

        when(usuarioActividadCalendarioRepository.findDistinctActividadIdsByFilters(1, 2, null, null, null, pageable))
                .thenReturn(idsPage);

        ApiResponse<Page<UsuarioActividadCalendarioDTOResponse>> response =
                service.listarActividadesConRelaciones(1, 2, null, null, null, pageable);

        assertEquals(200, response.getCodigo());
        assertEquals("No se encontraron actividades.", response.getMensaje());
        assertEquals(0, response.getData().getTotalElements());
        verifyNoInteractions(actividadRepository);
    }

    @Test
    void eliminarActividad_actividadNoExiste_lanzaExcepcion() {
        when(actividadRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminarActividad(99));
    }

    @Test
    void obtenerActividadConRelaciones_actividadNoExiste_lanzaExcepcion() {
        when(actividadRepository.findById(50)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerActividadConRelaciones(50));
    }

    @Test
    void listarPorTipoDocencia_parametrosObligatorios_falla() {
        assertThrows(ValidacionNegocioException.class,
                () -> service.listarPorTipoDocencia(null, 2, null, null, null, Pageable.unpaged()));
        assertThrows(ValidacionNegocioException.class,
                () -> service.listarPorTipoDocencia(1, null, null, null, null, Pageable.unpaged()));
    }

    @Test
    void listarPorTipoDocencia_sinResultados_devuelveMensajeSinDatos() {
        Pageable pageable = Pageable.unpaged();
        when(asignacionRepository.findAll(ArgumentMatchers.<Specification<Asignacion>>any(), same(pageable))).thenReturn(Page.empty(pageable));

        ApiResponse<Page<DocenciaDTOResponse>> response =
                service.listarPorTipoDocencia(1, 2, null, null, null, pageable);

        assertEquals(200, response.getCodigo());
        assertEquals("No se encontraron actividades de Docencia.", response.getMensaje());
        assertEquals(0, response.getData().getTotalElements());
    }

    @Test
    void listarPorTipoDocencia_tipoContratacionInvalida_lanzaExcepcion() {
        assertThrows(ValidacionNegocioException.class,
                () -> service.listarPorTipoDocencia(1, 2, null, "Tipo inexistente", null, Pageable.unpaged()));
    }

    @Test
    void validarCupoUsuariosEnCargo_sinUsuarios_lanzaExcepcion() {
        assertThrows(ValidacionNegocioException.class,
                () -> service.validarCupoUsuariosEnCargo(1, null, 2, List.of()));
    }

    @Test
    void validarCupoUsuariosEnCargo_conCargoCalculaMenorCupo() {
        CargoActividad cargo = new CargoActividad();
        cargo.setOidCargoActividad(9);
        cargo.setMaxHorasSemana(12f);
        TipoActividad tipo = new TipoActividad();
        tipo.setOidTipoActividad(4);
        cargo.setTipoActividad(tipo);

        when(cargoActividadRepository.findById(9)).thenReturn(Optional.of(cargo));

        when(usuarioActividadCalendarioRepository
                .findByUsuario_OidUsuarioAndActividadCalendario_CargoActividad_OidCargoActividad(1, 9))
                .thenReturn(List.of(relacionConHoras(4f)));
        when(usuarioActividadCalendarioRepository
                .findByUsuario_OidUsuarioAndActividadCalendario_CargoActividad_OidCargoActividad(2, 9))
                .thenReturn(List.of(relacionConHoras(12f)));

        Fecha inicio = new Fecha();
        inicio.setFechaInicial(LocalDateTime.of(2024, 1, 1, 0, 0));
        Fecha fin = new Fecha();
        fin.setFechaInicial(LocalDateTime.of(2024, 1, 29, 0, 0));
        when(fechaRepository.findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(5, 1))
                .thenReturn(Optional.of(inicio));
        when(fechaRepository.findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(5, 10))
                .thenReturn(Optional.of(fin));

        ApiResponse<ValidacionHorasCargoDTOResponse> response = service.validarCupoUsuariosEnCargo(
                null, 9, 5, List.of(1, 2));

        assertEquals(200, response.getCodigo());
        ValidacionHorasCargoDTOResponse data = response.getData();
        assertFalse(data.isPuedeAsignar());
        assertEquals(2, data.getOidUsuarioMenorCupo());
        assertEquals(0f, data.getHorasDisponiblesUsuarioMenorCupo());
        assertEquals(12f, data.getHorasMaximasCargo());
        assertEquals(5f, data.getSemanasMaximas());
    }

    @Test
    void crearActividadConRelaciones_usuarioNoPermitidoPorContratacion_lanzaExcepcion() {
        UsuarioActividadCalendarioDTORequest request = new UsuarioActividadCalendarioDTORequest();
        request.setOidTipoActividad(1);
        request.setOidEstadoActividad(2);
        request.setNombreActividad("Actividad test");
        request.setHoras(4f);
        request.setOidCalendario(3);
        request.setOidsUsuarios(List.of(10));

        TipoActividad tipoActividad = new TipoActividad();
        tipoActividad.setOidTipoActividad(1);
        EstadoActividad estadoActividad = new EstadoActividad();
        estadoActividad.setOidEstadoActividad(2);
        Calendario calendario = new Calendario();
        calendario.setOidcalendario(3);
        calendario.setHorasPlanta(30f);
        calendario.setHorasOcasionales(25f);

        UsuarioDetalle detalle = new UsuarioDetalle();
        detalle.setContratacion("CATEDRA");
        Usuario usuario = new Usuario();
        usuario.setOidUsuario(10);
        usuario.setUsuarioDetalle(detalle);
        usuario.setIdentificacion("1000");
        usuario.setNombres("Nombre");
        usuario.setApellidos("Apellido");

        when(tipoActividadRepository.findById(1)).thenReturn(Optional.of(tipoActividad));
        when(estadoActividadRepository.findById(2)).thenReturn(Optional.of(estadoActividad));
        when(calendarioRepository.findById(3)).thenReturn(Optional.of(calendario));
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuario));
        lenient().when(usuarioActividadCalendarioRepository.findByUsuario_OidUsuario(10)).thenReturn(List.of());

        assertThrows(ValidacionNegocioException.class, () -> service.crearActividadConRelaciones(request));
    }

    @Test
    void crearActividadConRelaciones_usuarioPlantaSinHorasEnCalendarioUsaLimiteDefault() {
        UsuarioActividadCalendarioDTORequest request = new UsuarioActividadCalendarioDTORequest();
        request.setOidTipoActividad(11);
        request.setOidEstadoActividad(22);
        request.setNombreActividad("Actividad");
        request.setHoras(41f);
        request.setOidCalendario(33);
        request.setOidsUsuarios(List.of(44));

        TipoActividad tipoActividad = new TipoActividad();
        tipoActividad.setOidTipoActividad(11);
        EstadoActividad estadoActividad = new EstadoActividad();
        estadoActividad.setOidEstadoActividad(22);
        Calendario calendario = new Calendario();
        calendario.setOidcalendario(33);
        calendario.setHorasPlanta(null);

        UsuarioDetalle detalle = new UsuarioDetalle();
        detalle.setContratacion("PLANTA");
        Usuario usuario = new Usuario();
        usuario.setOidUsuario(44);
        usuario.setUsuarioDetalle(detalle);
        usuario.setIdentificacion("2000");
        usuario.setNombres("Nombre");
        usuario.setApellidos("Apellido");

        when(tipoActividadRepository.findById(11)).thenReturn(Optional.of(tipoActividad));
        when(estadoActividadRepository.findById(22)).thenReturn(Optional.of(estadoActividad));
        when(calendarioRepository.findById(33)).thenReturn(Optional.of(calendario));
        when(usuarioRepository.findById(44)).thenReturn(Optional.of(usuario));
        when(usuarioActividadCalendarioRepository.findByUsuario_OidUsuario(44)).thenReturn(List.of());

        ValidacionNegocioException exception = assertThrows(ValidacionNegocioException.class,
                () -> service.crearActividadConRelaciones(request));
        assertTrue(exception.getMessage().contains("40"), "Debe usar el límite por defecto de 40 horas.");
    }

    @Test
    void crearActividadConRelaciones_usuarioPlantaMedioTiempoNoPuedeExcederMitadLimite() {
        UsuarioActividadCalendarioDTORequest request = new UsuarioActividadCalendarioDTORequest();
        request.setOidTipoActividad(11);
        request.setOidEstadoActividad(22);
        request.setNombreActividad("Actividad");
        request.setHoras(11f);
        request.setOidCalendario(33);
        request.setOidsUsuarios(List.of(44));

        TipoActividad tipoActividad = new TipoActividad();
        tipoActividad.setOidTipoActividad(11);
        EstadoActividad estadoActividad = new EstadoActividad();
        estadoActividad.setOidEstadoActividad(22);
        Calendario calendario = new Calendario();
        calendario.setOidcalendario(33);
        calendario.setHorasPlanta(20f);

        UsuarioDetalle detalle = new UsuarioDetalle();
        detalle.setContratacion("PLANTA");
        detalle.setDedicacion("MEDIO TIEMPO");
        Usuario usuario = new Usuario();
        usuario.setOidUsuario(44);
        usuario.setUsuarioDetalle(detalle);
        usuario.setIdentificacion("3000");
        usuario.setNombres("Docente");
        usuario.setApellidos("Medio Tiempo");

        when(tipoActividadRepository.findById(11)).thenReturn(Optional.of(tipoActividad));
        when(estadoActividadRepository.findById(22)).thenReturn(Optional.of(estadoActividad));
        when(calendarioRepository.findById(33)).thenReturn(Optional.of(calendario));
        when(usuarioRepository.findById(44)).thenReturn(Optional.of(usuario));
        when(usuarioActividadCalendarioRepository.findByUsuario_OidUsuario(44)).thenReturn(List.of());

        ValidacionNegocioException exception = assertThrows(ValidacionNegocioException.class,
                () -> service.crearActividadConRelaciones(request));
        assertTrue(exception.getMessage().contains("10"),
                "Para medio tiempo el límite debería ser la mitad del configurado en el calendario.");
    }

    private UsuarioActividadCalendario relacionConHoras(float horas) {
        Actividad actividad = new Actividad();
        actividad.setHoras(horas);
        ActividadCalendario actividadCalendario = new ActividadCalendario();
        actividadCalendario.setActividad(actividad);

        UsuarioActividadCalendario relacion = new UsuarioActividadCalendario();
        relacion.setActividadCalendario(actividadCalendario);
        return relacion;
    }
}
