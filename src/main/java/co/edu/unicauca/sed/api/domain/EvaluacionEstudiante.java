package co.edu.unicauca.sed.api.domain;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "EVALUACIONESTUDIANTE")
@Data
public class EvaluacionEstudiante {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "evaluacionEstudianteSeq")
    @SequenceGenerator(name = "evaluacionEstudianteSeq", sequenceName = "SEQ_OIDEVALUACIONESTUDIANTE", allocationSize = 1)
    @Column(name = "OIDEVALUACIONESTUDIANTE")
    private Integer oidEvaluacionEstudiante;

    @ManyToOne
    @JoinColumn(name = "OIDFUENTE", nullable = false)
    private Fuente fuente;

    @ManyToOne
    @JoinColumn(name = "OIDESTADOETAPADESARROLLO", nullable = false)
    private EstadoEtapaDesarrollo estadoEtapaDesarrollo;

    @Column(name = "OBSERVACION")
    private String observacion;

    @CreationTimestamp
    @Column(name = "FECHAEVALUACION", nullable = false)
    private LocalDateTime fechaEvaluacion;

}
