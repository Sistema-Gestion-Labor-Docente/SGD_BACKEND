package co.edu.unicauca.sgd.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import co.edu.unicauca.sgd.api.domain.Asignacion;
import co.edu.unicauca.sgd.api.domain.Necesidad;
import co.edu.unicauca.sgd.api.domain.Seleccionado;
import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.EstadoActividad;
import co.edu.unicauca.sgd.api.domain.Materia;
import co.edu.unicauca.sgd.api.domain.Plan;
import co.edu.unicauca.sgd.api.domain.Programa;
import co.edu.unicauca.sgd.api.domain.TipoActividad;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioDetalle;

@DataJpaTest
class AsignacionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AsignacionRepository repository;

    @Test
    void findByNecesidad_OidNecesidad_devuelveAsignaciones() {
        Necesidad necesidad = persistNecesidad();
        Seleccionado seleccionado1 = persistSeleccionado();
        Seleccionado seleccionado2 = persistSeleccionado();
        Asignacion a1 = persistAsignacion(necesidad, seleccionado1);
        Asignacion a2 = persistAsignacion(necesidad, seleccionado2);

        List<Asignacion> resultado = repository.findByNecesidad_OidNecesidad(necesidad.getOidNecesidad());

        assertThat(resultado).extracting(Asignacion::getOidAsignacion)
                .containsExactlyInAnyOrder(a1.getOidAsignacion(), a2.getOidAsignacion());
    }

    @Test
    void countByNecesidad_OidNecesidad_cuentaAsignaciones() {
        Necesidad necesidad = persistNecesidad();
        Seleccionado seleccionado1 = persistSeleccionado();
        Seleccionado seleccionado2 = persistSeleccionado();
        persistAsignacion(necesidad, seleccionado1);
        persistAsignacion(necesidad, seleccionado2);

        long total = repository.countByNecesidad_OidNecesidad(necesidad.getOidNecesidad());

        assertThat(total).isEqualTo(2L);
    }

    @Test
    void findByNecesidadAndSeleccionado_encuentraUnaAsignacion() {
        Necesidad necesidad = persistNecesidad();
        Seleccionado seleccionado = persistSeleccionado();
        Asignacion asignacion = persistAsignacion(necesidad, seleccionado);

        var opt = repository.findByNecesidad_OidNecesidadAndSeleccionado_OidSeleccionado(
                necesidad.getOidNecesidad(), seleccionado.getOidSeleccionado());

        assertThat(opt).isPresent();
        assertThat(opt.get().getOidAsignacion()).isEqualTo(asignacion.getOidAsignacion());
    }

    @Test
    void findBySeleccionado_OidSeleccionado_devuelveAsignaciones() {
        Seleccionado seleccionado = persistSeleccionado();
        Necesidad necesidad = persistNecesidad();
        Asignacion asignacion = persistAsignacion(necesidad, seleccionado);

        List<Asignacion> resultado = repository.findBySeleccionado_OidSeleccionado(seleccionado.getOidSeleccionado());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getOidAsignacion()).isEqualTo(asignacion.getOidAsignacion());
    }

    private Necesidad persistNecesidad() {
        Calendario calendario = persistCalendario();
        Programa programa = persistPrograma();
        Plan plan = persistPlan(programa);
        Materia materia = persistMateria(plan);

        Necesidad necesidad = new Necesidad();
        necesidad.setCalendario(calendario);
        necesidad.setMateria(materia);
        necesidad.setGrupo("G1");
        necesidad.setCupo(10);
        necesidad.setEstado(co.edu.unicauca.sgd.api.enums.EstadoNecesidad.BORRADOR);
        necesidad.setUsuarioCreacion("test");
        return entityManager.persistFlushFind(necesidad);
    }

    private Seleccionado persistSeleccionado() {
        Calendario calendario = persistCalendario();
        UsuarioDetalle detalle = persistUsuarioDetalle();
        Usuario usuario = persistUsuario(detalle);

        Seleccionado seleccionado = new Seleccionado();
        seleccionado.setCalendario(calendario);
        seleccionado.setUsuario(usuario);
        seleccionado.setDedicacion("TIEMPO COMPLETO");
        seleccionado.setUsuarioCreacion("test");
        return entityManager.persistFlushFind(seleccionado);
    }

    private Asignacion persistAsignacion(Necesidad necesidad, Seleccionado seleccionado) {
        TipoActividad tipo = persistTipoActividad();
        EstadoActividad estadoActividad = persistEstadoActividad();
        Actividad actividad = new Actividad();
        actividad.setTipoActividad(tipo);
        actividad.setEstadoActividad(estadoActividad);
        actividad.setNombreActividad("Actividad");
        actividad.setSemanas(1f);
        actividad = entityManager.persistFlushFind(actividad);

        Asignacion asignacion = new Asignacion();
        asignacion.setNecesidad(necesidad);
        asignacion.setSeleccionado(seleccionado);
        asignacion.setActividad(actividad);
        asignacion.setUsuarioCreacion("test");
        return entityManager.persistFlushFind(asignacion);
    }

    private Calendario persistCalendario() {
        Calendario cal = new Calendario();
        cal.setAnioCalendario("2024");
        cal.setNumeroCalendario(1);
        cal.setEstado("ACTIVO");
        cal.setUsuarioCreacion("test");
        return entityManager.persistFlushFind(cal);
    }

    private Programa persistPrograma() {
        Programa programa = new Programa();
        programa.setNombre("Programa");
        programa.setNombreCorto("PROG");
        programa.setUsuarioCreacion("test");
        return entityManager.persistFlushFind(programa);
    }

    private Plan persistPlan(Programa programa) {
        Plan plan = new Plan();
        plan.setNumero("1");
        plan.setEstado("ACTIVO");
        plan.setPrograma(programa);
        plan.setUsuarioCreacion("test");
        return entityManager.persistFlushFind(plan);
    }

    private Materia persistMateria(Plan plan) {
        Materia materia = new Materia();
        materia.setOidMateria("OID1");
        materia.setCodigo("COD1");
        materia.setNombre("Materia");
        materia.setSemestre(1);
        materia.setPlan(plan);
        materia.setUsuarioCreacion("test");
        return entityManager.persistFlushFind(materia);
    }

    private UsuarioDetalle persistUsuarioDetalle() {
        UsuarioDetalle detalle = new UsuarioDetalle();
        detalle.setFacultad("FAC");
        return entityManager.persistFlushFind(detalle);
    }

    private Usuario persistUsuario(UsuarioDetalle detalle) {
        Usuario usuario = new Usuario();
        usuario.setUsuarioDetalle(detalle);
        usuario.setIdentificacion("1000");
        usuario.setNombres("Nombre");
        usuario.setApellidos("Apellido");
        usuario.setCorreo("correo@test.com");
        return entityManager.persistFlushFind(usuario);
    }

    private TipoActividad persistTipoActividad() {
        TipoActividad tipo = new TipoActividad();
        tipo.setNombre("DOCENCIA");
        tipo.setDescripcion("DOCENCIA");
        return entityManager.persistFlushFind(tipo);
    }

    private EstadoActividad persistEstadoActividad() {
        EstadoActividad estado = new EstadoActividad();
        estado.setNombre("ACTIVO");
        return entityManager.persistFlushFind(estado);
    }
}
