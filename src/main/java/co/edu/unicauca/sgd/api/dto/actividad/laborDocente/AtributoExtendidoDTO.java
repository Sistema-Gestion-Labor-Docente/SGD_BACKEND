package co.edu.unicauca.sgd.api.dto.actividad.laborDocente;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AtributoExtendidoDTO {
    private String nombre;
    private String tipo;
    private String valor;
}