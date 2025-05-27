package co.edu.unicauca.sed.api.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ESTADOFUENTE")
@Data
public class EstadoFuente {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "estadoFuenteSeq")
    @SequenceGenerator(name = "estadoFuenteSeq", sequenceName = "SEQ_OIDESTADOFUENTE", allocationSize = 1)
    @Column(name = "OIDESTADOFUENTE", nullable = false)
    private Integer oidEstadoFuente;

    @Column(name = "NOMBREESTADO", nullable = false)
    private String nombreEstado;

    @Column(name = "FECHACREACION", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHAACTUALIZACION", nullable = false)
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
