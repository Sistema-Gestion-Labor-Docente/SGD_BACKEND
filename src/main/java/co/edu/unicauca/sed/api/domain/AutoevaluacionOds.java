package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "AUTOEVALUACIONODS")
public class AutoevaluacionOds {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "autoevaluacionOdsSeq")
    @SequenceGenerator(name = "autoevaluacionOdsSeq", sequenceName = "SEQ_OIDAUTOEVALUACIONODS", allocationSize = 1)
    @Column(name = "OIDAUTOEVALUACIONODS")
    private Integer oidAutoevaluacionOds;

    @ManyToOne
    @JoinColumn(name = "OIDAUTOEVALUACION", nullable = false)
    private Autoevaluacion autoevaluacion;

    @ManyToOne
    @JoinColumn(name = "OIDOBJETIVODESARROLLOSOSTE", nullable = false)
    private ObjetivoDesarrolloSostenible ods;

    @Column(name = "RESULTADO", length = 255)
    private String resultado;

    @Column(name = "NOMBREDOCUMENTO", length = 255)
    private String nombreDocumento;

    @Column(name = "RUTADOCUMENTO", length = 255)
    private String rutaDocumento;
}
