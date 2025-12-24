package co.edu.unicauca.sgd.api.service.necesidad.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.notNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.Asignacion;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.EstadoActividad;
import co.edu.unicauca.sgd.api.domain.Materia;
import co.edu.unicauca.sgd.api.domain.Plan;
import co.edu.unicauca.sgd.api.domain.Programa;
import co.edu.unicauca.sgd.api.domain.Necesidad;
import co.edu.unicauca.sgd.api.domain.Seleccionado;
import co.edu.unicauca.sgd.api.domain.TipoActividad;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioDetalle;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.AtributoDTO;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sgd.api.dto.necesidades.AsignacionDTORequest;
import co.edu.unicauca.sgd.api.dto.necesidades.AsignacionDTOResponse;
import co.edu.unicauca.sgd.api.enums.ContratacionEnum;
import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;
import co.edu.unicauca.sgd.api.exception.ValidacionNegocioException;
import co.edu.unicauca.sgd.api.exception.asignacion.AsignacionCalendarioInvalidoException;
import co.edu.unicauca.sgd.api.exception.asignacion.AsignacionLimiteDocentesException;
import co.edu.unicauca.sgd.api.exception.asignacion.AsignacionOperacionNoPermitidaException;
import co.edu.unicauca.sgd.api.mapper.AsignacionMapper;
import co.edu.unicauca.sgd.api.repository.ActividadRepository;
import co.edu.unicauca.sgd.api.repository.AsignacionRepository;
import co.edu.unicauca.sgd.api.repository.EstadoActividadRepository;
import co.edu.unicauca.sgd.api.repository.EavAtributoRepository;
import co.edu.unicauca.sgd.api.repository.NecesidadRepository;
import co.edu.unicauca.sgd.api.repository.SeleccionadoRepository;
import co.edu.unicauca.sgd.api.repository.TipoActividadRepository;
import co.edu.unicauca.sgd.api.service.EavAtributoService;

@ExtendWith(MockitoExtension.class)
class AsignacionServiceImplTest {

    @Mock
    private AsignacionRepository asignacionRepository;
    @Mock
    private NecesidadRepository necesidadRepository;
    @Mock
    private SeleccionadoRepository seleccionadoRepository;
    @Mock
    private TipoActividadRepository tipoActividadRepository;
    @Mock
    private EstadoActividadRepository estadoActividadRepository;
    @Mock
    private ActividadRepository actividadRepository;
    @Mock
    private EavAtributoService eavAtributoService;
    @Mock
    private EavAtributoRepository eavAtributoRepository;
    @Mock
    private AsignacionMapper asignacionMapper;

    @InjectMocks
    private AsignacionServiceImpl asignacionService;

    private Necesidad necesidad;
    private Seleccionado seleccionado;

    @BeforeEach
    void init() {
        Calendario calendario = new Calendario();
        calendario.setOidcalendario(10);
        calendario.setSemanasClase(16f);
        calendario.setSemanasPreparacion(4f);

        Materia materia = new Materia();
        materia.setHorasSemana(12);
        materia.setCodigo("MAT-101");
        materia.setNombre("Calculo");
        materia.setSemestre(3);
        Departamento departamento = new Departamento();
        departamento.setOidDepartamento(20);
        materia.setDepartamento(departamento);

        Plan plan = new Plan();
        Programa programa = new Programa();
        programa.setNombre("Programa de Prueba");
        plan.setPrograma(programa);
        materia.setPlan(plan);

        necesidad = new Necesidad();
        necesidad.setOidNecesidad(1);
        necesidad.setCalendario(calendario);
        necesidad.setMateria(materia);
        necesidad.setGrupo("A");
        necesidad.setEstado(EstadoNecesidad.NO_ASIGNADA);

        UsuarioDetalle detalle = new UsuarioDetalle();
        detalle.setDedicacion("TIEMPO COMPLETO");
        Usuario usuario = new Usuario();
        usuario.setUsuarioDetalle(detalle);
        usuario.setNombres("Docente");
        usuario.setApellidos("Prueba");

        seleccionado = new Seleccionado();
        seleccionado.setOidSeleccionado(2);
        seleccionado.setCalendario(calendario);
        seleccionado.setTipo(ContratacionEnum.PLANTA);
        seleccionado.setUsuario(usuario);
    }

    @Test
    void listar_DeberiaFiltrarPorSeleccionado() {
        Asignacion asignacion = buildAsignacion(1, seleccionado.getOidSeleccionado());
        Page<Asignacion> page = new PageImpl<>(Collections.singletonList(asignacion), Pageable.unpaged(), 1);

        when(asignacionRepository.findAll(ArgumentMatchers.<Specification<Asignacion>>any(), eq(Pageable.unpaged()))).thenReturn(page);

        AsignacionDTOResponse dto = AsignacionDTOResponse.builder().build();
        when(asignacionMapper.toResponse(asignacion)).thenReturn(dto);

        ApiResponse<Page<AsignacionDTOResponse>> response =
                asignacionService.listar(10, 20, 1, seleccionado.getOidSeleccionado(), null, null, null, Pageable.unpaged());

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(response.getData().getTotalElements()).isEqualTo(1);
        assertThat(response.getData().getContent()).containsExactly(dto);
        verify(asignacionRepository).findAll(ArgumentMatchers.<Specification<Asignacion>>any(), eq(Pageable.unpaged()));
    }

    @Test
    void listar_DeberiaRetornar400SiFaltanParametrosObligatorios() {
        ApiResponse<Page<AsignacionDTOResponse>> sinCalendario =
                asignacionService.listar(null, 20, null, null, null, null, null, Pageable.unpaged());
        assertThat(sinCalendario.getCodigo()).isEqualTo(400);

        ApiResponse<Page<AsignacionDTOResponse>> sinDepartamento =
                asignacionService.listar(10, null, null, null, null, null, null, Pageable.unpaged());
        assertThat(sinDepartamento.getCodigo()).isEqualTo(400);

        verify(asignacionRepository, never()).findAll(ArgumentMatchers.<Specification<Asignacion>>any(), any(Pageable.class));
    }

    @Test
    void listar_DeberiaRetornar400SiSemestreInvalido() {
        ApiResponse<Page<AsignacionDTOResponse>> response =
                asignacionService.listar(10, 20, null, null, null, 20, null, Pageable.unpaged());

        assertThat(response.getCodigo()).isEqualTo(400);
        verify(asignacionRepository, never()).findAll(ArgumentMatchers.<Specification<Asignacion>>any(), any(Pageable.class));
    }

    @Test
    void crear_DeberiaGuardarAsignacionYRedistribuirHoras() {
        AsignacionDTORequest request = buildRequest();

        TipoActividad tipoActividad = new TipoActividad();
        EstadoActividad estadoActividad = new EstadoActividad();

        when(eavAtributoRepository.findAll()).thenReturn(Collections.emptyList());

        when(necesidadRepository.findById(1)).thenReturn(Optional.of(necesidad));
        when(seleccionadoRepository.findById(2)).thenReturn(Optional.of(seleccionado));
        when(asignacionRepository.countByNecesidad_OidNecesidad(1)).thenReturn(0L, 1L);
        when(asignacionRepository.findByNecesidad_OidNecesidadAndSeleccionado_OidSeleccionado(1, 2)).thenReturn(Optional.empty());
        when(tipoActividadRepository.findByNombreIgnoreCase("DOCENCIA")).thenReturn(Optional.of(tipoActividad));
        when(estadoActividadRepository.findById(3)).thenReturn(Optional.of(estadoActividad));
        when(actividadRepository.save(notNull(Actividad.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Asignacion asignacionGuardada = new Asignacion();
        asignacionGuardada.setNecesidad(necesidad);
        asignacionGuardada.setSeleccionado(seleccionado);
        Actividad actividadAsignacion = new Actividad();
        actividadAsignacion.setNombreActividad("Labor");
        asignacionGuardada.setActividad(actividadAsignacion);
        when(asignacionRepository.findBySeleccionado_OidSeleccionado(2)).thenReturn(Collections.singletonList(asignacionGuardada));
        when(asignacionRepository.findBySeleccionado_OidSeleccionado(2)).thenReturn(Collections.singletonList(asignacionGuardada));

        when(asignacionRepository.save(notNull(Asignacion.class))).thenAnswer(invocation -> {
            Asignacion asignacion = invocation.getArgument(0, Asignacion.class);
            asignacion.setOidAsignacion(50);
            return asignacion;
        });
        when(asignacionRepository.findByNecesidad_OidNecesidad(1)).thenReturn(Collections.singletonList(asignacionGuardada));

        AsignacionDTOResponse dto = AsignacionDTOResponse.builder().build();
        when(asignacionMapper.toResponse(notNull(Asignacion.class))).thenReturn(dto);

        ApiResponse<AsignacionDTOResponse> response = asignacionService.crear(request);

        assertThat(response.getCodigo()).isEqualTo(201);
        assertThat(response.getData()).isEqualTo(dto);

        ArgumentCaptor<ActividadBaseDTO> actividadCaptor = ArgumentCaptor.forClass(ActividadBaseDTO.class);
        Float horasEsperadas = 12f;
        assertThat(asignacionGuardada.getHorasDocencia()).isEqualTo(horasEsperadas);
        verify(asignacionRepository).saveAll(ArgumentMatchers.<Iterable<Asignacion>>any());
        verify(actividadRepository).save(notNull(Actividad.class));
        verify(eavAtributoService).actualizarAtributosDinamicos(actividadCaptor.capture(), any(), any());
        assertThat(actividadCaptor.getValue().getNombreActividad()).isEqualTo("Calculo");
        assertThat(actividadCaptor.getValue().getAtributos())
                .extracting(AtributoDTO::getCodigoAtributo, AtributoDTO::getValor)
                .containsExactly(
                        tuple("CODIGO", "MAT-101"),
                        tuple("GRUPO", "A"),
                        tuple("MATERIA", "Calculo"),
                        tuple("PROGRAMA", "Programa de Prueba"),
                        tuple("SEMESTRE", "3")
                );
        assertThat(necesidad.getEstado()).isEqualTo(EstadoNecesidad.ASIGNADA);
        verify(necesidadRepository).save(necesidad);
    }

    @Test
    void crear_DeberiaFallarCuandoNoHaySemanasPreparacionParaPlanta() {
        AsignacionDTORequest request = buildRequest();
        necesidad.getCalendario().setSemanasPreparacion(null);

        when(necesidadRepository.findById(1)).thenReturn(Optional.of(necesidad));
        when(seleccionadoRepository.findById(2)).thenReturn(Optional.of(seleccionado));

        assertThrows(ValidacionNegocioException.class, () -> asignacionService.crear(request));
    }

    @Test
    void crear_DeberiaFallarCuandoSuperaHorasPermitidas() {
        AsignacionDTORequest request = buildRequest();

        TipoActividad tipoActividad = new TipoActividad();
        EstadoActividad estadoActividad = new EstadoActividad();

        when(necesidadRepository.findById(1)).thenReturn(Optional.of(necesidad));
        when(seleccionadoRepository.findById(2)).thenReturn(Optional.of(seleccionado));
        when(asignacionRepository.countByNecesidad_OidNecesidad(1)).thenReturn(0L, 1L);
        when(asignacionRepository.findByNecesidad_OidNecesidadAndSeleccionado_OidSeleccionado(1, 2)).thenReturn(Optional.empty());
        when(tipoActividadRepository.findByNombreIgnoreCase("DOCENCIA")).thenReturn(Optional.of(tipoActividad));
        when(estadoActividadRepository.findById(3)).thenReturn(Optional.of(estadoActividad));
        when(actividadRepository.save(notNull(Actividad.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Asignacion asignacionGuardada = new Asignacion();
        asignacionGuardada.setNecesidad(necesidad);
        asignacionGuardada.setSeleccionado(seleccionado);
        asignacionGuardada.setActividad(new Actividad());
        asignacionGuardada.setHorasDocencia(6f);
        asignacionGuardada.setHorasDocencia(12f);

        Asignacion asignacionExistente = new Asignacion();
        asignacionExistente.setSeleccionado(seleccionado);
        asignacionExistente.setHorasDocencia(13f);

        when(asignacionRepository.save(notNull(Asignacion.class))).thenAnswer(invocation -> {
            Asignacion asignacion = invocation.getArgument(0, Asignacion.class);
            asignacion.setOidAsignacion(60);
            asignacion.setSeleccionado(seleccionado);
            asignacion.setHorasDocencia(12f);
            return asignacion;
        });
        when(asignacionRepository.findByNecesidad_OidNecesidad(1)).thenReturn(Collections.singletonList(asignacionGuardada));
        when(asignacionRepository.findBySeleccionado_OidSeleccionado(2)).thenReturn(Arrays.asList(asignacionExistente, asignacionGuardada));

        assertThrows(ValidacionNegocioException.class, () -> asignacionService.crear(request));
    }

    @Test
    void crear_PlantaMedioTiempoNoDebeExcederOchoHoras() {
        AsignacionDTORequest request = buildRequest();
        seleccionado.getUsuario().getUsuarioDetalle().setDedicacion("MEDIO TIEMPO");

        TipoActividad tipoActividad = new TipoActividad();
        EstadoActividad estadoActividad = new EstadoActividad();

        when(necesidadRepository.findById(1)).thenReturn(Optional.of(necesidad));
        when(seleccionadoRepository.findById(2)).thenReturn(Optional.of(seleccionado));
        when(asignacionRepository.countByNecesidad_OidNecesidad(1)).thenReturn(0L, 1L);
        when(asignacionRepository.findByNecesidad_OidNecesidadAndSeleccionado_OidSeleccionado(1, 2)).thenReturn(Optional.empty());
        when(tipoActividadRepository.findByNombreIgnoreCase("DOCENCIA")).thenReturn(Optional.of(tipoActividad));
        when(estadoActividadRepository.findById(3)).thenReturn(Optional.of(estadoActividad));
        when(actividadRepository.save(notNull(Actividad.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Asignacion asignacionGuardada = new Asignacion();
        asignacionGuardada.setNecesidad(necesidad);
        asignacionGuardada.setSeleccionado(seleccionado);
        asignacionGuardada.setActividad(new Actividad());

        when(asignacionRepository.save(notNull(Asignacion.class))).thenAnswer(invocation -> {
            Asignacion asignacion = invocation.getArgument(0, Asignacion.class);
            asignacion.setOidAsignacion(80);
            asignacion.setHorasDocencia(12f);
            asignacion.setSeleccionado(seleccionado);
            asignacion.setNecesidad(necesidad);
            return asignacion;
        });
        when(asignacionRepository.findByNecesidad_OidNecesidad(1)).thenReturn(Collections.singletonList(asignacionGuardada));
        when(asignacionRepository.findBySeleccionado_OidSeleccionado(2))
                .thenReturn(List.of(asignacionGuardada, asignacionGuardada));

        assertThrows(ValidacionNegocioException.class, () -> asignacionService.crear(request));
    }

    @Test
    void crear_OcasionalMedioTiempoNoDebeExcederDoceHoras() {
        AsignacionDTORequest request = buildRequest();
        seleccionado.setTipo(ContratacionEnum.OCASIONAL);
        seleccionado.getUsuario().getUsuarioDetalle().setDedicacion("MEDIO TIEMPO");
        necesidad.getMateria().setHorasSemana(20);

        TipoActividad tipoActividad = new TipoActividad();
        EstadoActividad estadoActividad = new EstadoActividad();

        when(necesidadRepository.findById(1)).thenReturn(Optional.of(necesidad));
        when(seleccionadoRepository.findById(2)).thenReturn(Optional.of(seleccionado));
        when(asignacionRepository.countByNecesidad_OidNecesidad(1)).thenReturn(0L, 1L);
        when(asignacionRepository.findByNecesidad_OidNecesidadAndSeleccionado_OidSeleccionado(1, 2)).thenReturn(Optional.empty());
        when(tipoActividadRepository.findByNombreIgnoreCase("DOCENCIA")).thenReturn(Optional.of(tipoActividad));
        when(estadoActividadRepository.findById(3)).thenReturn(Optional.of(estadoActividad));
        when(actividadRepository.save(notNull(Actividad.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Asignacion asignacionGuardada = new Asignacion();
        asignacionGuardada.setNecesidad(necesidad);
        asignacionGuardada.setSeleccionado(seleccionado);
        asignacionGuardada.setActividad(new Actividad());

        when(asignacionRepository.save(notNull(Asignacion.class))).thenAnswer(invocation -> {
            Asignacion asignacion = invocation.getArgument(0, Asignacion.class);
            asignacion.setOidAsignacion(81);
            asignacion.setHorasDocencia(15f);
            return asignacion;
        });
        when(asignacionRepository.findByNecesidad_OidNecesidad(1)).thenReturn(Collections.singletonList(asignacionGuardada));
        when(asignacionRepository.findBySeleccionado_OidSeleccionado(2)).thenReturn(Collections.singletonList(asignacionGuardada));

        assertThrows(ValidacionNegocioException.class, () -> asignacionService.crear(request));
    }

    @Test
    void crear_Catedra_NoDebeAsignarHorasPreparacion() {
        AsignacionDTORequest request = buildRequest();
        seleccionado.setTipo(ContratacionEnum.CATEDRA);
        necesidad.getCalendario().setSemanasPreparacion(null);

        TipoActividad tipoActividad = new TipoActividad();
        EstadoActividad estadoActividad = new EstadoActividad();

        when(necesidadRepository.findById(1)).thenReturn(Optional.of(necesidad));
        when(seleccionadoRepository.findById(2)).thenReturn(Optional.of(seleccionado));
        when(asignacionRepository.countByNecesidad_OidNecesidad(1)).thenReturn(0L, 1L);
        when(asignacionRepository.findByNecesidad_OidNecesidadAndSeleccionado_OidSeleccionado(1, 2)).thenReturn(Optional.empty());
        when(tipoActividadRepository.findByNombreIgnoreCase("DOCENCIA")).thenReturn(Optional.of(tipoActividad));
        when(estadoActividadRepository.findById(3)).thenReturn(Optional.of(estadoActividad));
        when(actividadRepository.save(notNull(Actividad.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Asignacion asignacionGuardada = new Asignacion();
        asignacionGuardada.setNecesidad(necesidad);
        asignacionGuardada.setSeleccionado(seleccionado);
        asignacionGuardada.setActividad(new Actividad());
        when(asignacionRepository.save(notNull(Asignacion.class))).thenAnswer(invocation -> {
            Asignacion asignacion = invocation.getArgument(0, Asignacion.class);
            asignacion.setOidAsignacion(70);
            return asignacion;
        });
        when(asignacionRepository.findByNecesidad_OidNecesidad(1)).thenReturn(Collections.singletonList(asignacionGuardada));
        when(asignacionRepository.findBySeleccionado_OidSeleccionado(2)).thenReturn(Collections.singletonList(asignacionGuardada));

        ApiResponse<AsignacionDTOResponse> response = asignacionService.crear(request);

        assertThat(response.getCodigo()).isEqualTo(201);
        assertThat(asignacionGuardada.getHorasPreparacion()).isZero();
        assertThat(asignacionGuardada.getSemanasPreparacion()).isZero();
    }

    @Test
    void crear_DeberiaFallarCuandoSuperaLimiteDocentes() {
        AsignacionDTORequest request = buildRequest();

        when(necesidadRepository.findById(1)).thenReturn(Optional.of(necesidad));
        when(seleccionadoRepository.findById(2)).thenReturn(Optional.of(seleccionado));
        when(asignacionRepository.countByNecesidad_OidNecesidad(1)).thenReturn(3L);

        assertThrows(AsignacionLimiteDocentesException.class, () -> asignacionService.crear(request));
        verify(asignacionRepository, never()).save(any());
    }

    @Test
    void actualizar_NoDebePermitirCambiosDeNecesidadONuevoSeleccionado() {
        Asignacion asignacion = new Asignacion();
        asignacion.setOidAsignacion(5);
        asignacion.setNecesidad(necesidad);
        asignacion.setSeleccionado(seleccionado);

        when(asignacionRepository.findById(5)).thenReturn(Optional.of(asignacion));

        AsignacionDTORequest request = buildRequest();
        request.setOidNecesidad(99);

        assertThrows(AsignacionOperacionNoPermitidaException.class, () -> asignacionService.actualizar(5, request));
        verify(asignacionRepository, never()).save(any());
    }

    @Test
    void eliminar_DeberiaRedistribuirCuandoQuedanDocentes() {
        Asignacion asignacion = new Asignacion();
        asignacion.setOidAsignacion(9);
        asignacion.setNecesidad(necesidad);
        asignacion.setSeleccionado(seleccionado);
        Actividad actividad = new Actividad();
        actividad.setNombreActividad("Actividad");
        asignacion.setActividad(actividad);

        Asignacion remanente = new Asignacion();
        remanente.setNecesidad(necesidad);
        remanente.setSeleccionado(seleccionado);
        Actividad remAct = new Actividad();
        remAct.setNombreActividad("Remanente");
        remanente.setActividad(remAct);

        necesidad.setEstado(EstadoNecesidad.ASIGNADA);

        when(asignacionRepository.findById(9)).thenReturn(Optional.of(asignacion));
        when(asignacionRepository.findByNecesidad_OidNecesidad(1)).thenReturn(Collections.singletonList(remanente));
        when(asignacionRepository.countByNecesidad_OidNecesidad(1)).thenReturn(1L);

        ApiResponse<Void> response = asignacionService.eliminar(9);

        assertThat(response.getCodigo()).isEqualTo(204);
        verify(eavAtributoService).eliminarAtributosActividad(actividad);
        verify(asignacionRepository).delete(asignacion);
        verify(asignacionRepository).saveAll(ArgumentMatchers.<Iterable<Asignacion>>any());
        assertThat(necesidad.getEstado()).isEqualTo(EstadoNecesidad.ASIGNADA);
        verify(necesidadRepository, never()).save(necesidad);
    }

    @Test
    void eliminar_DebeMarcarNecesidadComoNoAsignadaCuandoNoQuedanDocentes() {
        Asignacion asignacion = new Asignacion();
        asignacion.setOidAsignacion(10);
        asignacion.setNecesidad(necesidad);
        asignacion.setSeleccionado(seleccionado);
        necesidad.setEstado(EstadoNecesidad.ASIGNADA);

        when(asignacionRepository.findById(10)).thenReturn(Optional.of(asignacion));
        when(asignacionRepository.findByNecesidad_OidNecesidad(1)).thenReturn(Collections.emptyList());
        when(asignacionRepository.countByNecesidad_OidNecesidad(1)).thenReturn(0L);

        ApiResponse<Void> response = asignacionService.eliminar(10);

        assertThat(response.getCodigo()).isEqualTo(204);
        verify(eavAtributoService).eliminarAtributosActividad(asignacion.getActividad());
        verify(asignacionRepository).delete(asignacion);
        verify(asignacionRepository, never()).saveAll(ArgumentMatchers.<Iterable<Asignacion>>any());
        assertThat(necesidad.getEstado()).isEqualTo(EstadoNecesidad.NO_ASIGNADA);
        verify(necesidadRepository).save(necesidad);
    }

    @Test
    void validarSeleccionadoCalendario_DeberiaLanzarCuandoCalendariosDiferentes() {
        Calendario otroCalendario = new Calendario();
        otroCalendario.setOidcalendario(99);

        Seleccionado otroSeleccionado = new Seleccionado();
        otroSeleccionado.setOidSeleccionado(2);
        otroSeleccionado.setCalendario(otroCalendario);

        when(necesidadRepository.findById(1)).thenReturn(Optional.of(necesidad));
        when(seleccionadoRepository.findById(2)).thenReturn(Optional.of(otroSeleccionado));

        assertThrows(AsignacionCalendarioInvalidoException.class,
                () -> asignacionService.validarSeleccionadoCalendario(1, 2));
    }

    @Test
    void validarSeleccionadoCalendario_DeberiaLanzarCuandoFaltanIds() {
        assertThrows(ValidacionNegocioException.class,
                () -> asignacionService.validarSeleccionadoCalendario(null, 2));
        assertThrows(ValidacionNegocioException.class,
                () -> asignacionService.validarSeleccionadoCalendario(1, null));
    }

    @Test
    void validarSeleccionadoCalendario_DeberiaPermitirCalendariosIguales() {
        when(necesidadRepository.findById(1)).thenReturn(Optional.of(necesidad));
        when(seleccionadoRepository.findById(2)).thenReturn(Optional.of(seleccionado));

        assertDoesNotThrow(() -> asignacionService.validarSeleccionadoCalendario(1, 2));
        verify(necesidadRepository).findById(1);
        verify(seleccionadoRepository).findById(2);
    }

    private Asignacion buildAsignacion(int id, int oidSeleccionado) {
        Asignacion asignacion = new Asignacion();
        asignacion.setOidAsignacion(id);
        asignacion.setNecesidad(necesidad);
        Seleccionado seleccionadoAsignacion = new Seleccionado();
        seleccionadoAsignacion.setOidSeleccionado(oidSeleccionado);
        seleccionadoAsignacion.setCalendario(necesidad.getCalendario());
        asignacion.setSeleccionado(seleccionadoAsignacion);

        Actividad actividad = new Actividad();
        actividad.setNombreActividad("Actividad " + id);
        asignacion.setActividad(actividad);
        return asignacion;
    }

    private AsignacionDTORequest buildRequest() {
        AsignacionDTORequest request = new AsignacionDTORequest();
        request.setOidNecesidad(1);
        request.setOidSeleccionado(2);
        request.setOidEstadoActividad(3);
        return request;
    }
}
