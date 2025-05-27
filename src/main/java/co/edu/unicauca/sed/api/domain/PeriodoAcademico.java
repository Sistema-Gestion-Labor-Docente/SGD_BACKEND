package co.edu.unicauca.sed.api.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "PERIODOACADEMICO")
@Data
public class PeriodoAcademico {

    public PeriodoAcademico() {
    }

    public PeriodoAcademico(Integer idPeriodoAcademico) {
        this.oidPeriodoAcademico = idPeriodoAcademico;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "periacadSeq")
    @SequenceGenerator(name = "periacadSeq", sequenceName = "SEQ_OIDPERIODOACADEMICO", allocationSize = 1)
    @Column(name = "OIDPERIODOACADEMICO")
    private Integer oidPeriodoAcademico;

    @ManyToOne
    @JoinColumn(name = "OIDESTADOPERIODOACADEMICO", nullable = false)
    private EstadoPeriodoAcademico estadoPeriodoAcademico;

    @Column(name = "IDPERIODO")
    private String idPeriodo;

    @Column(name = "FECHAINICIO")
    private LocalDate fechaInicio;

    @Column(name = "FECHAFIN")
    private LocalDate fechaFin;

    @Column(name = "IDPERIODOAPI", unique = true)
    private Integer idPeriodoApi;

    @Column(name = "FECHACREACION", updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHAACTUALIZACION", nullable = false)
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    @JsonIgnore
    @OneToMany(mappedBy = "oidPeriodoAcademico", cascade = CascadeType.REMOVE)
    private List<Proceso> procesosPeriodoAcademico;
}
