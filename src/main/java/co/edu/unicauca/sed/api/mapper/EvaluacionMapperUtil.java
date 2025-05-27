package co.edu.unicauca.sed.api.mapper;


import co.edu.unicauca.sed.api.domain.Fuente;
import co.edu.unicauca.sed.api.domain.Usuario;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class EvaluacionMapperUtil {

    public static Map<String, Object> construirInformacionFuente(Fuente fuente, String tipoCalificacion, String observacion, String nombreArchivo) {
        Map<String, Object> fuenteMap = new LinkedHashMap<>();
        fuenteMap.put("oidFuente",fuente.getOidFuente());
        fuenteMap.put("evaluado", construirUsuarioMap(fuente.getActividad().getProceso().getEvaluado()));
        fuenteMap.put("evaluador", construirUsuarioMap(fuente.getActividad().getProceso().getEvaluador()));
        fuenteMap.put("nombreActividad", fuente.getActividad().getNombreActividad());
        fuenteMap.put("observacion", observacion);
        fuenteMap.put("nombreArchivo", nombreArchivo);
        fuenteMap.put("calificacion", fuente.getCalificacion());
        fuenteMap.put("tipoCalificacion", tipoCalificacion);
        fuenteMap.put("fechaCreacion", fuente.getFechaCreacion());
        fuenteMap.put("fechaActualizacion", fuente.getFechaActualizacion());
        return fuenteMap;
    }

    private static Map<String, Object> construirUsuarioMap(Usuario usuario) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("oidUsuario", usuario.getOidUsuario());
        userMap.put("nombres", usuario.getNombres());
        userMap.put("apellidos", usuario.getApellidos());
        userMap.put("departamento", usuario.getUsuarioDetalle().getDepartamento());
        userMap.put("nombreCompleto", usuario.getNombres() + " " + usuario.getApellidos());
        return userMap;
    }
}