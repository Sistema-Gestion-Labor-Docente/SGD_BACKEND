package co.edu.unicauca.sgd.api.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ACTIVIDADCALENDARIO")
@Data
public class ActividadCalendario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actividadCalendarioSeq")
    @SequenceGenerator(name = "actividadCalendarioSeq", sequenceName = "SEQ_OIDACTIVIDADCALENDARIO", allocationSize = 1)
    @Column(name = "OIDACTIVIDADCALENDARIO")
    private Integer oidActividadCalendario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OIDACTIVIDAD", nullable = false)
    private Actividad actividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OIDCALENDARIO", nullable = false)
    private Calendario calendario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OIDCARGOACTIVIDAD")
    private CargoActividad cargoActividad;

    @Column(name = "FECHACREACION", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @CreatedBy
    @Column(name = "USUARIOCREACION", updatable = false, nullable = false, length = 100)
    private String usuarioCreacion;

    @Column(name = "FECHAACTUALIZACION")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    @LastModifiedBy
    @Column(name = "USUARIOACTUALIZACION", length = 100)
    private String usuarioActualizacion;
}
