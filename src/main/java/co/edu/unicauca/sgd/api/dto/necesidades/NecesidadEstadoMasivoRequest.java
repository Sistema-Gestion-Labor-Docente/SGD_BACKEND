package co.edu.unicauca.sgd.api.dto.necesidades;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NecesidadEstadoMasivoRequest {
    private List<Integer> oidNecesidades;
}
