package co.edu.unicauca.sed.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unicauca.sed.api.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>, JpaSpecificationExecutor<Usuario> {
        Usuario findByIdentificacion(String identificacion);

        Optional<Usuario> findByCorreo(String correo);

        boolean existsByRolesNombre(String nombreRol);

        @Query("SELECT COUNT(u) FROM Usuario u JOIN u.roles r JOIN u.estadoUsuario e " +
                        "WHERE u.usuarioDetalle.departamento = :departamento " +
                        "AND r.nombre IN :roles " +
                        "AND e.nombre = 'ACTIVO' " +
                        "AND (:idUsuario IS NULL OR u.oidUsuario <> :idUsuario)")

        long countByUsuarioDetalle_DepartamentoAndRoles_NombreInExcludingUser(
                        @Param("departamento") String departamento,
                        @Param("roles") List<String> roles,
                        @Param("idUsuario") Integer idUsuario);

        @Query("SELECT COUNT(u) " +
                        "FROM Usuario u " +
                        "JOIN u.roles r " +
                        "JOIN u.estadoUsuario e " +
                        "WHERE u.usuarioDetalle.facultad = :facultad " +
                        "AND r.nombre IN :roles " +
                        "AND (:idUsuario IS NULL OR u.oidUsuario <> :idUsuario)" +
                        "AND e.nombre = 'ACTIVO'")
        long countByUsuarioDetalle_FacultadAndRoles_NombreInExcludingUser(
                        @Param("facultad") String facultad,
                        @Param("roles") List<String> roles,
                        @Param("idUsuario") Integer idUsuario);

        @Query("SELECT u " +
                        "FROM Usuario u " +
                        "JOIN u.roles r " +
                        "JOIN u.estadoUsuario e " +
                        "WHERE u.usuarioDetalle.departamento = :departamento " +
                        "AND r.oid = :oidRol " +
                        "AND e.nombre = 'ACTIVO'")
        Optional<Usuario> findFirstActiveByDepartamentoAndRolId(
                        @Param("departamento") String departamento,
                        @Param("oidRol") Integer oidRol);

        @Query("SELECT u " +
                        "FROM Usuario u " +
                        "JOIN u.roles r " +
                        "JOIN u.estadoUsuario e " +
                        "WHERE u.usuarioDetalle.facultad = :facultad " +
                        "AND r.oid = :oidRol " +
                        "AND e.nombre = 'ACTIVO'")
        Optional<Usuario> findFirstActiveByFacultadAndRolId(
                        @Param("facultad") String facultad,
                        @Param("oidRol") Integer oidRol);
}
