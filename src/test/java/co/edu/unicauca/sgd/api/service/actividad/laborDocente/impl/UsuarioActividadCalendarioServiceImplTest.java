package co.edu.unicauca.sgd.api.service.actividad.laborDocente.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unicauca.sgd.api.exception.RecursoNoEncontradoException;
import co.edu.unicauca.sgd.api.exception.ValidacionNegocioException;
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
import co.edu.unicauca.sgd.api.mapper.UsuarioActividadCalendarioMapper;

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
                () -> service.listarActividadesConRelaciones(null, null, null, Pageable.unpaged()));
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
}
