package co.edu.unicauca.sgd.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioDetalle;

@DataJpaTest
class DepartamentoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DepartamentoRepository repository;

    @Test
    void findByJefe_OidUsuario_encuentraDepartamento() {
        UsuarioDetalle detalle = new UsuarioDetalle();
        detalle.setFacultad("FAC");
        detalle = entityManager.persistFlushFind(detalle);

        Usuario jefe = new Usuario();
        jefe.setUsuarioDetalle(detalle);
        jefe.setIdentificacion("100");
        jefe.setNombres("Nombre");
        jefe.setApellidos("Apellido");
        jefe.setCorreo("correo@test.com");
        jefe = entityManager.persistFlushFind(jefe);

        Departamento departamento = new Departamento();
        departamento.setNombre("Depto");
        departamento.setFacultad("FACULTAD");
        departamento.setUsuarioCreacion("test");
        departamento.setJefe(jefe);
        departamento = entityManager.persistFlushFind(departamento);

        Optional<Departamento> result = repository.findByJefe_OidUsuario(jefe.getOidUsuario());

        assertThat(result).isPresent();
        assertThat(result.get().getOidDepartamento()).isEqualTo(departamento.getOidDepartamento());
    }

    @Test
    void findByNombre_encuentraDepartamento() {
        Departamento departamento = new Departamento();
        departamento.setNombre("DEPTO");
        departamento.setFacultad("FACULTAD");
        departamento.setUsuarioCreacion("test");
        departamento = entityManager.persistFlushFind(departamento);

        Optional<Departamento> result = repository.findByNombre("DEPTO");

        assertThat(result).isPresent();
        assertThat(result.get().getOidDepartamento()).isEqualTo(departamento.getOidDepartamento());
    }
}
