package co.edu.unicauca.sed.api.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ESTADOACTIVIDAD")
public class EstadoActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "estadoActividadSeq")
    @SequenceGenerator(name = "estadoActividadSeq", sequenceName = "SEQ_OIDESTADOACTIVIDAD", allocationSize = 1)
    @Column(name = "OIDESTADOACTIVIDAD", nullable = false)
    private Integer oidEstadoActividad;

    @Column(name = "NOMBRE")
    private String nombre;

    @Column(name = "FECHACREACION", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHAACTUALIZACION", nullable = false)
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
