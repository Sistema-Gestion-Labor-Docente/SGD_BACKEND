package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad que representa la carga de la labor docente asociada a un usuario en un periodo académico.
 */
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "LABORDOCENTE")
public class LaborDocente {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "laborDocenteSeq")
    @SequenceGenerator(name = "laborDocenteSeq", sequenceName = "SEQ_OIDLABORDOCENTE", allocationSize = 1)
    @Column(name = "OIDLABORDOCENTE")
    private Integer oidLaborDocente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "OIDPERIODOACADEMICO", nullable = false)
    private PeriodoAcademico periodoAcademico;

    @ManyToOne(optional = false)
    @JoinColumn(name = "OIDUSUARIO", nullable = false)
    private Usuario usuario;

    @Column(name = "RUTADOCUMENTO", nullable = false, length = 255)
    private String rutaDocumento;

    @Column(name = "NOMBREDOCUMENTO", nullable = false, length = 255)
    private String nombreDocumento;

    @Column(name = "FECHACREACION", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHAACTUALIZACION", nullable = false)
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
