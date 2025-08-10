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
@Table(name = "MATERIA")
public class Materia {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "materiaSeq")
  @SequenceGenerator(name = "materiaSeq", sequenceName = "SEQ_IDMATERIA", allocationSize = 1)
  @Column(name = "IDMATERIA")
  private Integer idMateria;

  @Column(name = "OIDMATERIA", nullable = false)
  private String oidMateria;

  @Column(name = "CODIGO", nullable = false)
  private String codigo;

  @Column(name = "NOMBRE", nullable = false)
  private String nombre;

  @Column(name = "SEMESTRE", nullable = false)
  private Integer semestre;

  @Column(name = "HORASSEMANA")
  private Integer horasSemana;

  @ManyToOne
  @JoinColumn(name = "OIDDEPARTAMENTO")
  private Departamento departamento;

  @ManyToOne
  @JoinColumn(name = "OIDPLAN", nullable = false)
  private Plan plan;

  @CreationTimestamp 
  @Column(name = "FECHACREACION", updatable = false, nullable = false)
  private LocalDateTime fechaCreacion;

  @Column(name = "USUARIOCREACION", updatable = false, nullable = false, length = 100)
  private String usuarioCreacion;

  @UpdateTimestamp 
  @Column(name = "FECHAACTUALIZACION")
  private LocalDateTime fechaActualizacion;

  @Column(name = "USUARIOACTUALIZACION", length = 100)
  private String usuarioActualizacion;

  /* Co-requisitos: pares simétricos (A<->B). Guardamos solo una fila por par. */
  @ManyToMany
  @JoinTable(
    name = "MATERIACORREQUISITO",
    joinColumns = @JoinColumn(name = "IDMATERIA_A", referencedColumnName = "IDMATERIA"),
    inverseJoinColumns = @JoinColumn(name = "IDMATERIA_B", referencedColumnName = "IDMATERIA")
  )
  @JsonIgnore
  private List<Materia> correquisitos;
}

