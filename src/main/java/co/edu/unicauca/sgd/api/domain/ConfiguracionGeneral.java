package co.edu.unicauca.sgd.api.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "CONFIGURACIONGENERAL")
public class ConfiguracionGeneral {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "configGeneralSeq")
    @SequenceGenerator(name = "configGeneralSeq", sequenceName = "SEQ_OIDCONFIGGENERAL", allocationSize = 1)
    @Column(name = "OIDCONFIGGENERAL")
    private Integer oidConfigGeneral;

    @Column(name = "CLAVE", nullable = false, unique = true, length = 150)
    private String clave;

    @Lob
    @Column(name = "VALOR")
    private String valor;

    @Column(name = "HABILITADO", nullable = false)
    private boolean habilitado = true;

    @CreationTimestamp
    @Column(name = "FECHACREACION", updatable = false, nullable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "FECHAACTUALIZACION", nullable = false)
    private LocalDateTime fechaActualizacion;
}
