package co.edu.unicauca.sed.api.service.actividad.Impl;

import co.edu.unicauca.sed.api.domain.TipoActividad;
import co.edu.unicauca.sed.api.dto.AtributoDTO;
import co.edu.unicauca.sed.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sed.api.repository.TipoActividadRepository;
import co.edu.unicauca.sed.api.repository.UsuarioRepository;
import co.edu.unicauca.sed.api.service.actividad.ActividadDetalleService;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del servicio de detalle de actividad.
 */
@Service
public class ActividadDetalleServiceImpl implements ActividadDetalleService {

    private final TipoActividadRepository tipoActividadRepository;
    private final UsuarioRepository usuarioRepository;

    public ActividadDetalleServiceImpl(TipoActividadRepository tipoActividadRepository,
            UsuarioRepository usuarioRepository) {
        this.tipoActividadRepository = tipoActividadRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public String generarNombreActividad(ActividadBaseDTO actividadDTO) {
        Integer idTipoActividad = actividadDTO.getTipoActividad().getOidTipoActividad();
        TipoActividad tipoActividad = tipoActividadRepository.findById(idTipoActividad)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de actividad no encontrado con ID: " + idTipoActividad));
    
        String nombreTipoActividad = tipoActividad.getNombre();
        List<AtributoDTO> atributos = actividadDTO.getAtributos();
    
        // Buscar valores específicos de los atributos según el tipo de actividad
        String nombreEstudiante = obtenerValorAtributo(atributos, "NOMBREESTUDIANTE");
        String materia = obtenerValorAtributo(atributos, "MATERIA");
        String grupo = obtenerValorAtributo(atributos, "GRUPO");
        String nombreProyecto = obtenerValorAtributo(atributos, "NOMBREPROYECTO");
        String actividad = obtenerValorAtributo(atributos, "ACTIVIDAD");
        String semillero = obtenerValorAtributo(atributos, "SEMILLERO");
    
        Integer idUsuario = actividadDTO.getOidEvaluador();
        /*Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + idUsuario));*/
    
        // Generar el nombre de la actividad según el tipo de actividad
        String nombreGenerado;
        switch (nombreTipoActividad.toUpperCase()) {
            case "DOCENCIA":
                nombreGenerado = String.format("%s-%s", materia, grupo);
                break;
            case "TRABAJOS DOCENCIA":
            case "TRABAJOS DE INVESTIGACION":
                nombreGenerado = String.format("Trabajo %s", nombreEstudiante);
                break;
            case "PROYECTOS INVESTIGACIÓN":
                nombreGenerado = String.format("%s", nombreProyecto);
                break;
            case "ADMINISTRACIÓN":
            case "OTROS SERVICIOS":
            case "CAPACITACIÓN":
            case "ASESORÍA":
            case "EXTENSIÓN":
                nombreGenerado = String.format("%s", actividad);
                break;
            case "SEMILLEROS DE INVESTIGACIÓN":
                nombreGenerado = String.format("%s", semillero);
                break;
            default:
                nombreGenerado = "ACTIVIDAD-GENERICA-" + idTipoActividad;
                break;
        }
        
        return nombreGenerado;
    }    

    private String obtenerValorAtributo(List<AtributoDTO> atributos, String codigoAtributo) {
        return atributos.stream()
            .filter(a -> a.getCodigoAtributo().equalsIgnoreCase(codigoAtributo))
            .map(AtributoDTO::getValor)
            .findFirst()
            .orElse("");
    }
}
