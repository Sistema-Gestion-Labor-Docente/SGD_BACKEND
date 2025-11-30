package co.edu.unicauca.sgd.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import co.edu.unicauca.sgd.api.domain.NombreFecha;

@DataJpaTest
class NombreFechaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NombreFechaRepository repository;

    @Test
    void findByNombreContainingIgnoreCase_filtraPorNombre() {
        persistNombreFecha("Periodo Académico");
        persistNombreFecha("Otro");

        Page<NombreFecha> page = repository.findByNombreContainingIgnoreCase("periodo", PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getNombre()).containsIgnoringCase("Periodo");
    }

    @Test
    void findByNombreContainingIgnoreCaseAndOidNombreFechaNotIn_excluyeIds() {
        NombreFecha nf1 = persistNombreFecha("Periodo 1");
        persistNombreFecha("Periodo 2");

        Page<NombreFecha> page = repository.findByNombreContainingIgnoreCaseAndOidNombreFechaNotIn(
                "Periodo", List.of(nf1.getOidNombreFecha()), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getOidNombreFecha()).isNotEqualTo(nf1.getOidNombreFecha());
    }

    @Test
    void findByOidNombreFechaNotIn_excluyeListaIds() {
        NombreFecha nf1 = persistNombreFecha("N1");
        NombreFecha nf2 = persistNombreFecha("N2");

        Page<NombreFecha> page = repository.findByOidNombreFechaNotIn(
                List.of(nf1.getOidNombreFecha()), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getOidNombreFecha()).isEqualTo(nf2.getOidNombreFecha());
    }

    private NombreFecha persistNombreFecha(String nombre) {
        NombreFecha nf = new NombreFecha();
        nf.setNombre(nombre);
        nf.setUsuarioCreacion("test");
        return entityManager.persistFlushFind(nf);
    }
}
