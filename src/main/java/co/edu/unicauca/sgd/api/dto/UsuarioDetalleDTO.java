package co.edu.unicauca.sgd.api.dto;

import java.time.LocalDateTime;

import co.edu.unicauca.sgd.api.domain.UsuarioDetalle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDetalleDTO {
    private Integer oidUsuarioDetalle;
    private String facultad;
    private String departamento;
    private String programa;
    private String categoria;
    private String contratacion;
    private String dedicacion;
    private String estudios;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public static UsuarioDetalleDTO fromEntity(UsuarioDetalle entity) {
        if (entity == null) {
            return null;
        }

        UsuarioDetalleDTO dto = new UsuarioDetalleDTO();
        dto.setOidUsuarioDetalle(entity.getOidUsuarioDetalle());
        dto.setFacultad(entity.getFacultad());
        dto.setDepartamento(entity.getDepartamento());
        dto.setPrograma(entity.getPrograma());
        dto.setCategoria(entity.getCategoria());
        dto.setContratacion(entity.getContratacion());
        dto.setDedicacion(entity.getDedicacion());
        dto.setEstudios(entity.getEstudios());
        dto.setFechaCreacion(entity.getFechaCreacion());
        dto.setFechaActualizacion(entity.getFechaActualizacion());
        return dto;
    }
}
