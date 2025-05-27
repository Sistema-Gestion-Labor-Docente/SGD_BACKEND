package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * Entidad que representa un Atributo en el modelo EAV.
 */
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "EAVATRIBUTO")
public class EavAtributo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "eavAtributoSeq")
    @SequenceGenerator(name = "eavAtributoSeq", sequenceName = "SEQ_OIDEAVATRIBUTO", allocationSize = 1)
    @Column(name = "OIDEAVATRIBUTO")
    private Integer oideavAtributo;

    @Column(name = "NOMBRE", nullable = false, unique = true, length = 255)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPODATO", nullable = false)
    private TipoDato tipoDato;

    @Column(name = "FECHACREACION", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHAACTUALIZACION", nullable = false)
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    /**
     * Tipos de datos permitidos según la restricción en la base de datos.
     */
    public enum TipoDato {
        VARCHAR, INT, FLOAT, DATE, BOOLEAN
    }
    
}
