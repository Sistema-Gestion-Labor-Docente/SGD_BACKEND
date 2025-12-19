package co.edu.unicauca.sgd.api.service.materias.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

import co.edu.unicauca.sgd.api.domain.Materia;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.MateriaDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.MateriaDTOResponse;
import co.edu.unicauca.sgd.api.mapper.MateriaMapper;
import co.edu.unicauca.sgd.api.repository.DepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.MateriaRepository;
import co.edu.unicauca.sgd.api.repository.PlanRepository;

@ExtendWith(MockitoExtension.class)
class MateriaServiceImplTest {

    @Mock
    private MateriaRepository materiaRepository;
    @Mock
    private DepartamentoRepository departamentoRepository;
    @Mock
    private PlanRepository planRepository;
    @Mock
    private MateriaMapper materiaMapper;

    private MateriaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MateriaServiceImpl(materiaRepository, departamentoRepository, planRepository, materiaMapper);
    }

    @Test
    void obtenerTodos_sinDatos_devuelveMensajeSinMaterias() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(materiaRepository.findAll(ArgumentMatchers.<Specification<Materia>>any(), eq(pageable))).thenReturn(Page.empty(pageable));

        ApiResponse<Page<MateriaDTOResponse>> response =
                service.obtenerTodos(null, null, null, null, null, null, pageable);

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(response.getMensaje()).isEqualTo("No se encontraron materias.");
        assertThat(response.getData().getContent()).isEmpty();
    }

    @Test
    void actualizar_sinCorrequisitoEnRequest_desvinculaMateria() {
        Materia existente = new Materia();
        existente.setIdMateria(1);
        existente.setCorrequisito(new Materia());

        MateriaDTORequest request = new MateriaDTORequest();
        request.setIdCorrequisito(null);

        when(materiaRepository.findById(eq(1))).thenReturn(java.util.Optional.of(existente));
        when(materiaRepository.save(any(Materia.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(materiaMapper.toResponse(any(Materia.class))).thenReturn(new MateriaDTOResponse());

        ApiResponse<MateriaDTOResponse> response = service.actualizar(1, request);

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(existente.getCorrequisito()).isNull();
    }

    @Test
    void actualizar_conCorrequisitoInvalido_desvinculaMateria() {
        Materia existente = new Materia();
        existente.setIdMateria(2);
        existente.setCorrequisito(new Materia());

        MateriaDTORequest request = new MateriaDTORequest();
        request.setIdCorrequisito(-5); // no válido

        when(materiaRepository.findById(eq(2))).thenReturn(java.util.Optional.of(existente));
        when(materiaRepository.save(any(Materia.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(materiaMapper.toResponse(any(Materia.class))).thenReturn(new MateriaDTOResponse());

        ApiResponse<MateriaDTOResponse> response = service.actualizar(2, request);

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(existente.getCorrequisito()).isNull();
    }

    @Test
    void actualizar_conCorrequisitoNoEncontrado_desvinculaMateria() {
        Materia existente = new Materia();
        existente.setIdMateria(3);
        existente.setCorrequisito(new Materia());

        MateriaDTORequest request = new MateriaDTORequest();
        request.setIdCorrequisito(99);

        when(materiaRepository.findById(eq(3))).thenReturn(java.util.Optional.of(existente));
        when(materiaRepository.findById(eq(99))).thenReturn(java.util.Optional.empty());
        when(materiaRepository.save(any(Materia.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(materiaMapper.toResponse(any(Materia.class))).thenReturn(new MateriaDTOResponse());

        ApiResponse<MateriaDTOResponse> response = service.actualizar(3, request);

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(existente.getCorrequisito()).isNull();
    }
}
