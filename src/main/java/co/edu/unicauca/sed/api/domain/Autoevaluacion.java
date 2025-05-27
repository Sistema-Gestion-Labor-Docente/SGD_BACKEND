package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "AUTOEVALUACION")
public class Autoevaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "autoevaluacionSeq")
    @SequenceGenerator(name = "autoevaluacionSeq", sequenceName = "SEQ_OIDAUTOEVALUACION", allocationSize = 1)
    @Column(name = "OIDAUTOEVALUACION")
    private Integer oidAutoevaluacion;

    @ManyToOne
    @JoinColumn(name = "OIDFUENTE", nullable = false)
    private Fuente fuente;

    @Column(name = "DESCRIPCION")
    private String descripcion;

    @Column(name = "SCREENSHOTSIMCA")
    private String screenshotSimca;

    @Column(name = "RUTADOCUMENTOSC")
    private String rutaDocumentoSc;

    @Column(name = "FECHACREACION", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHAACTUALIZACION", nullable = false)
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
