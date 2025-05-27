package co.edu.unicauca.sgd.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeccionDTO {
    private Integer oidLeccionAprendida;
    private String descripcion;
}
