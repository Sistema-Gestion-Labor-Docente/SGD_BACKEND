package co.edu.unicauca.sed.api.repository;

import org.springframework.stereotype.Repository;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.Fuente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface FuenteRepository extends JpaRepository<Fuente, Integer> {

    // Método personalizado para buscar fuentes por OID de la actividad
    @Query("SELECT f FROM Fuente f WHERE f.actividad.oidActividad = :oidActividad")
    List<Fuente> findByActividadOid(@Param("oidActividad") Integer oidActividad);

    Optional<Fuente> findByActividadAndTipoFuente(Actividad actividad, String tipoFuente);

    @Query("""
                SELECT f FROM Fuente f
                WHERE f.estadoFuente.oidEstadoFuente = :estado
                  AND f.tipoFuente = :tipo
                  AND f.actividad.proceso.oidPeriodoAcademico.oidPeriodoAcademico = :oidPeriodo
            """)
    List<Fuente> findFuentesPendientesByTipoAndPeriodo(
            @Param("estado") Integer estado,
            @Param("tipo") String tipo,
            @Param("oidPeriodo") Integer oidPeriodo);
}
