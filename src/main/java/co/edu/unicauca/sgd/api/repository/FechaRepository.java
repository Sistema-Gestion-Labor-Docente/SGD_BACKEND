package co.edu.unicauca.sgd.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.Fecha;
import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;

@Repository
public interface FechaRepository extends JpaRepository<Fecha, Integer>, JpaSpecificationExecutor<Fecha> {

    List<Fecha> findByCalendario_Oidcalendario(Integer oidCalendario);

    long countByCalendario_OidcalendarioAndTipo(Integer oidCalendario, TipoFechaEnum tipo);

    long countByCalendario_OidcalendarioAndTipoAndOidFechaNot(Integer oidCalendario, TipoFechaEnum tipo, Integer oidFecha);

    Optional<Fecha> findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(Integer oidCalendario, Integer oidNombreFecha);

    Boolean existsByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(Integer oidCalendario, Integer oidNombreFecha);

}

