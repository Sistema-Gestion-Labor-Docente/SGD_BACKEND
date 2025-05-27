package co.edu.unicauca.sed.api.dto.actividad;

import java.time.LocalDateTime;
import java.util.List;

import co.edu.unicauca.sed.api.domain.TipoActividad;
import co.edu.unicauca.sed.api.dto.AtributoDTO;
import co.edu.unicauca.sed.api.dto.FuenteDTO;
import co.edu.unicauca.sed.api.dto.UsuarioDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActividadBaseDTO {

    private Integer oidActividad;
    private TipoActividad tipoActividad;
    private Integer oidProceso;
    private Integer oidEstadoActividad;
    private String nombreActividad;
    private Float horas;
    private Float semanas;
    private Boolean informeEjecutivo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private List<FuenteDTO> fuentes;
    private List<AtributoDTO> atributos;
    private UsuarioDTO evaluador;
    private Integer oidEvaluado;
    private Integer oidEvaluador;
    private Integer idLaborDocente;
    private Boolean esLaborDocente;
    private Boolean archivoLaborDocente;

    // Constructor completo
    public ActividadBaseDTO(Integer oidActividad, TipoActividad tipoActividad, Integer oidProceso, Integer oidEstadoActividad,
                             String nombreActividad, Float horas, Float semanas, Boolean informeEjecutivo,
                             LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion, List<FuenteDTO> fuentes,
                             List<AtributoDTO> atributos, UsuarioDTO evaluador, Integer oidEvaluado, Integer oidEvaluador) {
        this.oidActividad = oidActividad;
        this.tipoActividad = tipoActividad;
        this.oidProceso = oidProceso;
        this.oidEstadoActividad = oidEstadoActividad;
        this.nombreActividad = nombreActividad;
        this.horas = horas;
        this.semanas = semanas;
        this.informeEjecutivo = informeEjecutivo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.fuentes = fuentes;
        this.atributos = atributos;
        this.evaluador = evaluador;
        this.oidEvaluado = oidEvaluado;
        this.oidEvaluador = oidEvaluador;
    }
}
