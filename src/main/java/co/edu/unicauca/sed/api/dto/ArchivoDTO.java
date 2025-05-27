package co.edu.unicauca.sed.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArchivoDTO {
    private String nombre;
    private String ruta;
}
