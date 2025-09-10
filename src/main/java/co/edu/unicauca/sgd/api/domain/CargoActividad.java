package co.edu.unicauca.sgd.api.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "CARGO_ACTIVIDAD")
@Data
public class CargoActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cargoActividadSeq")
    @SequenceGenerator(name = "cargoActividadSeq", sequenceName = "SEQ_OIDCARGOACTIVIDAD", allocationSize = 1)
    @Column(name = "OIDCARGOACTIVIDAD")
    private Integer oidCargoActividad;

    @Column(name = "NOMBRE", nullable = false, length = 255)
    private String nombre;

    @Column(name = "TIPO", nullable = false, length = 30)
    private String tipo;

    @Column(name = "MAXHORASSEMANA", nullable = false)
    private Float maxHorasSemana;

    @ManyToOne
    @JoinColumn(name = "OIDTIPOACTIVIDAD", nullable = false)
    private TipoActividad tipoActividad;

    @Column(name = "FECHACREACION", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "USUARIOCREACION", nullable = false, updatable = false, length = 100)
    private String usuarioCreacion;

    @Column(name = "FECHAACTUALIZACION")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    @Column(name = "USUARIOACTUALIZACION", length = 100)
    private String usuarioActualizacion;
}

