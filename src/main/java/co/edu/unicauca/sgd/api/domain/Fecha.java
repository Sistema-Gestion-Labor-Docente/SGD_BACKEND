package co.edu.unicauca.sgd.api.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "FECHA")
public class Fecha {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fechaSeq")
    @SequenceGenerator(name = "fechaSeq", sequenceName = "SEQ_OIDFECHA", allocationSize = 1)
    @Column(name = "OIDFECHA")
    private Integer oidFecha;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OIDNOMBREFECHA", nullable = false)
    private NombreFecha nombreFecha;

    @Column(name = "FECHAINICIAL")
    private LocalDateTime fechaInicial;

    @Column(name = "FECHAFIN")
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false)
    private TipoFechaEnum tipo;

    @CreatedDate
    @Column(name = "FECHACREACION", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @CreatedBy
    @Column(name = "USUARIOCREACION", nullable = false, updatable = false, length = 100)
    private String usuarioCreacion;

    @LastModifiedDate
    @Column(name = "FECHAACTUALIZACION")
    private LocalDateTime fechaActualizacion;

    @LastModifiedBy
    @Column(name = "USUARIOACTUALIZACION", length = 100)
    private String usuarioActualizacion;

    @ManyToOne
    @JoinColumn(name = "OIDCALENDARIO", nullable = false, updatable = false)
    private Calendario calendario;

    @Transient
    public String getNombreResuelto() {
        String periodo = getPeriodo();
        String base = this.getNombreFecha().getNombre();
        return base
            .replace("{calendar}", periodo)
            .replace("{identificador del período}", periodo);
    }

    @Transient
    public String getPeriodo() {
        return this.calendario.getAnioCalendario() + " - " + this.calendario.getNumeroCalendario();
    }

    @Transient
    public boolean isUniqueDate() {
        return this.nombreFecha != null && this.nombreFecha.isUniqueDate();
    }
}
