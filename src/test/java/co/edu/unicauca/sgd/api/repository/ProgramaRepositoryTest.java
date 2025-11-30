package co.edu.unicauca.sgd.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import co.edu.unicauca.sgd.api.domain.Programa;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioDetalle;

@DataJpaTest
class ProgramaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProgramaRepository repository;

    @Test
    void findByCoordinador_OidUsuario_encuentraPrograma() {
        UsuarioDetalle detalle = new UsuarioDetalle();
        detalle.setFacultad("FAC");
        detalle = entityManager.persistFlushFind(detalle);

        Usuario coordinador = new Usuario();
        coordinador.setUsuarioDetalle(detalle);
        coordinador.setIdentificacion("100");
        coordinador.setNombres("Nombre");
        coordinador.setApellidos("Apellido");
        coordinador.setCorreo("correo@test.com");
        coordinador = entityManager.persistFlushFind(coordinador);

        Programa programa = new Programa();
        programa.setNombre("Programa");
        programa.setNombreCorto("PROG");
        programa.setUsuarioCreacion("test");
        programa.setCoordinador(coordinador);
        programa = entityManager.persistFlushFind(programa);

        Optional<Programa> result = repository.findByCoordinador_OidUsuario(coordinador.getOidUsuario());

        assertThat(result).isPresent();
        assertThat(result.get().getOidPrograma()).isEqualTo(programa.getOidPrograma());
    }
}
