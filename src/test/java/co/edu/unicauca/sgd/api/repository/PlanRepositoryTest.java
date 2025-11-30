package co.edu.unicauca.sgd.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import co.edu.unicauca.sgd.api.domain.Plan;
import co.edu.unicauca.sgd.api.domain.Programa;

@DataJpaTest
class PlanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlanRepository repository;

    @Test
    void saveAndFindById_persistePlan() {
        Programa programa = new Programa();
        programa.setNombre("Programa");
        programa.setNombreCorto("PROG");
        programa.setUsuarioCreacion("test");
        programa = entityManager.persistFlushFind(programa);

        Plan plan = new Plan();
        plan.setNumero("1");
        plan.setEstado("ACTIVO");
        plan.setPrograma(programa);
        plan.setUsuarioCreacion("test");

        Plan persisted = entityManager.persistFlushFind(plan);

        assertThat(persisted.getOidPlan()).isNotNull();
        assertThat(repository.findById(persisted.getOidPlan())).isPresent();
    }
}
