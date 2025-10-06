package co.edu.unicauca.sgd.api.dto.necesidades;

import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NecesidadDTORequest {

    private Integer oidCalendario;

    private Integer idMateria;

    @NotBlank(message = "El grupo es obligatorio")
    @Size(max = 2, message = "El grupo no puede superar 10 caracteres")
    private String grupo;

    @Min(value = 0, message = "El cupo debe ser mayor o igual a 0")
    private Integer cupo;

    private EstadoNecesidad estado;

    private Integer correquisitoOidNecesidad;
}
