package co.edu.unicauca.sgd.api.service.necesidad.impl;

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

import co.edu.unicauca.sgd.api.domain.Necesidad;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTOResponse;
import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;
import co.edu.unicauca.sgd.api.mapper.NecesidadMapper;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.MateriaRepository;
import co.edu.unicauca.sgd.api.repository.NecesidadRepository;

@ExtendWith(MockitoExtension.class)
class NecesidadServiceImplTest {

    @Mock
    private NecesidadRepository necesidadRepository;
    @Mock
    private CalendarioRepository calendarioRepository;
    @Mock
    private MateriaRepository materiaRepository;
    @Mock
    private NecesidadMapper necesidadMapper;

    private NecesidadServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NecesidadServiceImpl(necesidadRepository, calendarioRepository, materiaRepository, necesidadMapper);
    }

    @Test
    void obtenerTodos_sinResultados_devuelveMensajeSinDatos() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(necesidadRepository.findAll(ArgumentMatchers.<Specification<Necesidad>> any(), eq(pageable))).thenReturn(Page.empty(pageable));

        ApiResponse<Page<NecesidadDTOResponse>> response =
                service.obtenerTodos(null, null, EstadoNecesidad.NO_ASIGNADA, pageable);

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(response.getMensaje()).isEqualTo("No se encontraron necesidades.");
        assertThat(response.getData().getContent()).isEmpty();
    }
}
