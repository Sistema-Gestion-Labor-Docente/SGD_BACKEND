package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "LECCIONAPRENDIDA")
public class LeccionAprendida {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "leccionAprendidaSeq")
    @SequenceGenerator(name = "leccionAprendidaSeq", sequenceName = "SEQ_OIDLECCIONAPRENDIDA", allocationSize = 1)
    @Column(name = "OIDLECCIONAPRENDIDA")
    private Integer oidLeccionAprendida;

    @ManyToOne
    @JoinColumn(name = "OIDAUTOEVALUACION", nullable = false)
    private Autoevaluacion autoevaluacion;

    @Column(name = "DESCRIPCION", nullable = false, length = 500)
    private String descripcion;
}
