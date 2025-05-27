package co.edu.unicauca.sed.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sed.api.domain.UsuarioDetalle;

@Repository
public interface UsuarioDetalleRepository extends JpaRepository<UsuarioDetalle, Integer> {

  // Métodos para UsuarioDetalle
  @Query("SELECT DISTINCT u.facultad FROM UsuarioDetalle u WHERE u.facultad IS NOT NULL")
  List<String> findDistinctFacultad();

  @Query("SELECT DISTINCT u.departamento FROM UsuarioDetalle u WHERE u.departamento IS NOT NULL")
  List<String> findDistinctDepartamento();

  @Query("SELECT DISTINCT u.categoria FROM UsuarioDetalle u WHERE u.categoria IS NOT NULL")
  List<String> findDistinctCategoria();

  @Query("SELECT DISTINCT u.contratacion FROM UsuarioDetalle u WHERE u.contratacion IS NOT NULL")
  List<String> findDistinctContratacion();

  @Query("SELECT DISTINCT u.dedicacion FROM UsuarioDetalle u WHERE u.dedicacion IS NOT NULL")
  List<String> findDistinctDedicacion();

  @Query("SELECT DISTINCT u.estudios FROM UsuarioDetalle u WHERE u.estudios IS NOT NULL")
  List<String> findDistinctEstudios();

  @Query("SELECT DISTINCT u.programa FROM UsuarioDetalle u WHERE u.programa IS NOT NULL")
  List<String> findDistinctProgramas();
}
