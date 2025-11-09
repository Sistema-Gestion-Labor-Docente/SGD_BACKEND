package co.edu.unicauca.sgd.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.ActividadCalendario;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.EstadoActividad;
import co.edu.unicauca.sgd.api.domain.TipoActividad;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioActividadCalendario;
import co.edu.unicauca.sgd.api.domain.UsuarioDetalle;
import co.edu.unicauca.sgd.api.repository.projection.UsuarioHorasProjection;

@DataJpaTest
class UsuarioActividadCalendarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsuarioActividadCalendarioRepository repository;

    private TipoActividad tipoActividad;
    private EstadoActividad estadoActividad;
    private Calendario calendario;

    @BeforeEach
    void setUp() {
        tipoActividad = persistTipoActividad("DOCENCIA");
        estadoActividad = persistEstadoActividad("APROBADA");
        calendario = persistCalendario();
    }

    @Test
    void sumarHorasPorUsuarios_agrupaHorasPorCadaUsuario() {
        Usuario usuario1 = persistUsuario("1001");
        Usuario usuario2 = persistUsuario("1002");

        persistRelacion(usuario1, 4f);
        persistRelacion(usuario1, 3f);
        persistRelacion(usuario2, 6f);

        List<UsuarioHorasProjection> resultado = repository
                .sumarHorasPorUsuarios(List.of(usuario1.getOidUsuario(), usuario2.getOidUsuario()));

        Map<Integer, Float> horasPorUsuario = resultado.stream()
                .collect(Collectors.toMap(UsuarioHorasProjection::getOidUsuario, UsuarioHorasProjection::getTotalHoras));

        assertThat(horasPorUsuario.get(usuario1.getOidUsuario())).isEqualTo(7f);
        assertThat(horasPorUsuario.get(usuario2.getOidUsuario())).isEqualTo(6f);
    }

    @Test
    void sumarHorasPorUsuario_devuelveTotalParaUnSoloUsuario() {
        Usuario usuario = persistUsuario("2001");
        persistRelacion(usuario, 5f);
        persistRelacion(usuario, 2f);

        Float total = repository.sumarHorasPorUsuario(usuario.getOidUsuario());

        assertThat(total).isEqualTo(7f);
    }

    @Test
    void sumarHorasPorUsuario_sinRelacionesDevuelveCero() {
        Usuario usuario = persistUsuario("3001");

        Float total = repository.sumarHorasPorUsuario(usuario.getOidUsuario());

        assertThat(total).isEqualTo(0f);
    }

    private Usuario persistUsuario(String identificacion) {
        UsuarioDetalle detalle = new UsuarioDetalle();
        detalle.setFacultad("Facultad");
        detalle.setDepartamento("Departamento");
        detalle.setPrograma("Programa");
        detalle.setCategoria("Categoria");
        detalle.setContratacion("Tiempo completo");
        detalle.setDedicacion("40");
        detalle.setEstudios("Doctorado");
        entityManager.persist(detalle);

        Usuario usuario = new Usuario();
        usuario.setUsuarioDetalle(detalle);
        usuario.setIdentificacion(identificacion);
        usuario.setNombres("Nombre " + identificacion);
        usuario.setApellidos("Apellido " + identificacion);
        usuario.setCorreo(identificacion + "@mail.com");
        return entityManager.persist(usuario);
    }

    private UsuarioActividadCalendario persistRelacion(Usuario usuario, float horas) {
        Actividad actividad = new Actividad();
        actividad.setTipoActividad(tipoActividad);
        actividad.setEstadoActividad(estadoActividad);
        actividad.setNombreActividad("Actividad " + Math.random());
        actividad.setHoras(horas);
        actividad.setSemanas(1f);
        entityManager.persist(actividad);

        ActividadCalendario actividadCalendario = new ActividadCalendario();
        actividadCalendario.setActividad(actividad);
        actividadCalendario.setCalendario(calendario);
        actividadCalendario.setUsuarioCreacion("test");
        entityManager.persist(actividadCalendario);

        UsuarioActividadCalendario relacion = new UsuarioActividadCalendario();
        relacion.setUsuario(usuario);
        relacion.setActividadCalendario(actividadCalendario);
        relacion.setUsuarioCreacion("test");
        return entityManager.persist(relacion);
    }

    private TipoActividad persistTipoActividad(String nombre) {
        TipoActividad tipo = new TipoActividad();
        tipo.setNombre(nombre);
        tipo.setDescripcion(nombre + " desc");
        return entityManager.persist(tipo);
    }

    private EstadoActividad persistEstadoActividad(String nombre) {
        EstadoActividad estado = new EstadoActividad();
        estado.setNombre(nombre);
        return entityManager.persist(estado);
    }

    private Calendario persistCalendario() {
        Calendario cal = new Calendario();
        cal.setAnioCalendario("2024");
        cal.setNumeroCalendario(1);
        cal.setEstado("ACTIVO");
        cal.setUsuarioCreacion("test");
        return entityManager.persist(cal);
    }
}
