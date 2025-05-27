package co.edu.unicauca.sgd.api.service.fuente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.domain.ObjetivoComponente;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.ComponenteConObjetivosDTO;

public interface ObjetivoComponenteService {

    ObjetivoComponente guardar(ObjetivoComponente objetivoComponente);

    ObjetivoComponente buscarPorId(Integer id);

    ApiResponse<Page<ComponenteConObjetivosDTO>> listar(Pageable pageable);
}
