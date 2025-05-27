package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * Entidad que representa un valor entero en la actividad del modelo EAV.
 */
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ACTIVIDADINT")
public class ActividadInt {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actividadIntSeq")
    @SequenceGenerator(name = "actividadIntSeq", sequenceName = "SEQ_OIDACTIVIDADINT", allocationSize = 1)
    @Column(name = "OIDACTIVIDADINT")
    private Integer oidActividadInt;

    @ManyToOne
    @JoinColumn(name = "OIDEAVATRIBUTO", nullable = false)
    private EavAtributo eavAtributo;

    @ManyToOne
    @JoinColumn(name = "OIDACTIVIDAD", nullable = false)
    private Actividad actividad;

    @Column(name = "VALOR", nullable = false)
    private Integer valor;

    @Column(name = "FECHACREACION", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHAACTUALIZACION", nullable = false)
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    public ActividadInt() {}

    public ActividadInt(Actividad actividad, EavAtributo eavAtributo, Integer valor) {
        this.actividad = actividad;
        this.eavAtributo = eavAtributo;
        this.valor = valor;
    }
}
