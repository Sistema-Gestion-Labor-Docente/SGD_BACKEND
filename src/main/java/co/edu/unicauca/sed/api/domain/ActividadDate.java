package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * Entidad que representa un valor de fecha en la actividad del modelo EAV.
 */
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ACTIVIDADDATE")
public class ActividadDate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actividadDateSeq")
    @SequenceGenerator(name = "actividadDateSeq", sequenceName = "SEQ_OIDACTIVIDADDATE", allocationSize = 1)
    @Column(name = "OIDACTIVIDADDATE")
    private Integer oidActividadDate;

    @ManyToOne
    @JoinColumn(name = "OIDEAVATRIBUTO", nullable = false)
    private EavAtributo eavAtributo;

    @ManyToOne
    @JoinColumn(name = "OIDACTIVIDAD")
    private Actividad actividad;

    @Column(name = "VALOR", nullable = false)
    private LocalDateTime valor;

    @Column(name = "FECHACREACION", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHAACTUALIZACION", nullable = false)
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    public ActividadDate() {}

    public ActividadDate(Actividad actividad, EavAtributo eavAtributo, LocalDateTime valor) {
        this.actividad = actividad;
        this.eavAtributo = eavAtributo;
        this.valor = valor;
    }
}
