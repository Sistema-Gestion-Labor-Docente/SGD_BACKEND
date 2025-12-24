package co.edu.unicauca.sgd.api.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.repository.ActividadBooleanRepository;
import co.edu.unicauca.sgd.api.repository.ActividadDateRepository;
import co.edu.unicauca.sgd.api.repository.ActividadDecimalRepository;
import co.edu.unicauca.sgd.api.repository.ActividadIntRepository;
import co.edu.unicauca.sgd.api.repository.ActividadVarcharRepository;
import co.edu.unicauca.sgd.api.repository.EavAtributoRepository;

@ExtendWith(MockitoExtension.class)
class EavAtributoServiceTest {

    @Mock
    private ActividadIntRepository actividadIntRepository;
    @Mock
    private ActividadDecimalRepository actividadDecimalRepository;
    @Mock
    private ActividadVarcharRepository actividadVarcharRepository;
    @Mock
    private ActividadDateRepository actividadDateRepository;
    @Mock
    private ActividadBooleanRepository actividadBooleanRepository;
    @Mock
    private EavAtributoRepository eavAtributoRepository;

    @InjectMocks
    private EavAtributoService eavAtributoService;

    @Test
    void eliminarAtributosActividad_DeberiaEjecutarBorradoEnTodosLosRepositorios() {
        Actividad actividad = new Actividad();

        eavAtributoService.eliminarAtributosActividad(actividad);

        verify(actividadVarcharRepository).deleteByActividad(actividad);
        verify(actividadVarcharRepository).flush();
        verify(actividadDecimalRepository).deleteByActividad(actividad);
        verify(actividadDecimalRepository).flush();
        verify(actividadIntRepository).deleteByActividad(actividad);
        verify(actividadIntRepository).flush();
        verify(actividadBooleanRepository).deleteByActividad(actividad);
        verify(actividadBooleanRepository).flush();
        verify(actividadDateRepository).deleteByActividad(actividad);
        verify(actividadDateRepository).flush();
    }

    @Test
    void eliminarAtributosActividad_NoDebeInteractuarConRepositoriosCuandoActividadNula() {
        eavAtributoService.eliminarAtributosActividad(null);

        verifyNoInteractions(
                actividadVarcharRepository,
                actividadDecimalRepository,
                actividadIntRepository,
                actividadBooleanRepository,
                actividadDateRepository);
    }
}
