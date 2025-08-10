package co.edu.unicauca.sgd.api.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "USUARIODEPARTAMENTO")
public class UsuarioDepartamento {

  @Id // PK en OIDUSUARIO para garantizar un único departamento por usuario
  @Column(name = "OIDUSUARIO")
  private Integer oidUsuario;

  @MapsId
  @ManyToOne
  @JoinColumn(name = "OIDUSUARIO", referencedColumnName = "OIDUSUARIO", insertable = false, updatable = false)
  private Usuario usuario;

  @ManyToOne
  @JoinColumn(name = "OIDDEPARTAMENTO", nullable = false)
  private Departamento departamento;

  @CreationTimestamp
  @Column(name = "FECHACREACION", nullable = false, updatable = false)
  private LocalDateTime fechaCreacion;
}

