package co.edu.unicauca.sgd.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unicauca.sgd.api.domain.materias.MateriaCorrequisito;
import co.edu.unicauca.sgd.api.domain.materias.MateriaCorrequisitoId;

public interface MateriaCorrequisitoRepository extends JpaRepository<MateriaCorrequisito, MateriaCorrequisitoId> {

    /* Devuelve los ids de las materias co-requisito de una dada, sin importar si queda en A o B */
    @Query("""
        select case
                when mc.materiaA.idMateria = :id then mc.materiaB.idMateria
                else mc.materiaA.idMateria
            end
        from MateriaCorrequisito mc
        where mc.materiaA.idMateria = :id or mc.materiaB.idMateria = :id
    """)
    List<Integer> findCorrequisitosIds(@Param("id") Integer id);

    /* Elimina todas las filas donde participa la materia */
    @Modifying
    @Query("""
        delete from MateriaCorrequisito mc
        where mc.materiaA.idMateria = :oid or mc.materiaB.idMateria = :id
    """)
    void deleteAllForMateria(@Param("id") Integer id);

}
