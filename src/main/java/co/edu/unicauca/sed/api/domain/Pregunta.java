package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Check;

/**
 * Entidad que representa una pregunta de evaluación.
 */
@Entity
@Table(name = "PREGUNTA")
@Data
@Check(constraints = "ESTADOPREGUNTA IN (0,1)") // 0: Inactivo, 1: Activo
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "preguntaSeq")
    @SequenceGenerator(name = "preguntaSeq", sequenceName = "SEQ_OIDPREGUNTA", allocationSize = 1)
    @Column(name = "OIDPREGUNTA")
    private Integer oidPregunta;

    @Column(name = "PREGUNTA", nullable = false, length = 255)
    private String pregunta;

    @Column(name = "PORCENTAJEIMPORTANCIA", nullable = false)
    private Float porcentajeImportancia;

    @Column(name = "ESTADOPREGUNTA", nullable = false)
    private Boolean estadoPregunta;
}
