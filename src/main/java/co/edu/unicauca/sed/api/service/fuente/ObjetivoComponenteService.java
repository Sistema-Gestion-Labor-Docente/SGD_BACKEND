package co.edu.unicauca.sed.api.service.fuente;

import co.edu.unicauca.sed.api.domain.ObjetivoComponente;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.ComponenteConObjetivosDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ObjetivoComponenteService {

    ObjetivoComponente guardar(ObjetivoComponente objetivoComponente);

    ObjetivoComponente buscarPorId(Integer id);

    ApiResponse<Page<ComponenteConObjetivosDTO>> listar(Pageable pageable);
}
