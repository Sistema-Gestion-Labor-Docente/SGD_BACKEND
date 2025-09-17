package co.edu.unicauca.sgd.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.UsuarioDepartamento;

public interface UsuarioDepartamentoRepository extends JpaRepository<UsuarioDepartamento, Integer>, JpaSpecificationExecutor<UsuarioDepartamento> {

    List<UsuarioDepartamento> findByDepartamento(Departamento departamento);

    boolean existsByUsuarioOidUsuarioAndDepartamentoOidDepartamento(Integer oidUsuario, Integer oidDepartamento);

}
