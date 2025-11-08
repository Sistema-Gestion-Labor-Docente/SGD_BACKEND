package co.edu.unicauca.sgd.api.service.calendario.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.NombreFechaDTOResponse;
import co.edu.unicauca.sgd.api.mapper.NombreFechaMapper;
import co.edu.unicauca.sgd.api.repository.NombreFechaRepository;

@ExtendWith(MockitoExtension.class)
class NombreFechaServiceImplTest {

    @Mock
    private NombreFechaRepository repository;
    @Mock
    private NombreFechaMapper mapper;

    private NombreFechaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NombreFechaServiceImpl(repository, mapper);
    }

    @Test
    void obtenerTodas_sinResultadosDevuelveMensajeVacio() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(repository.findByOidNombreFechaNotIn(any(), any(Pageable.class))).thenReturn(Page.empty(pageable));

        ApiResponse<Page<NombreFechaDTOResponse>> response = service.obtenerTodas(null, pageable);

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(response.getMensaje()).isEqualTo("No se encontraron nombres de fecha.");
        assertThat(response.getData().getContent()).isEmpty();
    }
}
