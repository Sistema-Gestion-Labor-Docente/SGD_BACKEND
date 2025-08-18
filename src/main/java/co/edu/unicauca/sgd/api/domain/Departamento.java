package co.edu.unicauca.sgd.api.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "DEPARTAMENTO")
public class Departamento {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "departamentoSeq")
  @SequenceGenerator(name = "departamentoSeq", sequenceName = "SEQ_OIDDEPARTAMENTO", allocationSize = 1)
  @Column(name = "OIDDEPARTAMENTO")
  private Integer oidDepartamento;

  @Column(name = "NOMBRE", nullable = false, unique = true)
  private String nombre;

  @Column(name = "FACULTAD", nullable = false)
  private String facultad;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "JEFE_OIDUSUARIO")
  private Usuario jefe;

  @CreationTimestamp
  @Column(name = "FECHACREACION", updatable = false, nullable = false)
  private LocalDateTime fechaCreacion;

  @CreatedBy
  @Column(name = "USUARIOCREACION", updatable = false, nullable = false, length = 100)
  private String usuarioCreacion;

  @UpdateTimestamp
  @Column(name = "FECHAACTUALIZACION")
  private LocalDateTime fechaActualizacion;

  @LastModifiedBy
  @Column(name = "USUARIOACTUALIZACION", length = 100)
  private String usuarioActualizacion;

  @OneToMany(mappedBy = "departamento") @JsonIgnore
  private List<Materia> materias;

  public Departamento() {}
  public Departamento(Integer oidDepartamento) { this.oidDepartamento = oidDepartamento; }
}

