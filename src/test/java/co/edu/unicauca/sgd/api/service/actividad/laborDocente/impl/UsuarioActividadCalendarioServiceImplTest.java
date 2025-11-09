package co.edu.unicauca.sgd.api.service.actividad.laborDocente.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.DocenciaDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.ValidacionHorasCargoDTOResponse;
import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.ActividadCalendario;
import co.edu.unicauca.sgd.api.domain.CargoActividad;
import co.edu.unicauca.sgd.api.domain.Fecha;
import co.edu.unicauca.sgd.api.domain.TipoActividad;
import co.edu.unicauca.sgd.api.domain.UsuarioActividadCalendario;
import co.edu.unicauca.sgd.api.exception.RecursoNoEncontradoException;
import co.edu.unicauca.sgd.api.exception.ValidacionNegocioException;
import co.edu.unicauca.sgd.api.mapper.UsuarioActividadCalendarioMapper;
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

    private UsuarioActividadCalendarioServiceImpl service;

    @BeforeEach
    void setUp() {
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
    void listarPorTipoDocencia_sinResultados_devuelveMensajeSinDatos() {
        Pageable pageable = Pageable.unpaged();
        when(actividadRepository.findByTipoActividad_Nombre("Docencia", pageable))
                .thenReturn(Page.empty(pageable));

        ApiResponse<Page<DocenciaDTOResponse>> response = service.listarPorTipoDocencia(pageable);

        assertEquals(200, response.getCodigo());
        assertEquals("No se encontraron actividades de Docencia.", response.getMensaje());
        assertEquals(0, response.getData().getTotalElements());
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
