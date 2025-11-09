package co.edu.unicauca.sgd.api.dto.actividad.laborDocente;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionHorasCargoDTOResponse {

    private boolean puedeAsignar;
    private Integer oidUsuarioMenorCupo;
    private Float horasDisponiblesUsuarioMenorCupo;
    private Float horasMaximasCargo;
    private Float semanasMaximas;
}
