package co.edu.unicauca.sgd.api.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "USUARIOACTIVIDADCALENDARIO")
@Data
public class UsuarioActividadCalendario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuarioActividadCalendarioSeq")
    @SequenceGenerator(name = "usuarioActividadCalendarioSeq", sequenceName = "SEQ_OIDUSUARIOACTIVIDADCALENDARIO", allocationSize = 1)
    @Column(name = "OIDUSUARIOACTIVIDADCALENDARIO")
    private Integer oidUsuarioActividadCalendario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OIDUSUARIO", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OIDACTIVIDAD", nullable = false)
    private Actividad actividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OIDCALENDARIO", nullable = false)
    private Calendario calendario;

    @Column(name = "FECHACREACION", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @CreatedBy
    @Column(name = "USUARIOCREACION", updatable = false, nullable = false, length = 100)
    private String usuarioCreacion;

    @Column(name = "FECHAACTUALIZACION")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    @LastModifiedBy
    @Column(name = "USUARIOACTUALIZACION", length = 100)
    private String usuarioActualizacion;
}