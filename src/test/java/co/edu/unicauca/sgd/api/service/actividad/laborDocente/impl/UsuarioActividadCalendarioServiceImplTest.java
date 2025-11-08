package co.edu.unicauca.sgd.api.service.actividad.laborDocente.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.DocenciaDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTOResponse;
import co.edu.unicauca.sgd.api.exception.RecursoNoEncontradoException;
import co.edu.unicauca.sgd.api.exception.ValidacionNegocioException;
import co.edu.unicauca.sgd.api.mapper.UsuarioActividadCalendarioMapper;
import co.edu.unicauca.sgd.api.repository.ActividadCalendarioRepository;
import co.edu.unicauca.sgd.api.repository.ActividadRepository;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.CargoActividadRepository;
import co.edu.unicauca.sgd.api.repository.EavAtributoRepository;
import co.edu.unicauca.sgd.api.repository.EstadoActividadRepository;
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
}
