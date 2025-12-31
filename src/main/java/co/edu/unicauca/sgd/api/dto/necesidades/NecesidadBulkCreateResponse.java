package co.edu.unicauca.sgd.api.dto.necesidades;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NecesidadBulkCreateResponse {
    private List<NecesidadDTOResponse> creadas;
    private List<NecesidadBulkExcesoResponse> excedidas;
}
