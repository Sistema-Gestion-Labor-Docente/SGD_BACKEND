package co.edu.unicauca.sed.api.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "INFORMEADMINISTRACION")
@Data
public class InformeAdministracion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "informeAdministracionSeq")
    @SequenceGenerator(name = "informeAdministracionSeq", sequenceName = "SEQ_OIDINFORMEADMINISTRACION", allocationSize = 1)
    @Column(name = "OIDINFORMEADMINISTRACION", nullable = false)
    private Integer oidInformeAdministracion;

    @Column(name = "CALIFICACION", nullable = false)
    private Float calificacion;

    @ManyToOne
    @JoinColumn(name = "OIDFUENTE", nullable = false)
    private Fuente fuente;

    @ManyToOne
    @JoinColumn(name = "OIDOBJETIVOCOMPONENTE", nullable = false)
    private ObjetivoComponente objetivoComponente;

    @CreationTimestamp
    @Column(name = "FECHACREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHAACTUALIZACION")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
