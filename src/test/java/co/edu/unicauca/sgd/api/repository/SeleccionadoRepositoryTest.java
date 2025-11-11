package co.edu.unicauca.sgd.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.Seleccionado;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioDepartamento;
import co.edu.unicauca.sgd.api.domain.UsuarioDetalle;

@DataJpaTest
class SeleccionadoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SeleccionadoRepository seleccionadoRepository;

    @Test
    void findByCalendarioOidcalendario_returnsOnlyMatches() {
        Calendario calendarioPrincipal = persistCalendario("2024", 1);
        Calendario calendarioSecundario = persistCalendario("2025", 1);

        Usuario usuarioCalendario = persistUsuario("111");
        persistSeleccionado(calendarioPrincipal, usuarioCalendario);
        persistSeleccionado(calendarioSecundario, persistUsuario("222"));

        List<Seleccionado> resultados =
                seleccionadoRepository.findByCalendarioOidcalendario(calendarioPrincipal.getOidcalendario());

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getUsuario().getIdentificacion()).isEqualTo("111");
    }

    @Test
    void existsByCalendarioAndUsuario_returnsTrueWhenEntryExists() {
        Calendario calendario = persistCalendario("2026", 2);
        Usuario usuario = persistUsuario("333");
        persistSeleccionado(calendario, usuario);

        boolean existe = seleccionadoRepository.existsByCalendarioOidcalendarioAndUsuarioOidUsuario(
                calendario.getOidcalendario(), usuario.getOidUsuario());

        assertThat(existe).isTrue();
    }

    @Test
    void findByCalendarioAndDepartamento_filtersByDepartamentoAsociado() {
        Calendario calendario = persistCalendario("2027", 1);
        Departamento deptoDocencia = persistDepartamento("Docencia");
        Departamento deptoOtro = persistDepartamento("Investigacion");

        Usuario usuarioDocente = persistUsuario("444");
        Usuario usuarioOtro = persistUsuario("555");

        persistRelacionDepartamento(usuarioDocente, deptoDocencia);
        persistRelacionDepartamento(usuarioOtro, deptoOtro);

        persistSeleccionado(calendario, usuarioDocente);
        persistSeleccionado(calendario, usuarioOtro);

        List<Seleccionado> resultados = seleccionadoRepository.findByCalendarioAndDepartamento(
                calendario.getOidcalendario(),
                deptoDocencia.getOidDepartamento());

        assertThat(resultados)
                .hasSize(1)
                .first()
                .extracting(sel -> sel.getUsuario().getIdentificacion())
                .isEqualTo("444");
    }

    private Calendario persistCalendario(String anio, int numero) {
        Calendario calendario = new Calendario();
        calendario.setAnioCalendario(anio);
        calendario.setNumeroCalendario(numero);
        calendario.setEstado("ACTIVO");
        calendario.setUsuarioCreacion("tester");
        return entityManager.persistAndFlush(calendario);
    }

    private Departamento persistDepartamento(String nombre) {
        Departamento departamento = new Departamento();
        departamento.setNombre(nombre);
        departamento.setFacultad("Facultad " + nombre);
        departamento.setUsuarioCreacion("tester");
        return entityManager.persistAndFlush(departamento);
    }

    private Usuario persistUsuario(String identificacion) {
        UsuarioDetalle detalle = new UsuarioDetalle();
        detalle.setFacultad("Facultad");
        detalle.setDepartamento("Departamento");
        detalle.setPrograma("Programa");
        detalle.setCategoria("Categoria");
        detalle.setContratacion("PLANTA");
        detalle.setDedicacion("Tiempo completo");
        detalle.setEstudios("Doctorado");
        entityManager.persist(detalle);

        Usuario usuario = new Usuario();
        usuario.setUsuarioDetalle(detalle);
        usuario.setIdentificacion(identificacion);
        usuario.setNombres("Nombre " + identificacion);
        usuario.setApellidos("Apellido " + identificacion);
        usuario.setCorreo(identificacion + "@mail.com");
        return entityManager.persistAndFlush(usuario);
    }

    private void persistRelacionDepartamento(Usuario usuario, Departamento departamento) {
        UsuarioDepartamento relacion = new UsuarioDepartamento();
        relacion.setOidUsuario(usuario.getOidUsuario());
        relacion.setUsuario(usuario);
        relacion.setDepartamento(departamento);
        entityManager.persistAndFlush(relacion);
    }

    private void persistSeleccionado(Calendario calendario, Usuario usuario) {
        Seleccionado seleccionado = new Seleccionado();
        seleccionado.setCalendario(calendario);
        seleccionado.setUsuario(usuario);
        seleccionado.setDedicacion("Tiempo completo");
        seleccionado.setUsuarioCreacion("tester");
        entityManager.persistAndFlush(seleccionado);
    }
}
