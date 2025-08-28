package co.edu.unicauca.sgd.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.edu.unicauca.sgd.api.domain.Materia;

public interface MateriaRepository extends JpaRepository<Materia, Integer>, JpaSpecificationExecutor<Materia> {

    Optional<Materia> findFirstByOidMateriaOrCodigoOrNombre(String oidMateria, String codigo, String nombre);

    Optional<Materia> findFirstByOidMateriaIgnoreCaseOrCodigoIgnoreCaseOrNombreIgnoreCase(String oidMateria, String codigo, String nombre);
    
    Optional<Materia> findByOidMateria(String oidMateria);

}
