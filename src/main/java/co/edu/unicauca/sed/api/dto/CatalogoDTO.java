package co.edu.unicauca.sed.api.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class CatalogoDTO {

    private List<Map<String, String>> facultades;
    private List<Map<String, String>> departamentos;
    private List<Map<String, String>> categorias;
    private List<Map<String, String>> contrataciones;
    private List<Map<String, String>> dedicaciones;
    private List<Map<String, String>> estudios;
    private List<Map<String, String>> programas;
    private List<Map<String, Object>> roles;
    private List<Map<String, Object>> tipoActividades;
    private List<Map<String, Object>> preguntaEvaluacionDocente;
    private List<Map<String, Object>> estadoEtapaDesarrollo;
}
