package co.edu.unicauca.sgd.api.domain;

import java.time.LocalDateTime;

import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "FECHA")
public class Fecha {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fechaSeq")
    @SequenceGenerator(name = "fechaSeq", sequenceName = "SEQ_OIDFECHA", allocationSize = 1)
    @Column(name = "OIDFECHA")
    private Integer oidFecha;

    @Column(name = "NOMBRE", nullable = false, length = 255)
    private String nombre;

    @Column(name = "FECHAINICIAL", nullable = false)
    private LocalDateTime fechaInicial;

    @Column(name = "FECHAFIN", nullable = false)
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false)
    private TipoFechaEnum tipo;

    @ManyToOne
    @JoinColumn(name = "OIDCALENDARIO", nullable = false)
    private Calendario calendario;
}
