package co.edu.unicauca.sgd.api.service.fuente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.domain.Componente;

public interface ComponenteService {

    Componente guardar(Componente componente);

    Componente buscarPorId(Integer id);

    Page<Componente> listar(Pageable pageable);
}
