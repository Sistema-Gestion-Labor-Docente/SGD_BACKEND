package co.edu.unicauca.sed.api.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "OBJETIVODESARROLLOSOSTENIBLE")
public class ObjetivoDesarrolloSostenible {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "odsSeq")
    @SequenceGenerator(name = "odsSeq", sequenceName = "SEQ_OIDOBJETIVODESARROLLOSOSTE", allocationSize = 1)
    @Column(name = "OIDOBJETIVODESARROLLOSOSTE")
    private Integer oidObjetivoDesarrolloSostenible;

    @Column(name = "NOMBRE", nullable = false, length = 255)
    private String nombre;

    @Column(name = "DESCRIPCION", nullable = false, length = 255)
    private String descripcion;

    @Column(name = "ESTADO", nullable = false)
    private Boolean estado;
}
