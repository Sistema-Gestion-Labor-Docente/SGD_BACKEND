package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entidad que representa el estado consolidado.
 */
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ESTADOCONSOLIDADO")
public class EstadoConsolidado {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "estadoConsolidadoSeq")
    @SequenceGenerator(name = "estadoConsolidadoSeq", sequenceName = "SEQ_OIDESTADOCONSOLIDADO", allocationSize = 1)
    @Column(name = "OIDESTADOCONSOLIDADO")
    private Integer oidEstadoConsolidado;

    @Column(name = "NOMBRE", nullable = false, length = 15)
    private String nombre;
}
