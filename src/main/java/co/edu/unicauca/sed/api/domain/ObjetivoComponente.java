package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "OBJETIVOCOMPONENTE")
@Data
public class ObjetivoComponente {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "objetivoComponenteSeq")
    @SequenceGenerator(name = "objetivoComponenteSeq", sequenceName = "SEQ_OIDOBJETIVOCOMPONENTE", allocationSize = 1)
    @Column(name = "OIDOBJETIVOCOMPONENTE", nullable = false)
    private Integer oidObjetivoComponente;

    @Column(name = "DESCRIPCION", nullable = false, length = 500)
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "OIDCOMPONENTE", nullable = false)
    private Componente componente;
}
