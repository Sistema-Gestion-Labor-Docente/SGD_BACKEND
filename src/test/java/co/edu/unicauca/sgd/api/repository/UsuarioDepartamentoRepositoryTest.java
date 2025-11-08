package co.edu.unicauca.sgd.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.ActividadCalendario;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.EstadoActividad;
import co.edu.unicauca.sgd.api.domain.TipoActividad;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioActividadCalendario;
import co.edu.unicauca.sgd.api.domain.UsuarioDepartamento;
import co.edu.unicauca.sgd.api.domain.UsuarioDetalle;

@DataJpaTest
class UsuarioDepartamentoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsuarioDepartamentoRepository repository;

    private Departamento departamentoDocencia;
    private Departamento departamentoMixto;
    private TipoActividad tipoDocencia;
    private TipoActividad tipoInvestigacion;
    private EstadoActividad estadoAprobado;

    @BeforeEach
    void setUp() {
        departamentoDocencia = persistDepartamento("Departamento Docencia");
        departamentoMixto = persistDepartamento("Departamento Mixto");
        tipoDocencia = persistTipoActividad("DOCENCIA");
        tipoInvestigacion = persistTipoActividad("INVESTIGACION");
        estadoAprobado = persistEstadoActividad("APROBADA");
    }

    @Test
    void findProfesoresConTipoActividad_filtraPorDepartamento() {
        UsuarioDepartamento profesorDocencia = persistProfesorConActividad("111", departamentoDocencia, tipoDocencia);
        persistProfesorConActividad("222", departamentoMixto, tipoDocencia);

        List<UsuarioDepartamento> resultado = repository.findProfesoresConTipoActividad("DOCENCIA",
                departamentoDocencia.getOidDepartamento());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getOidUsuario()).isEqualTo(profesorDocencia.getOidUsuario());
    }

    @Test
    void findProfesoresConTipoActividadDiferente_excluyeDocencia() {
        persistProfesorConActividad("333", departamentoMixto, tipoInvestigacion);
        persistProfesorConActividad("444", departamentoMixto, tipoDocencia);

        List<UsuarioDepartamento> resultado = repository.findProfesoresConTipoActividadDiferente("DOCENCIA",
                departamentoMixto.getOidDepartamento());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getUsuario().getIdentificacion()).isEqualTo("333");
    }

    private Departamento persistDepartamento(String nombre) {
        Departamento departamento = new Departamento();
        departamento.setNombre(nombre);
        departamento.setFacultad("Facultad");
        departamento.setUsuarioCreacion("test");
        return entityManager.persist(departamento);
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

    private UsuarioDepartamento persistProfesorConActividad(String identificacion, Departamento departamento,
            TipoActividad tipoActividad) {
        UsuarioDetalle detalle = new UsuarioDetalle();
        detalle.setFacultad("Facultad");
        detalle.setDepartamento("Depto");
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
        entityManager.persist(usuario);

        UsuarioDepartamento usuarioDepartamento = new UsuarioDepartamento();
        usuarioDepartamento.setOidUsuario(usuario.getOidUsuario());
        usuarioDepartamento.setUsuario(usuario);
        usuarioDepartamento.setDepartamento(departamento);
        entityManager.persist(usuarioDepartamento);

        Actividad actividad = new Actividad();
        actividad.setTipoActividad(tipoActividad);
        actividad.setEstadoActividad(estadoAprobado);
        actividad.setNombreActividad("Actividad " + identificacion);
        actividad.setHoras(4f);
        actividad.setSemanas(1f);
        entityManager.persist(actividad);

        Calendario calendario = new Calendario();
        calendario.setAnioCalendario("2025");
        calendario.setNumeroCalendario(1);
        calendario.setUsuarioCreacion("system");
        calendario.setEstado("ACTIVO");
        entityManager.persist(calendario);

        ActividadCalendario actividadCalendario = new ActividadCalendario();
        actividadCalendario.setActividad(actividad);
        actividadCalendario.setCalendario(calendario);
        actividadCalendario.setUsuarioCreacion("system");
        entityManager.persist(actividadCalendario);

        UsuarioActividadCalendario relacion = new UsuarioActividadCalendario();
        relacion.setUsuario(usuario);
        relacion.setActividadCalendario(actividadCalendario);
        relacion.setUsuarioCreacion("system");
        entityManager.persist(relacion);

        entityManager.flush();
        return usuarioDepartamento;
    }
}
