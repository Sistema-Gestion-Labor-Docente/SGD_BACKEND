package co.edu.unicauca.sgd.api.dto.necesidades;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NecesidadBulkCreateRequest {

    @NotNull(message = "El calendario es obligatorio")
    private Integer oidCalendario;

    @NotEmpty(message = "Debe proporcionar al menos una materia")
    @Valid
    private List<NecesidadBulkItemRequest> necesidades;

    @Data
    public static class NecesidadBulkItemRequest {
        @NotNull(message = "La materia es obligatoria")
        private Integer idMateria;

        @NotNull(message = "La cantidad de grupos es obligatoria")
        @Min(value = 1, message = "La cantidad de grupos debe ser al menos 1")
        private Integer cantidadGrupos;

        @NotNull(message = "El cupo es obligatorio")
        @Min(value = 0, message = "El cupo debe ser mayor o igual a 0")
        private Integer cupo;
    }
}
