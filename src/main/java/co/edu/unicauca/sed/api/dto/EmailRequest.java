package co.edu.unicauca.sed.api.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class EmailRequest {
    private List<String> correos;
    private String asunto;
    private String mensaje;
    private Map<String, byte[]> documentos;
}
