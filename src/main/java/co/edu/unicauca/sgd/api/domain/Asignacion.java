package co.edu.unicauca.sgd.api.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "ASIGNACION",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_ASIG_NEC_SEL", columnNames = { "OIDNECESIDAD", "OIDSELECCIONADO" })
    }
)
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "asignacionSeq")
    @SequenceGenerator(name = "asignacionSeq", sequenceName = "SEQ_OIDASIGNACION", allocationSize = 1)
    @Column(name = "OIDASIGNACION")
    private Integer oidAsignacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "OIDNECESIDAD", nullable = false, updatable = false)
    private Necesidad necesidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "OIDSELECCIONADO", nullable = false, updatable = false)
    private Seleccionado seleccionado;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    @JoinColumn(name = "OIDACTIVIDAD", nullable = false)
    private Actividad actividad;

    @Column(name = "HORASDOCENCIA")
    private Float horasDocencia;

    @Column(name = "SEMANASDOCENCIA")
    private Float semanasDocencia;

    @Column(name = "HORASPREPARACION")
    private Float horasPreparacion;

    @Column(name = "SEMANASPREPARACION")
    private Float semanasPreparacion;

    @CreationTimestamp
    @Column(name = "FECHACREACION", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @CreatedBy
    @Column(name = "USUARIOCREACION", nullable = false, updatable = false, length = 100)
    private String usuarioCreacion;

    @UpdateTimestamp
    @Column(name = "FECHAACTUALIZACION")
    private LocalDateTime fechaActualizacion;

    @LastModifiedBy
    @Column(name = "USUARIOACTUALIZACION", length = 100)
    private String usuarioActualizacion;
}
