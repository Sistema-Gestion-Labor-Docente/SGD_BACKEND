package co.edu.unicauca.sgd.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import co.edu.unicauca.sgd.api.enums.ContratacionEnum;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "SELECCIONADO")
@NoArgsConstructor
@AllArgsConstructor
public class Seleccionado {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seleccionadoSeq")
    @SequenceGenerator(name = "seleccionadoSeq", sequenceName = "SEQ_OIDSELECCIONADO", allocationSize = 1)
    @Column(name = "OIDSELECCIONADO")
    private Integer oidSeleccionado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OIDCALENDARIO", nullable = false)
    private Calendario calendario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OIDUSUARIO", nullable = false)
    private Usuario usuario;

    @Column(name = "TIPO", length = 50)
    private ContratacionEnum tipo;

    @CreationTimestamp
    @Column(name = "FECHACREACION", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @CreatedBy
    @Column(name = "USUARIOCREACION", nullable = false, length = 100, updatable = false)
    private String usuarioCreacion;

    @UpdateTimestamp
    @Column(name = "FECHAACTUALIZACION")
    private LocalDateTime fechaActualizacion;

    @LastModifiedBy
    @Column(name = "USUARIOACTUALIZACION", length = 100)
    private String usuarioActualizacion;

    // Constructores de conveniencia
    public Seleccionado(Integer oidSeleccionado) {
        this.oidSeleccionado = oidSeleccionado;
    }
}