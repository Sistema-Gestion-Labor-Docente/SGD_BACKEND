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

import co.edu.unicauca.sgd.api.domain.Programa;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.ProgramaDTOResponse;
import co.edu.unicauca.sgd.api.mapper.ProgramaMapper;
import co.edu.unicauca.sgd.api.repository.ProgramaRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ProgramaServiceImplTest {

    @Mock
    private ProgramaRepository programaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ProgramaMapper programaMapper;

    private ProgramaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProgramaServiceImpl(programaRepository, usuarioRepository, programaMapper);
    }

    @Test
    void obtenerTodos_sinDatos_devuelveMensajeAcorde() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(programaRepository.findAll(ArgumentMatchers.<Specification<Programa>>any(), eq(pageable))).thenReturn(Page.empty(pageable));

        ApiResponse<Page<ProgramaDTOResponse>> response = service.obtenerTodos(null, pageable);

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(response.getMensaje()).isEqualTo("No se encontraron programas.");
        assertThat(response.getData().getContent()).isEmpty();
    }
}
