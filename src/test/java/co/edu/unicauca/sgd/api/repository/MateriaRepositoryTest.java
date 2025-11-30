package co.edu.unicauca.sgd.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.Materia;
import co.edu.unicauca.sgd.api.domain.Plan;
import co.edu.unicauca.sgd.api.domain.Programa;

@DataJpaTest
class MateriaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MateriaRepository repository;

    @Test
    void findAllByPlanOidPlan_filtraPorPlan() {
        Plan plan1 = persistPlan(1);
        Plan plan2 = persistPlan(2);

        persistMateria("M1", plan1, null);
        persistMateria("M2", plan2, null);

        List<Materia> materias = repository.findAllByPlanOidPlan(plan1.getOidPlan());

        assertThat(materias).hasSize(1);
        assertThat(materias.get(0).getPlan().getOidPlan()).isEqualTo(plan1.getOidPlan());
    }

    @Test
    void findFirstByOidMateriaOrCodigoOrNombre_encuentraPorCualquiera() {
        Plan plan = persistPlan(1);
        Materia materia = persistMateria("OID1", plan, null);
        materia.setCodigo("COD1");
        materia.setNombre("NOMBRE1");
        materia = entityManager.persistFlushFind(materia);

        Optional<Materia> porCodigo = repository.findFirstByOidMateriaOrCodigoOrNombre(null, "COD1", null);
        assertThat(porCodigo).isPresent();
        assertThat(porCodigo.get().getOidMateria()).isEqualTo("OID1");
    }

    @Test
    void findMateriasSinCorrequisitoNiReferencias_excluyeMateriasConCorrequisito() {
        Plan plan = persistPlan(1);
        Departamento departamento = persistDepartamento();

        // Materia libre: no tiene correquisito ni es correquisito de otra
        Materia libre = persistMateria("LIBRE", plan, departamento);

        // Materia base: no tiene correquisito, pero será correquisito de otra
        Materia base = persistMateria("BASE", plan, departamento);

        // Materia que usa a base como correquisito
        Materia conCorrequisito = persistMateria("CON", plan, departamento);
        conCorrequisito.setCorrequisito(base);
        conCorrequisito = entityManager.persistFlushFind(conCorrequisito);

        Page<Materia> page = repository.findMateriasSinCorrequisitoNiReferencias(
                departamento.getOidDepartamento(), plan.getOidPlan(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getOidMateria()).isEqualTo("LIBRE");
    }

    @Test
    void existsByCorrequisito_retornarTrueSiAlgunaMateriaUsaCorrequisito() {
        Plan plan = persistPlan(1);
        Materia base = persistMateria("BASE", plan, null);
        Materia otra = persistMateria("OTRA", plan, null);
        otra.setCorrequisito(base);
        otra = entityManager.persistFlushFind(otra);

        boolean exists = repository.existsByCorrequisito(base);

        assertThat(exists).isTrue();
    }

    @Test
    void countByPlanOidPlan_cuentaMateriasPorPlan() {
        Plan plan = persistPlan(1);
        persistMateria("M1", plan, null);
        persistMateria("M2", plan, null);

        long total = repository.countByPlanOidPlan(plan.getOidPlan());

        assertThat(total).isEqualTo(2L);
    }

    private Plan persistPlan(int numero) {
        Programa programa = new Programa();
        programa.setNombre("Programa " + numero);
        programa.setNombreCorto("PR" + numero);
        programa.setUsuarioCreacion("test");
        programa = entityManager.persistFlushFind(programa);

        Plan plan = new Plan();
        plan.setNumero(String.valueOf(numero));
        plan.setEstado("ACTIVO");
        plan.setPrograma(programa);
        plan.setUsuarioCreacion("test");
        return entityManager.persistFlushFind(plan);
    }

    private Departamento persistDepartamento() {
        Departamento d = new Departamento();
        d.setNombre("DEPTO");
        d.setFacultad("FACULTAD");
        d.setUsuarioCreacion("test");
        return entityManager.persistFlushFind(d);
    }

    private Materia persistMateria(String oidMateria, Plan plan, Departamento departamento) {
        Materia m = new Materia();
        m.setOidMateria(oidMateria);
        m.setCodigo(oidMateria);
        m.setNombre("Materia " + oidMateria);
        m.setSemestre(1);
        m.setPlan(plan);
        m.setDepartamento(departamento);
        m.setUsuarioCreacion("test");
        return entityManager.persistFlushFind(m);
    }
}
