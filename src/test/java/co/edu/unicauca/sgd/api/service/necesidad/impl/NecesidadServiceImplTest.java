package co.edu.unicauca.sgd.api.service.necesidad.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Materia;
import co.edu.unicauca.sgd.api.domain.Necesidad;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadBulkCreateRequest;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTOResponse;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTORequest;
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

    @Test
    void guardar_conDatosValidosPersisteEntidad() {
        NecesidadDTORequest request = new NecesidadDTORequest();
        request.setOidCalendario(1);
        request.setIdMateria(2);
        request.setGrupo("b1");

        Calendario calendario = new Calendario();
        calendario.setOidcalendario(1);
        Materia materia = new Materia();
        materia.setIdMateria(2);
        materia.setNombre("Materia X");

        when(calendarioRepository.findById(1)).thenReturn(Optional.of(calendario));
        when(materiaRepository.findById(2)).thenReturn(Optional.of(materia));
        when(necesidadRepository.existsByCalendario_OidcalendarioAndMateria_IdMateriaAndGrupo(1, 2, "B1")).thenReturn(false);
        when(necesidadMapper.toEntity(request)).thenReturn(new Necesidad());
        when(necesidadRepository.save(any(Necesidad.class))).thenAnswer(inv -> {
            Necesidad n = inv.getArgument(0);
            n.setOidNecesidad(10);
            return n;
        });
        NecesidadDTOResponse dto = new NecesidadDTOResponse();
        when(necesidadMapper.toResponse(any(Necesidad.class))).thenReturn(dto);

        ApiResponse<NecesidadDTOResponse> response = service.guardar(request);

        assertThat(response.getCodigo()).isEqualTo(201);
        ArgumentCaptor<Necesidad> captor = ArgumentCaptor.forClass(Necesidad.class);
        verify(necesidadRepository).save(captor.capture());
        assertThat(captor.getValue().getGrupo()).isEqualTo("B1");
        assertThat(captor.getValue().getCalendario()).isEqualTo(calendario);
        assertThat(captor.getValue().getMateria()).isEqualTo(materia);
    }

    @Test
    void guardarMasivo_conMateriaDuplicadaDevuelveError() {
        NecesidadBulkCreateRequest request = new NecesidadBulkCreateRequest();
        request.setOidCalendario(3);
        NecesidadBulkCreateRequest.NecesidadBulkItemRequest item1 = new NecesidadBulkCreateRequest.NecesidadBulkItemRequest();
        item1.setIdMateria(5);
        item1.setCantidadGrupos(1);
        item1.setCupo(30);
        NecesidadBulkCreateRequest.NecesidadBulkItemRequest item2 = new NecesidadBulkCreateRequest.NecesidadBulkItemRequest();
        item2.setIdMateria(5);
        item2.setCantidadGrupos(2);
        item2.setCupo(20);
        request.setNecesidades(List.of(item1, item2));

        Calendario calendario = new Calendario();
        calendario.setOidcalendario(3);

        when(calendarioRepository.findById(3)).thenReturn(Optional.of(calendario));

        ApiResponse<java.util.List<NecesidadDTOResponse>> response = service.guardarMasivo(request);

        assertThat(response.getCodigo()).isEqualTo(400);
        verify(necesidadRepository, never()).save(any(Necesidad.class));
    }

    @Test
    void actualizar_conConflictoDeGrupoDevuelveError() {
        Necesidad existente = new Necesidad();
        existente.setOidNecesidad(15);
        Calendario calendario = new Calendario();
        calendario.setOidcalendario(1);
        existente.setCalendario(calendario);
        Materia materia = new Materia();
        materia.setIdMateria(9);
        materia.setNombre("Materia Y");
        existente.setMateria(materia);
        existente.setGrupo("A");

        NecesidadDTORequest request = new NecesidadDTORequest();
        request.setGrupo("B");

        Necesidad conflicto = new Necesidad();
        conflicto.setOidNecesidad(20);

        when(necesidadRepository.findById(15)).thenReturn(Optional.of(existente));
        when(necesidadRepository.findByCalendario_OidcalendarioAndMateria_IdMateriaAndGrupo(1, 9, "B"))
                .thenReturn(Optional.of(conflicto));

        ApiResponse<NecesidadDTOResponse> response = service.actualizar(15, request);

        assertThat(response.getCodigo()).isEqualTo(400);
        verify(necesidadRepository, never()).save(any(Necesidad.class));
    }

    @Test
    void eliminar_conIdInexistenteDevuelve404() {
        when(necesidadRepository.existsById(40)).thenReturn(false);

        ApiResponse<Void> response = service.eliminar(40);

        assertThat(response.getCodigo()).isEqualTo(404);
    }
}
