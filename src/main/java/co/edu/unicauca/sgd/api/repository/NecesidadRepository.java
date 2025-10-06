package co.edu.unicauca.sgd.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.Necesidad;
import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;

@Repository
public interface NecesidadRepository extends JpaRepository<Necesidad, Integer>, JpaSpecificationExecutor<Necesidad> {

    // para la validación del correquisito: verificar existencia de necesidad de la materia X
    boolean existsByCalendario_OidcalendarioAndMateria_IdMateria(Integer oidCalendario, Integer idMateria);

    Optional<Necesidad> findByCalendario_OidcalendarioAndMateria_IdMateria(Integer oidCalendario, Integer idMateria);

    List<Necesidad> findAllByCalendario_Oidcalendario(Integer oidCalendario);

    // buscar por estado
    List<Necesidad> findAllByCalendario_OidcalendarioAndEstado(Integer oidCalendario, EstadoNecesidad estado);

}
