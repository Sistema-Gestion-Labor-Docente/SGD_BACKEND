package co.edu.unicauca.sed.api.domain;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CONSOLIDADO")
@Data
@NoArgsConstructor
public class Consolidado {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "consolidadoSeq")
    @SequenceGenerator(name = "consolidadoSeq", sequenceName = "SEQ_OIDCONSOLIDADO", allocationSize = 1)
    @Column(name = "OIDCONSOLIDADO")
    private Integer oidConsolidado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OIDPROCESO")
    @JsonBackReference
    private Proceso proceso;

    @Column(name = "CALIFICACION")
    private Double calificacion;

    @Column(name = "NOMBREDOCUMENTO")
    private String nombredocumento;

    @Column(name = "RUTADOCUMENTO")
    private String rutaDocumento;

    @Column(name = "NOTA")
    private String nota;

    @Column(name = "FECHACREACION", updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHAACTUALIZACION")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    /**
     * Constructor para inicializar Consolidado con un proceso.
     *
     * @param proceso Proceso asociado al consolidado.
     */
    public Consolidado(Proceso proceso) {
        this.proceso = proceso;
    }
}
