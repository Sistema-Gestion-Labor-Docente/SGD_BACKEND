package co.edu.unicauca.sgd.api.dto.calendario;

import java.time.LocalDateTime;

import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FechaDTOResponse {

    private Integer oidFecha;

    private Integer oidNombreFecha;
    private String nombre;
    private boolean uniqueDate;

    private LocalDateTime fechaInicial;
    private LocalDateTime fechaFin;
    private TipoFechaEnum tipo;

    private Integer oidCalendario;
    private String nombreCalendario;

    public void setUniqueDate(boolean uniqueDate) {
        this.uniqueDate = uniqueDate;
    }

    // Opcional: forzar también getUniqueDate() si alguna parte lo invoca
    public boolean getUniqueDate() {
        return this.uniqueDate;
    }

}

