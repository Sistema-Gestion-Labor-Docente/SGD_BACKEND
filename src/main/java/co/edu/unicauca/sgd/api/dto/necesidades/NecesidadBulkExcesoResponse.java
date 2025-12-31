package co.edu.unicauca.sgd.api.dto.necesidades;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NecesidadBulkExcesoResponse {
    private Integer idMateria;
    private String nombreMateria;
    private Integer gruposSolicitados;
    private Integer gruposDisponibles;
    private Integer gruposExcedidos;
}
