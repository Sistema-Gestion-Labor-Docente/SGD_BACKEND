package co.edu.unicauca.sgd.api.repository.projection;

public interface ActividadUsuariosProjection {
    Integer getOidActividad();
    Integer getOidActividadCalendario();
    Integer getOidCargoActividad();
    Integer getOidCalendario();
    String getNombreActividad();
    Double getSemanas();
    Integer getIdLaborDocente();
    Boolean getInformeEjecutivo();
    String getUsuariosJson();
}
