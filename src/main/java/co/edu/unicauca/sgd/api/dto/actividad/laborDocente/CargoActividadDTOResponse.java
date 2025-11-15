package co.edu.unicauca.sgd.api.dto.actividad.laborDocente;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CargoActividadDTOResponse {

    private Integer oidCargoActividad;
    private String nombre;
    private String tipo;
    private Float maxHorasSemana;
    private Integer maxActividades;
    private Integer oidTipoActividad;
    private String nombreTipoActividad;

    private LocalDateTime fechaCreacion;
    private String usuarioCreacion;
    private LocalDateTime fechaActualizacion;
    private String usuarioActualizacion;
    
}
