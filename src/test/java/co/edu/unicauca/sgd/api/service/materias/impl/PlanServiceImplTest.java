package co.edu.unicauca.sgd.api.service.materias.impl;

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

import java.util.Optional;

import co.edu.unicauca.sgd.api.domain.Plan;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.PlanDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.PlanDTOResponse;
import co.edu.unicauca.sgd.api.mapper.PlanMapper;
import co.edu.unicauca.sgd.api.repository.MateriaRepository;
import co.edu.unicauca.sgd.api.repository.PlanRepository;

@ExtendWith(MockitoExtension.class)
class PlanServiceImplTest {

    @Mock
    private PlanRepository planRepository;
    @Mock
    private MateriaRepository materiaRepository;
    @Mock
    private PlanMapper planMapper;

    private PlanServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PlanServiceImpl(planRepository, materiaRepository, planMapper);
    }

    @Test
    void obtenerTodos_sinDatos_devuelveMensajeAdecuado() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(planRepository.findAll(ArgumentMatchers.<Specification<Plan>>any(), eq(pageable))).thenReturn(Page.empty(pageable));

        ApiResponse<Page<PlanDTOResponse>> response = service.obtenerTodos(null, null, null, pageable);

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(response.getMensaje()).isEqualTo("No se encontraron planes.");
        assertThat(response.getData().getContent()).isEmpty();
    }

    @Test
    void buscarPorId_planNoExiste_devuelve404() {
        Integer oid = 1;
        when(planRepository.findById(oid)).thenReturn(Optional.empty());

        ApiResponse<PlanDTOResponse> response = service.buscarPorId(oid);

        assertThat(response.getCodigo()).isEqualTo(404);
        assertThat(response.getData()).isNull();
        assertThat(response.getMensaje()).isEqualTo("Plan no encontrado con ID: " + oid);
    }

    @Test
    void guardar_conPlanBaseInexistente_devuelve404() {
        Integer oidPlanBase = 10;
        PlanDTORequest request = new PlanDTORequest();
        request.setOidPlanBase(oidPlanBase);

        when(planRepository.findById(oidPlanBase)).thenReturn(Optional.empty());

        ApiResponse<PlanDTOResponse> response = service.guardar(request);

        assertThat(response.getCodigo()).isEqualTo(404);
        assertThat(response.getData()).isNull();
        assertThat(response.getMensaje()).isEqualTo("Plan base no encontrado con ID: " + oidPlanBase);
    }
}
