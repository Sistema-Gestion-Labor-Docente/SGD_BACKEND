package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "TIPOACTIVIDAD")
@Data
public class TipoActividad {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tipoActividadSeq")
    @SequenceGenerator(name = "tipoActividadSeq", sequenceName = "SEQ_OIDTIPOACTIVIDAD", allocationSize = 1)
    @Column(name = "OIDTIPOACTIVIDAD")
    private Integer oidTipoActividad;

    @Column(name = "NOMBRE", nullable = false)
    private String nombre;

    @Column(name = "DESCRIPCION", nullable = false)
    private String descripcion;
}
