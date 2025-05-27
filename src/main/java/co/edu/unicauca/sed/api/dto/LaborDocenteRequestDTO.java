package co.edu.unicauca.sed.api.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LaborDocenteRequestDTO {

    private Integer oidPeriodoAcademico;
    private Integer oidUsuario;
    private String nombreDocumento;
    private MultipartFile documento;
}
