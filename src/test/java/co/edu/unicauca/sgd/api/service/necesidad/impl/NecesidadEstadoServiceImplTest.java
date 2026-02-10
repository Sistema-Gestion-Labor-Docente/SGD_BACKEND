package co.edu.unicauca.sgd.api.service.necesidad.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import co.edu.unicauca.sgd.api.client.ClienteNotificacion;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.Materia;
import co.edu.unicauca.sgd.api.domain.Necesidad;
import co.edu.unicauca.sgd.api.domain.Plan;
import co.edu.unicauca.sgd.api.domain.Programa;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.NecesidadRepository;
import co.edu.unicauca.sgd.api.repository.AsignacionRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class NecesidadEstadoServiceImplTest {

    @Mock
    private NecesidadRepository necesidadRepository;

    @Mock
    private CalendarioRepository calendarioRepository;

    @Mock
    private ClienteNotificacion clienteNotificacion;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AsignacionRepository asignacionRepository;

    private NecesidadEstadoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NecesidadEstadoServiceImpl(
                necesidadRepository,
                calendarioRepository,
                clienteNotificacion,
                usuarioRepository,
                asignacionRepository);
        lenient().when(usuarioRepository.findFirstActiveByRolNombre(ArgumentMatchers.anyString())).thenReturn(Optional.empty());
        lenient().when(usuarioRepository.findByCorreo(ArgumentMatchers.anyString())).thenReturn(Optional.empty());
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void cambiarEstadoMasivo_shouldUpdateNecesidadesCuandoParametrosValidos() {
        Calendario calendario = new Calendario();
        calendario.setOidcalendario(1);
        Necesidad necesidad = necesidad(10, EstadoNecesidad.BORRADOR, 2, 3);

        when(calendarioRepository.findById(1)).thenReturn(Optional.of(calendario));
        when(necesidadRepository.findAllByCalendario_OidcalendarioAndEstado(1, EstadoNecesidad.BORRADOR))
                .thenReturn(new java.util.ArrayList<>(List.of(necesidad)));
        when(necesidadRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<Map<String, Object>> response = service.cambiarEstadoMasivo(
                1,
                EstadoNecesidad.BORRADOR,
                EstadoNecesidad.EN_REVISION_SECRETARIO,
                2,
                null,
                null);

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(necesidad.getEstado()).isEqualTo(EstadoNecesidad.EN_REVISION_SECRETARIO);
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().get("totalNecesidades")).isEqualTo(1);
        verify(necesidadRepository).saveAll(List.of(necesidad));
    }

    @Test
    void cambiarEstadoMasivo_enviaCorreoAlSecretarioCuandoTransicionABorrador() {
        Calendario calendario = new Calendario();
        calendario.setOidcalendario(2);
        calendario.setAnioCalendario("2024");
        calendario.setNumeroCalendario(1);

        Necesidad necesidad = necesidad(30, EstadoNecesidad.BORRADOR, 4, 5);

        Usuario secretario = usuario("secretaria@test.com", "Secretaria", "General");
        Usuario actor = usuario("actor@test.com", "Actor", "Prueba");

        when(calendarioRepository.findById(2)).thenReturn(Optional.of(calendario));
        when(necesidadRepository.findAllByCalendario_OidcalendarioAndEstado(2, EstadoNecesidad.BORRADOR))
                .thenReturn(new java.util.ArrayList<>(List.of(necesidad)));
        when(necesidadRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRepository.findFirstActiveByRolNombre("SECRETARIA/O FACULTAD")).thenReturn(Optional.of(secretario));
        when(usuarioRepository.findByCorreo("actor@test.com")).thenReturn(Optional.of(actor));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("actor@test.com", null));

        service.cambiarEstadoMasivo(2, EstadoNecesidad.BORRADOR, EstadoNecesidad.EN_REVISION_SECRETARIO, 4, null, null);

        verify(clienteNotificacion).enviarNotificacion(
                ArgumentMatchers.eq(List.of("secretaria@test.com")),
                ArgumentMatchers.contains("BORRADOR"),
                ArgumentMatchers.contains("Actor"));
    }

    @Test
    void cambiarEstadoMasivo_debeFallarCuandoNoSeProveeProgramaObligatorio() {
        Calendario calendario = new Calendario();
        calendario.setOidcalendario(1);
        when(calendarioRepository.findById(1)).thenReturn(Optional.of(calendario));

        ApiResponse<Map<String, Object>> response = service.cambiarEstadoMasivo(
                1,
                EstadoNecesidad.BORRADOR,
                EstadoNecesidad.EN_REVISION_SECRETARIO,
                null,
                null,
                null);

        assertThat(response.getCodigo()).isEqualTo(400);
        assertThat(response.getMensaje()).contains("programa es obligatorio");
        verify(necesidadRepository, never()).findAllByCalendario_OidcalendarioAndEstado(1, EstadoNecesidad.BORRADOR);
    }

    @Test
    void cambiarEstadoMasivo_debeRetornarNotFoundCuandoNoHayNecesidades() {
        Calendario calendario = new Calendario();
        calendario.setOidcalendario(1);
        when(calendarioRepository.findById(1)).thenReturn(Optional.of(calendario));
        when(necesidadRepository.findAllByCalendario_OidcalendarioAndEstado(1, EstadoNecesidad.EN_REVISION_SECRETARIO))
                .thenReturn(List.of());

        ApiResponse<Map<String, Object>> response = service.cambiarEstadoMasivo(
                1,
                EstadoNecesidad.EN_REVISION_SECRETARIO,
                EstadoNecesidad.EN_REVISION_JEFE,
                null,
                null,
                null);

        assertThat(response.getCodigo()).isEqualTo(404);
        assertThat(response.getMensaje()).contains("No se encontraron necesidades");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().get("totalNecesidades")).isEqualTo(0);
    }

    @Test
    void cambiarEstadoPorOids_debeActualizarEstadosCuandoTodoEsValido() {
        Necesidad necesidad1 = necesidad(11, EstadoNecesidad.EN_REVISION_SECRETARIO, 5, 6);
        Necesidad necesidad2 = necesidad(12, EstadoNecesidad.EN_REVISION_SECRETARIO, 5, 6);
        when(necesidadRepository.findAllById(List.of(11, 12)))
                .thenReturn(new java.util.ArrayList<>(List.of(necesidad1, necesidad2)));
        when(necesidadRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<Map<String, Object>> response = service.cambiarEstadoPorOids(
                List.of(11, 12),
                EstadoNecesidad.EN_REVISION_SECRETARIO,
                EstadoNecesidad.EN_REVISION_JEFE);

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().get("totalNecesidades")).isEqualTo(2);
        assertThat(necesidad1.getEstado()).isEqualTo(EstadoNecesidad.EN_REVISION_JEFE);
        assertThat(necesidad2.getEstado()).isEqualTo(EstadoNecesidad.EN_REVISION_JEFE);
        verify(necesidadRepository).saveAll(List.of(necesidad1, necesidad2));
    }

    @Test
    void cambiarEstadoPorOids_debeResponderConErrorCuandoEstadosActualesNoCoinciden() {
        Necesidad necesidad = necesidad(20, EstadoNecesidad.BORRADOR, 2, 3);
        when(necesidadRepository.findAllById(List.of(20))).thenReturn(List.of(necesidad));

        ApiResponse<Map<String, Object>> response = service.cambiarEstadoPorOids(
                List.of(20),
                EstadoNecesidad.EN_REVISION_SECRETARIO,
                EstadoNecesidad.EN_REVISION_JEFE);

        assertThat(response.getCodigo()).isEqualTo(400);
        assertThat(response.getMensaje()).contains("Solo se pueden actualizar");
        assertThat(response.getData()).isNotNull();
        Map<String, Object> data = response.getData();
        assertThat(data).containsKey("inconsistencias");
        verify(necesidadRepository, never()).saveAll(anyList());
    }

    private Necesidad necesidad(int oid, EstadoNecesidad estado, Integer oidPrograma, Integer oidDepartamento) {
        Necesidad necesidad = new Necesidad();
        necesidad.setOidNecesidad(oid);
        necesidad.setEstado(estado);
        necesidad.setGrupo("A");
        necesidad.setCupo(20);

        Materia materia = new Materia();
        Plan plan = new Plan();
        Programa programa = new Programa();
        programa.setOidPrograma(oidPrograma);
        programa.setNombre("Programa " + (oidPrograma != null ? oidPrograma : ""));
        plan.setPrograma(programa);
        materia.setPlan(plan);

        if (oidDepartamento != null) {
            Departamento departamento = new Departamento();
            departamento.setOidDepartamento(oidDepartamento);
            departamento.setNombre("Departamento " + oidDepartamento);
            materia.setDepartamento(departamento);
        }

        necesidad.setMateria(materia);
        return necesidad;
    }

    private Usuario usuario(String correo, String nombres, String apellidos) {
        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        return usuario;
    }
}
