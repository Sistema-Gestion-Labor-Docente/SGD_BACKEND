package co.edu.unicauca.sed.api.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import co.edu.unicauca.sed.api.domain.PeriodoAcademico;
import co.edu.unicauca.sed.api.domain.Proceso;
import co.edu.unicauca.sed.api.domain.Usuario;

public interface ProcesoRepository extends JpaRepository<Proceso, Integer>, JpaSpecificationExecutor<Proceso> {

    List<Proceso> findByEvaluado_OidUsuario(Integer oidUsuario);
    List<Proceso> findByOidPeriodoAcademico_OidPeriodoAcademico(Integer oidPeriodoAcademico);
    List<Proceso> findByEvaluadoAndOidPeriodoAcademico_OidPeriodoAcademico(Usuario evaluado, Integer oidPeriodoAcademico);
    List<Proceso> findByEvaluado_OidUsuarioAndOidPeriodoAcademico_OidPeriodoAcademico(Integer oidUsuario, Integer oidPeriodoAcademico);
    List<Proceso> findByEvaluado(Usuario evaluado);
    Optional<Proceso> findByEvaluadorAndEvaluadoAndOidPeriodoAcademicoAndNombreProceso(Usuario evaluador, Usuario evaluado, PeriodoAcademico oidPeriodoAcademico, String nombreProceso);
}
