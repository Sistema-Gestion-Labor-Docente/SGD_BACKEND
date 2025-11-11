package co.edu.unicauca.sgd.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Materia;
import co.edu.unicauca.sgd.api.domain.Necesidad;
import co.edu.unicauca.sgd.api.domain.Plan;
import co.edu.unicauca.sgd.api.domain.Programa;
import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;

@DataJpaTest
class NecesidadRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NecesidadRepository necesidadRepository;

    @Test
    void existsByCalendarioMateriaGrupo_returnsTrueWhenRecordExists() {
        Calendario calendario = persistCalendario("2024", 1);
        Materia materia = persistMateria("MAT101", "Matemáticas", calendario);
        persistNecesidad(calendario, materia, "A");

        boolean exists = necesidadRepository
                .existsByCalendario_OidcalendarioAndMateria_IdMateriaAndGrupo(
                        calendario.getOidcalendario(),
                        materia.getIdMateria(),
                        "A");

        assertThat(exists).isTrue();
    }

    @Test
    void findAllByCalendarioAndMateria_returnsAllGroupsForMateria() {
        Calendario calendario = persistCalendario("2025", 2);
        Materia materia = persistMateria("MAT202", "Programación", calendario);
        persistNecesidad(calendario, materia, "A");
        persistNecesidad(calendario, materia, "B");

        List<Necesidad> resultados = necesidadRepository
                .findAllByCalendario_OidcalendarioAndMateria_IdMateria(
                        calendario.getOidcalendario(),
                        materia.getIdMateria());

        assertThat(resultados).hasSize(2);
        assertThat(resultados)
                .extracting(Necesidad::getGrupo)
                .containsExactlyInAnyOrder("A", "B");
    }

    private Calendario persistCalendario(String anio, int numero) {
        Calendario calendario = new Calendario();
        calendario.setAnioCalendario(anio);
        calendario.setNumeroCalendario(numero);
        calendario.setEstado("ACTIVO");
        calendario.setUsuarioCreacion("tester");
        return entityManager.persistAndFlush(calendario);
    }

    private Materia persistMateria(String codigo, String nombre, Calendario calendario) {
        Programa programa = new Programa();
        programa.setNombre("Programa " + codigo);
        programa.setNombreCorto("PRG" + codigo);
        programa.setUsuarioCreacion("tester");
        programa = entityManager.persist(programa);

        Plan plan = new Plan();
        plan.setNumero("PLAN-" + codigo);
        plan.setEstado("ACTIVO");
        plan.setUsuarioCreacion("tester");
        plan.setPrograma(programa);
        plan = entityManager.persist(plan);

        Materia materia = new Materia();
        materia.setCodigo(codigo);
        materia.setNombre(nombre);
        materia.setSemestre(1);
        materia.setOidMateria("OID-" + codigo);
        materia.setPlan(plan);
        materia.setUsuarioCreacion("tester");
        return entityManager.persistAndFlush(materia);
    }

    private void persistNecesidad(Calendario calendario, Materia materia, String grupo) {
        Necesidad necesidad = new Necesidad();
        necesidad.setCalendario(calendario);
        necesidad.setMateria(materia);
        necesidad.setGrupo(grupo);
        necesidad.setCupo(30);
        necesidad.setEstado(EstadoNecesidad.BORRADOR);
        necesidad.setUsuarioCreacion("tester");
        entityManager.persistAndFlush(necesidad);
    }
}
