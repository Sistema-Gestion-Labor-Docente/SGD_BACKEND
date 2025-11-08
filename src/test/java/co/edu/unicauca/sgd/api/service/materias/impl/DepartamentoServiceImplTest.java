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

import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.DepartamentoDTOResponse;
import co.edu.unicauca.sgd.api.mapper.DepartamentoMapper;
import co.edu.unicauca.sgd.api.repository.DepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class DepartamentoServiceImplTest {

    @Mock
    private DepartamentoRepository departamentoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private DepartamentoMapper departamentoMapper;

    private DepartamentoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DepartamentoServiceImpl(departamentoRepository, usuarioRepository, departamentoMapper);
    }

    @Test
    void obtenerTodos_sinDatos_devuelveMensajeSinDepartamentos() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(departamentoRepository.findAll(ArgumentMatchers.<Specification<Departamento>>any(), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        ApiResponse<Page<DepartamentoDTOResponse>> response = service.obtenerTodos(null, pageable);

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(response.getMensaje()).isEqualTo("No se encontraron departamentos.");
        assertThat(response.getData().getContent()).isEmpty();
    }
}
