package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ESTADOUSUARIO")
public class EstadoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "estadoUsuarioSeq")
    @SequenceGenerator(name = "estadoUsuarioSeq", sequenceName = "SEQ_OIDESTADOUSUARIO", allocationSize = 1)
    @Column(name = "OIDESTADOUSUARIO", nullable = false)
    private Integer oidEstadoUsuario;

    @Column(name = "NOMBRE", nullable = false)
    private String nombre;

    @Column(name = "FECHACREACION", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHAACTUALIZACION", nullable = false)
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
