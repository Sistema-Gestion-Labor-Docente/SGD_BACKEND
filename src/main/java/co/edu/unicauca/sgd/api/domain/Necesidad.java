package co.edu.unicauca.sgd.api.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "NECESIDAD" /*, uniqueConstraints = @UniqueConstraint(name = "UQ_NEC_CAL_GRUPO", columnNames = {"OIDCALENDARIO","GRUPO"}) */)
public class Necesidad {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OIDNECESIDAD")
    @SequenceGenerator(name = "SEQ_OIDNECESIDAD", sequenceName = "SEQ_OIDNECESIDAD", allocationSize = 1)
    @Column(name = "OIDNECESIDAD")
    private Integer oidNecesidad;

    // relación al calendario (ya existente en tu proyecto)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "OIDCALENDARIO", referencedColumnName = "OIDCALENDARIO")
    private Calendario calendario;

    // relación a la materia (la materia contiene info del programa)
    // uso columna de FK IDMATERIA (ajusta referencedColumnName si tu PK de Materia se llama distinto)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDMATERIA", referencedColumnName = "IDMATERIA")
    private Materia materia;

    @Column(name = "GRUPO", length = 10, nullable = false)
    private String grupo;

    @Column(name = "CUPO")
    private Integer cupo;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", length = 50, nullable = false)
    private EstadoNecesidad estado;

    // relación técnica opcional entre necesidades (puede usarse para vincular la necesidad correquisito)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CORREQUISITO_OIDNECESIDAD", referencedColumnName = "OIDNECESIDAD")
    private Necesidad correquisitoNecesidad;

    // auditoría (consistente con ejemplo de Calendario)
    @Column(name = "FECHACREACION", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @CreatedBy
    @Column(name = "USUARIOCREACION", updatable = false, length = 100, nullable = false)
    private String usuarioCreacion;

    @Column(name = "FECHAACTUALIZACION")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    @LastModifiedBy
    @Column(name = "USUARIOACTUALIZACION", length = 100)
    private String usuarioActualizacion;
}
