package co.edu.unicauca.sgd.api.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "CALENDARIO")
public class Calendario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calendarioSeq")
    @SequenceGenerator(name = "calendarioSeq", sequenceName = "SEQ_OIDCALENDARIO", allocationSize = 1)
    @Column(name = "OIDCALENDARIO")
    private Integer oidcalendario;

    @Column(name = "ANIOCALENDARIO", nullable = false, length = 5)
    private String anioCalendario;

    @Column(name = "NUMEROCALENDARIO", nullable = false)
    private Integer numeroCalendario;

    @Column(name = "SEMANASCLASE")
    private Float semanasClase;

    @Column(name = "SEMANASPREPARACION")
    private Float semanasPreparacion;

    @Column(name = "HORASTOTALES")
    private Float horasTotales;

    @Column(name = "FECHACREACION", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "USUARIOCREACION", updatable = false, nullable = false, length = 100)
    private String usuarioCreacion;

    @Column(name = "FECHAACTUALIZACION")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    @Column(name = "USUARIOACTUALIZACION", length = 100)
    private String usuarioActualizacion;

    @Column(name = "ESTADO", nullable = false, length = 50)
    private String estado;

    @Column(name = "OBSERVACION", length = 1000)
    private String observacion;

    @OneToMany(mappedBy = "calendario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Fecha> fechas;

}
