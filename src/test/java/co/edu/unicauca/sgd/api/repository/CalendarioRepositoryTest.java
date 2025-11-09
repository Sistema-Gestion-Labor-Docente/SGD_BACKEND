package co.edu.unicauca.sgd.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;

import co.edu.unicauca.sgd.api.domain.Calendario;

@DataJpaTest
class CalendarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CalendarioRepository calendarioRepository;

    @Test
    void saveAndFindById_persistsCalendario() {
        Calendario calendario = new Calendario();
        calendario.setAnioCalendario("2024");
        calendario.setNumeroCalendario(1);
        calendario.setEstado("ACTIVO");
        calendario.setUsuarioCreacion("test");

        Calendario persisted = entityManager.persistFlushFind(calendario);

        assertThat(persisted.getOidcalendario()).isNotNull();
        assertThat(calendarioRepository.findById(persisted.getOidcalendario())).isPresent();
    }

    @Test
    void findAllWithSpecification_filtersByAnio() {
        entityManager.persist(buildCalendario("2024", 1, "ACTIVO"));
        entityManager.persist(buildCalendario("2025", 2, "PENDIENTE"));

        Specification<Calendario> spec = (root, query, cb) ->
                cb.equal(root.get("anioCalendario"), "2024");

        List<Calendario> resultados = calendarioRepository.findAll(spec);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getAnioCalendario()).isEqualTo("2024");
    }

    private Calendario buildCalendario(String anio, Integer numero, String estado) {
        Calendario calendario = new Calendario();
        calendario.setAnioCalendario(anio);
        calendario.setNumeroCalendario(numero);
        calendario.setEstado(estado);
        calendario.setUsuarioCreacion("tester");
        return calendario;
    }
}
