package co.edu.unicauca.sed.api.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComponenteConObjetivosDTO {
    private Integer oidComponente;
    private String nombre;
    private Float porcentaje;
    private List<ObjetivoDTO> objetivos;
}