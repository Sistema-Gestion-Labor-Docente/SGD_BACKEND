package co.edu.unicauca.sgd.api.domain;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "NOMBREFECHA")
public class NombreFecha {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nombreFechaSeq")
    @SequenceGenerator(name = "nombreFechaSeq", sequenceName = "SEQ_OIDNOMBREFECHA", allocationSize = 1)
    @Column(name = "OIDNOMBREFECHA")
    private Integer oidNombreFecha;

    @Column(name = "NOMBRE", nullable = false, unique = true, length = 255)
    private String nombre;

    @Column(name = "FECHACREACION", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "USUARIOCREACION", nullable = false, length = 100)
    private String usuarioCreacion;

    @Column(name = "FECHAACTUALIZACION")
    private LocalDateTime fechaActualizacion;

    @Column(name = "USUARIOACTUALIZACION", length = 100)
    private String usuarioActualizacion;
}