package co.edu.unicauca.sed.api.dto.actividad;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ActividadPaginadaDTO {
    private Map<String, List<Map<String, Object>>> actividades;
    private int currentPage;
    private int pageSize;
    private int totalItems;
    private int totalPages;
}
