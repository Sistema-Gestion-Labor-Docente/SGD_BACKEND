package co.edu.unicauca.sed.api.domain;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "USUARIODETALLE")
@Data
public class UsuarioDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuarioDetalleSeq")
    @SequenceGenerator(name = "usuarioDetalleSeq", sequenceName = "SEQ_OIDUSUARIODETALLE", allocationSize = 1)
    @Column(name = "OIDUSUARIODETALLE", nullable = false)
    private Integer oidUsuarioDetalle;

    @Column(name = "FACULTAD", nullable = false)
    private String facultad;

    @Column(name = "DEPARTAMENTO")
    private String departamento;

    @Column(name = "PROGRAMA")
    private String programa;

    @Column(name = "CATEGORIA")
    private String categoria;

    @Column(name = "CONTRATACION")
    private String contratacion;

    @Column(name = "DEDICACION")
    private String dedicacion;

    @Column(name = "ESTUDIOS")
    private String estudios;

    @Column(name = "FECHACREACION", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHAACTUALIZACION", nullable = false)
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
