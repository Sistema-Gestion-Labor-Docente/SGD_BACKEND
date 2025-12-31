package co.edu.unicauca.sgd.api.service.necesidad;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;
import java.util.List;
import java.util.Map;

public interface NecesidadEstadoService {

    ApiResponse<Map<String, Object>> cambiarEstadoMasivo(Integer oidCalendario,
                                                         EstadoNecesidad estadoOrigen,
                                                         EstadoNecesidad estadoDestino,
                                                         Integer oidPrograma,
                                                         Integer oidDepartamento,
                                                         java.util.List<Integer> oidNecesidades);

    ApiResponse<Map<String, Object>> cambiarEstadoPorOids(List<Integer> oidNecesidades,
                                                          EstadoNecesidad estadoOrigen,
                                                          EstadoNecesidad estadoDestino);
}
