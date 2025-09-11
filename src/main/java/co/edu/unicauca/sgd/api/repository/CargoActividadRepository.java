package co.edu.unicauca.sgd.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.CargoActividad;

@Repository
public interface CargoActividadRepository extends JpaRepository<CargoActividad, Integer>, JpaSpecificationExecutor<CargoActividad> {
    // Métodos personalizados aquí si necesitas (por ejemplo, búsqueda por tipo)
}
