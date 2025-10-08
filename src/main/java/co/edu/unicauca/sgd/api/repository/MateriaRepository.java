package co.edu.unicauca.sgd.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import co.edu.unicauca.sgd.api.domain.Materia;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, Integer>, JpaSpecificationExecutor<Materia> {

    List<Materia> findAllByPlanOidPlan(Integer oidPlan);

    Optional<Materia> findFirstByOidMateriaOrCodigoOrNombre(String oidMateria, String codigo, String nombre);

    Optional<Materia> findFirstByOidMateriaIgnoreCaseOrCodigoIgnoreCaseOrNombreIgnoreCase(String oidMateria, String codigo, String nombre);
    
    Optional<Materia> findByOidMateria(String oidMateria);

    @Query(
        value = """
            SELECT m
            FROM Materia m
            WHERE m.correquisito IS NULL
              AND NOT EXISTS (
                SELECT 1
                FROM Materia other
                WHERE other.correquisito = m
              )
              AND (
                :oidDepartamento IS NULL OR
                (m.departamento IS NOT NULL AND m.departamento.oidDepartamento = :oidDepartamento)
              )
              AND m.plan.oidPlan = :oidPlan
            """,
        countQuery = """
            SELECT COUNT(m)
            FROM Materia m
            WHERE m.correquisito IS NULL
              AND NOT EXISTS (
                SELECT 1
                FROM Materia other
                WHERE other.correquisito = m
              )
              AND (
                :oidDepartamento IS NULL OR
                (m.departamento IS NOT NULL AND m.departamento.oidDepartamento = :oidDepartamento)
              )
              AND m.plan.oidPlan = :oidPlan
            """
    )
    Page<Materia> findMateriasSinCorrequisitoNiReferencias(
            @Param("oidDepartamento") Integer oidDepartamento,
            @Param("oidPlan") Integer oidPlan,
            Pageable pageable);

    boolean existsByCorrequisito(Materia correquisito);

    boolean existsByCorrequisitoAndIdMateriaNot(Materia correquisito, Integer idMateria);

}
