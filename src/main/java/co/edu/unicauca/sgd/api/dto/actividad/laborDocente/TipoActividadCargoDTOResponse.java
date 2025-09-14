package co.edu.unicauca.sgd.api.dto.actividad.laborDocente;

import jakarta.persistence.Column;

public class TipoActividadCargoDTOResponse {

    private Integer oidTipoActividad;
    private String nombre;
    private String descripcion;

    private CargoActividadDTOResponse cargoActividad;

}
