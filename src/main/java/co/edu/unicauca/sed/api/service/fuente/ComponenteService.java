package co.edu.unicauca.sed.api.service.fuente;

import co.edu.unicauca.sed.api.domain.Componente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ComponenteService {

    Componente guardar(Componente componente);

    Componente buscarPorId(Integer id);

    Page<Componente> listar(Pageable pageable);
}
