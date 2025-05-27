package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * Entidad que representa un valor booleano en la actividad del modelo EAV.
 */
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ACTIVIDADBOOLEAN")
public class ActividadBoolean {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actividadBooleanSeq")
    @SequenceGenerator(name = "actividadBooleanSeq", sequenceName = "SEQ_OIDACTIVIDADBOOLEAN", allocationSize = 1)
    @Column(name = "OIDACTIVIDADBOOLEAN")
    private Integer oidActividadBoolean;

    @ManyToOne
    @JoinColumn(name = "OIDACTIVIDAD", nullable = false)
    private Actividad actividad;

    @ManyToOne
    @JoinColumn(name = "OIDEAVATRIBUTO")
    private EavAtributo eavAtributo;

    @Column(name = "VALOR", nullable = false)
    private Boolean valor;

    @Column(name = "FECHACREACION", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHAACTUALIZACION", nullable = false)
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    public ActividadBoolean () {}
    
    public ActividadBoolean(Actividad actividad, EavAtributo eavAtributo, Boolean valor) {
        this.actividad = actividad;
        this.eavAtributo = eavAtributo;
        this.valor = valor;
    }
}
