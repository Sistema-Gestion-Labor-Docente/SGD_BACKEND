package co.edu.unicauca.sgd.api.dto.necesidades;

import java.time.LocalDateTime;

import co.edu.unicauca.sgd.api.dto.materias.MateriaDTOResponse;
import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NecesidadDTOResponse {

    private Integer oidNecesidad;

    private Integer oidCalendario;
    private String anioCalendario;
    private Integer numeroCalendario;

    private Integer idMateria;
    private String oidMateria;
    private String codigoMateria;
    private String nombreMateria;
    private Integer semestreMateria;

    /** Información completa de la materia asociada. */
    private MateriaDTOResponse materia;

    private String grupo;
    private Integer cupo;
    private EstadoNecesidad estado;
    private String estadoDescripcion;

    private Integer correquisitoOidNecesidad;
    private String correquisitoNombreMateria;

    private LocalDateTime fechaCreacion;
    private String usuarioCreacion;
    private LocalDateTime fechaActualizacion;
    private String usuarioActualizacion;
}
