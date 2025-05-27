package co.edu.unicauca.sed.api.domain;

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
@Table(name = "ENCUESTARESPUESTA")
@Data
public class EncuestaRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "encuestaRespuestaSeq")
    @SequenceGenerator(name = "encuestaRespuestaSeq", sequenceName = "SEQ_OIDENCUESTARESPUESTA", allocationSize = 1)
    @Column(name = "OIDENCUESTARESPUESTA")
    private Integer oidEncuestaRespuesta;

    @ManyToOne
    @JoinColumn(name = "OIDENCUESTA", nullable = false)
    private Encuesta encuesta;

    @ManyToOne
    @JoinColumn(name = "OIDPREGUNTA", nullable = false)
    private Pregunta pregunta;

    @Column(name = "RESPUESTA", nullable = false)
    private String respuesta;
}
