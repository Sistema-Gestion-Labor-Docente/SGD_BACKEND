package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "COMPONENTE")
@Data
public class Componente {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "componenteSeq")
    @SequenceGenerator(name = "componenteSeq", sequenceName = "SEQ_OIDCOMPONENTE", allocationSize = 1)
    @Column(name = "OIDCOMPONENTE", nullable = false)
    private Integer oidComponente;

    @Column(name = "NOMBRE", nullable = false, length = 255)
    private String nombre;

    @Column(name = "PORCENTAJE", nullable = false)
    private Float porcentaje;
}
