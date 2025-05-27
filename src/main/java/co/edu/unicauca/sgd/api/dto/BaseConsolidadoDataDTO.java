package co.edu.unicauca.sgd.api.dto;

import java.util.List;

import co.edu.unicauca.sgd.api.domain.PeriodoAcademico;
import co.edu.unicauca.sgd.api.domain.Proceso;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioDetalle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseConsolidadoDataDTO {
    private Usuario evaluado;
    private UsuarioDetalle detalleUsuario;
    private PeriodoAcademico periodoAcademico;
    private List<Proceso> procesos;
}
