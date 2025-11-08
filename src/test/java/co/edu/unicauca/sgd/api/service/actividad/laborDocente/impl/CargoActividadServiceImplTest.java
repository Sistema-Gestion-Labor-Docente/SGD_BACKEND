package co.edu.unicauca.sgd.api.service.actividad.laborDocente.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import co.edu.unicauca.sgd.api.domain.CargoActividad;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.CargoActividadDTOResponse;
import co.edu.unicauca.sgd.api.mapper.CargoActividadMapper;
import co.edu.unicauca.sgd.api.repository.CargoActividadRepository;
import co.edu.unicauca.sgd.api.repository.TipoActividadRepository;

@ExtendWith(MockitoExtension.class)
class CargoActividadServiceImplTest {

    @Mock
    private CargoActividadRepository cargoActividadRepository;
    @Mock
    private TipoActividadRepository tipoActividadRepository;
    @Mock
    private CargoActividadMapper mapper;

    private CargoActividadServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CargoActividadServiceImpl(cargoActividadRepository, tipoActividadRepository, mapper);
    }

    @Test
    void obtenerTodos_cuandoNoHayDatosDevuelveMensajeAdecuado() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(cargoActividadRepository.findAll(ArgumentMatchers.<Specification<CargoActividad>>any(), eq(pageable))).thenReturn(Page.empty(pageable));

        ApiResponse<Page<CargoActividadDTOResponse>> response =
                service.obtenerTodos(null, null, null, pageable);

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(response.getMensaje()).isEqualTo("No se encontraron cargos de actividad.");
        assertThat(response.getData().getContent()).isEmpty();
    }
}
