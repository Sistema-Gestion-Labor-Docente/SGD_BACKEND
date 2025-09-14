package co.edu.unicauca.sgd.api.dto.actividad;

import java.time.LocalDateTime;
import java.util.List;

import co.edu.unicauca.sgd.api.domain.TipoActividad;
import co.edu.unicauca.sgd.api.dto.AtributoDTO;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.CargoActividadDTOResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActividadBaseDTO {

    private Integer oidActividad;
    private TipoActividad tipoActividad;
    private CargoActividadDTOResponse cargoActividad;
    private Integer oidEstadoActividad;
    private String nombreActividad;
    private Float horas;
    private Float semanas;
    private Boolean informeEjecutivo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private List<AtributoDTO> atributos;
    private Integer idLaborDocente;
    private Boolean esLaborDocente;
    private Boolean archivoLaborDocente;

    // Constructor completo
    public ActividadBaseDTO(Integer oidActividad, TipoActividad tipoActividad, Integer oidEstadoActividad,
                             String nombreActividad, Float horas, Float semanas, Boolean informeEjecutivo,
                             LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion,
                             List<AtributoDTO> atributos, Integer idLaborDocente, Boolean archivoLaborDocente) {
        this.oidActividad = oidActividad;
        this.tipoActividad = tipoActividad;
        this.oidEstadoActividad = oidEstadoActividad;
        this.nombreActividad = nombreActividad;
        this.horas = horas;
        this.semanas = semanas;
        this.informeEjecutivo = informeEjecutivo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.atributos = atributos;
        this.idLaborDocente = idLaborDocente;
        this.archivoLaborDocente = archivoLaborDocente;
    }
}
