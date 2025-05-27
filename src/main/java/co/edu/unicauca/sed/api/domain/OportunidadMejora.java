package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "OPORTUNIDADMEJORA")
public class OportunidadMejora {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oportunidadMejoraSeq")
    @SequenceGenerator(name = "oportunidadMejoraSeq", sequenceName = "SEQ_OIDOPORTUNIDADMEJORA", allocationSize = 1)
    @Column(name = "OIDOPORTUNIDADMEJORA")
    private Integer oidOportunidadMejora;

    @ManyToOne
    @JoinColumn(name = "OIDAUTOEVALUACION", nullable = false)
    private Autoevaluacion autoevaluacion;

    @Column(name = "DESCRIPCION", nullable = false, length = 500)
    private String descripcion;
}
