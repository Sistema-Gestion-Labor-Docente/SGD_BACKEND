package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entidad que representa el estado de la etapa de desarrollo.
 */
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ESTADOETAPADESARROLLO")
public class EstadoEtapaDesarrollo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "estadoEtapaDesarrolloSeq")
    @SequenceGenerator(name = "estadoEtapaDesarrolloSeq", sequenceName = "SEQ_OIDESTADOETAPADESARROLLO", allocationSize = 1)
    @Column(name = "OIDESTADOETAPADESARROLLO")
    private Integer oidEstadoEtapaDesarrollo;

    @Column(name = "NOMBRE", nullable = false, length = 15)
    private String nombre;
}
