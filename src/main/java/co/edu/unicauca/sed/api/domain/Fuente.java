package co.edu.unicauca.sed.api.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "FUENTE")
@Data
public class Fuente {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fuenteSeq")
    @SequenceGenerator(name = "fuenteSeq", sequenceName = "SEQ_OIDFUENTE", allocationSize = 1)
    @Column(name = "OIDFUENTE", nullable = false)
    private Integer oidFuente;

    @Column(name = "TIPOFUENTE", nullable = false)
    private String tipoFuente;

    @Column(name = "CALIFICACION")
    private Float calificacion;

    @Column(name = "TIPOCALIFICACION", length = 15)
    private String tipoCalificacion;

    @Column(name = "NOMBREDOCUMENTOFUENTE")
    private String nombreDocumentoFuente;

    @Column(name = "NOMBREDOCUMENTOINFORME")
    private String nombreDocumentoInforme;

    @Column(name = "RUTADOCUMENTOFUENTE")
    private String rutaDocumentoFuente;

    @Column(name = "RUTADOCUMENTOINFORME")
    private String rutaDocumentoInforme;

    @Column(name = "OBSERVACION")
    private String observacion;

    @CreationTimestamp
    @Column(name = "FECHACREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHAACTUALIZACION")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    @ManyToOne
    @JoinColumn(name = "OIDACTIVIDAD", nullable = false)
    @JsonIgnore
    private Actividad actividad;

    @ManyToOne
    @JoinColumn(name = "OIDESTADOFUENTE", nullable = false)
    @JsonIgnore
    private EstadoFuente estadoFuente;

    @PrePersist
    @PreUpdate
    private void validarTipoCalificacion() {
        if (tipoCalificacion != null) {
            if (!tipoCalificacion.equals("EN_LINEA") && !tipoCalificacion.equals("DOCUMENTO")) {
                throw new IllegalArgumentException("TIPOCALIFICACION solo puede ser 'EN_LINEA' o 'DOCUMENTO'.");
            }
        }
    }
}
