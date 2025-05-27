package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * Entidad que representa un valor VARCHAR en la actividad del modelo EAV.
 */
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ACTIVIDADVARCHAR")
public class ActividadVarchar {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actividadVarcharSeq")
    @SequenceGenerator(name = "actividadVarcharSeq", sequenceName = "SEQ_OIDACTIVIDADVARCHAR", allocationSize = 1)
    @Column(name = "OIDACTIVIDADVARCHAR")
    private Integer oidActividadVarchar;

    @ManyToOne
    @JoinColumn(name = "OIDEAVATRIBUTO", nullable = false)
    private EavAtributo eavAtributo;

    @ManyToOne
    @JoinColumn(name = "OIDACTIVIDAD", nullable = false)
    private Actividad actividad;

    @Column(name = "VALOR", nullable = false, length = 255)
    private String valor;

    @Column(name = "FECHACREACION", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHAACTUALIZACION", nullable = false)
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    public ActividadVarchar() {}

    public ActividadVarchar(Actividad actividad, EavAtributo eavAtributo, String valor) {
        this.actividad = actividad;
        this.eavAtributo = eavAtributo;
        this.valor = valor;
    }
}
