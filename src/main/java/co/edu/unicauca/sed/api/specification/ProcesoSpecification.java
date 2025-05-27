package co.edu.unicauca.sed.api.specification;

import org.springframework.data.jpa.domain.Specification;

import co.edu.unicauca.sed.api.domain.Proceso;

import java.time.LocalDateTime;

public class ProcesoSpecification {

    public static Specification<Proceso> byFilters(Integer evaluadorId, Integer evaluadoId, Integer idPeriodo, String nombreProceso, LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion) {

        Specification<Proceso> spec = Specification.where(null);

        if (evaluadorId != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("evaluador").get("oidUsuario"), evaluadorId));
        }

        if (evaluadoId != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("evaluado").get("oidUsuario"), evaluadoId));
        }

        if (idPeriodo != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("oidPeriodoAcademico").get("oidPeriodoAcademico"), idPeriodo));
        }

        if (nombreProceso != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("nombreProceso"),"%" + nombreProceso + "%"));
        }

        if (fechaCreacion != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("fechaCreacion"), fechaCreacion));
        }

        if (fechaActualizacion != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("fechaActualizacion"), fechaActualizacion));
        }

        return spec;
    }
}
