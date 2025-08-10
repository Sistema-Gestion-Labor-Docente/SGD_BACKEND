package co.edu.unicauca.sgd.api.mapper;


import java.util.List;
import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.dto.materias.CorrequisitoListaResponse;

@Component
public class CorrequisitoMapper {
    public CorrequisitoListaResponse toListResponse(Integer idMateria, List<Integer> otros) {
        CorrequisitoListaResponse r = new CorrequisitoListaResponse();
        r.setIdMateria(idMateria);
        r.setCorrequisitos(otros);
        return r;
    }
}
