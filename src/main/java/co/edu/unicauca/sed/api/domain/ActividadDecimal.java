package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * Entidad que representa un valor decimal en la actividad del modelo EAV.
 */
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ACTIVIDADDECIMAL")
public class ActividadDecimal {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actividadDecimalSeq")
    @SequenceGenerator(name = "actividadDecimalSeq", sequenceName = "SEQ_OIDACTIVIDADDECIMAL", allocationSize = 1)
    @Column(name = "OIDACTIVIDADDECIMAL")
    private Integer oidActividadDecimal;

    @ManyToOne
    @JoinColumn(name = "OIDACTIVIDAD", nullable = false)
    private Actividad actividad;

    @ManyToOne
    @JoinColumn(name = "OIDEAVATRIBUTO", nullable = false)
    private EavAtributo eavAtributo;

    @Column(name = "VALOR", nullable = false)
    private Float valor;

    @Column(name = "FECHACREACION", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHAACTUALIZACION", nullable = false)
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    public ActividadDecimal () {}
    
    public ActividadDecimal(Actividad actividad, EavAtributo eavAtributo, Float valor) {
        this.actividad = actividad;
        this.eavAtributo = eavAtributo;
        this.valor = valor;
    }
}
