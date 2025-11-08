package co.edu.unicauca.sgd.api.service.actividad.Impl;

import org.springframework.stereotype.Service;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.dto.AtributoDTO;
import co.edu.unicauca.sgd.api.dto.RolDTO;
import co.edu.unicauca.sgd.api.dto.UsuarioDTO;
import co.edu.unicauca.sgd.api.dto.UsuarioDetalleDTO;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sgd.api.service.EavAtributoService;
import co.edu.unicauca.sgd.api.service.actividad.ActividadDTOService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de conversión entre entidades Actividad y sus
 * respectivos DTOs.
 */
@Service
public class ActividadDTOServiceImpl implements ActividadDTOService {

    private static final String DEFAULT_NAME = "N/A";

    private final EavAtributoService eavAtributoService;


    public ActividadDTOServiceImpl(EavAtributoService eavAtributoService) {
        this.eavAtributoService = eavAtributoService;
    }

    @Override
    public ActividadBaseDTO buildActividadBaseDTO(Actividad actividad) {
        // Obtener los atributos dinámicos en formato AtributoDTO
        List<AtributoDTO> atributos = eavAtributoService.obtenerAtributosPorActividad(actividad);

        // Determinar si tiene relación con LaborDocente
        Integer idLabor = actividad.getIdLaborDocente();
        Boolean esLabor = (idLabor != null);

        return new ActividadBaseDTO(
                actividad.getOidActividad(),
                actividad.getTipoActividad(),
                actividad.getEstadoActividad().getOidEstadoActividad(),
                actividad.getNombreActividad(),
                actividad.getHoras(),
                actividad.getSemanas(),
                actividad.getInformeEjecutivo(),
                actividad.getFechaCreacion(),
                actividad.getFechaActualizacion(),
                atributos,
                actividad.getIdLaborDocente(),
                esLabor);
    }  

    @Override
    public UsuarioDTO convertToUsuarioDTO(Usuario usuario) {
        List<RolDTO> rolDTOList = usuario.getRoles().stream()
                .map(rol -> new RolDTO(rol.getNombre()))
                .collect(Collectors.toList());

        String nombres = usuario.getNombres() != null ? usuario.getNombres() : DEFAULT_NAME;
        String apellidos = usuario.getApellidos() != null ? usuario.getApellidos() : DEFAULT_NAME;

        UsuarioDTO dto = new UsuarioDTO(
                usuario.getOidUsuario(),
                usuario.getIdentificacion(),
                nombres,
                apellidos,
                rolDTOList);
        dto.setUsuarioDetalle(UsuarioDetalleDTO.fromEntity(usuario.getUsuarioDetalle()));
        return dto;
    }
}
