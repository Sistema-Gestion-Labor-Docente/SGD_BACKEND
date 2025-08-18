package co.edu.unicauca.sgd.api.specification;

import org.springframework.data.jpa.domain.Specification;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.utils.StringUtils;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CalendarioSpecs {

    public static Specification<Calendario> anioEq(String anio) {
        return (root, query, cb) -> StringUtils.hasText(anio)
                ? cb.equal(root.get("anioCalendario"), anio)
                : cb.conjunction();
    }

    public static Specification<Calendario> numeroEq(Integer numero) {
        return (root, query, cb) -> numero != null
                ? cb.equal(root.get("numeroCalendario"), numero)
                : cb.conjunction();
    }

    public static Specification<Calendario> estadoEq(String estado) {
        return (root, query, cb) -> StringUtils.hasText(estado)
                ? cb.equal(cb.upper(root.get("estado")), estado.toUpperCase())
                : cb.conjunction();
    }
}