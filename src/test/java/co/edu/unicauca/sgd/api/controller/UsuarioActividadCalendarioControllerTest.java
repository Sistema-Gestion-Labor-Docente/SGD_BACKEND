package co.edu.unicauca.sgd.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.ValidacionHorasCargoDTOResponse;
import co.edu.unicauca.sgd.api.service.actividad.laborDocente.UsuarioActividadCalendarioService;

@ExtendWith(MockitoExtension.class)
class UsuarioActividadCalendarioControllerTest {

    @Mock
    private UsuarioActividadCalendarioService service;

    private UsuarioActividadCalendarioController controller;

    @BeforeEach
    void setUp() {
        controller = new UsuarioActividadCalendarioController(service);
    }

    @Test
    void validarCupoUsuarios_devuelveRespuestaDelServicio() {
        ValidacionHorasCargoDTOResponse data = ValidacionHorasCargoDTOResponse.builder()
                .puedeAsignar(true)
                .horasMaximasCargo(12f)
                .horasDisponiblesUsuarioMenorCupo(6f)
                .oidUsuarioMenorCupo(10)
                .semanasMaximas(8f)
                .build();
        ApiResponse<ValidacionHorasCargoDTOResponse> serviceResponse =
                new ApiResponse<>(200, "ok", data);

        when(service.validarCupoUsuariosEnCargo(1, 2, 3, List.of(5, 7))).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<ValidacionHorasCargoDTOResponse>> response =
                controller.validarCupoUsuarios(1, 2, 3, List.of(5, 7));

        assertEquals(200, response.getStatusCodeValue());
        assertSame(data, response.getBody().getData());

        ArgumentCaptor<List<Integer>> usuariosCaptor = ArgumentCaptor.forClass(List.class);
        verify(service).validarCupoUsuariosEnCargo(1, 2, 3, usuariosCaptor.capture());
        assertEquals(List.of(5, 7), usuariosCaptor.getValue());
    }
}
