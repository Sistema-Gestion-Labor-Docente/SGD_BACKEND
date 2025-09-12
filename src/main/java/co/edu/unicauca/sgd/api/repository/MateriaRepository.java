package co.edu.unicauca.sgd.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.Materia;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, Integer>, JpaSpecificationExecutor<Materia> {

    List<Materia> findAllByPlanOidPlan(Integer oidPlan);

    Optional<Materia> findFirstByOidMateriaOrCodigoOrNombre(String oidMateria, String codigo, String nombre);

    Optional<Materia> findFirstByOidMateriaIgnoreCaseOrCodigoIgnoreCaseOrNombreIgnoreCase(String oidMateria, String codigo, String nombre);
    
    Optional<Materia> findByOidMateria(String oidMateria);

}
