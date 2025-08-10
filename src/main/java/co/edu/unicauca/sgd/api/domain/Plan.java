package co.edu.unicauca.sgd.api.domain;

import java.time.LocalDate;
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
@Table(name = "PLAN") 
public class Plan {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "planSeq")
  @SequenceGenerator(name = "planSeq", sequenceName = "SEQ_OIDPLAN", allocationSize = 1)
  @Column(name = "OIDPLAN")
  private Integer oidPlan;

  @Column(name = "NUMERO", nullable = false, unique = true)
  private String numero;

  @Column(name = "ESTADO", nullable = false)
  private String estado;

  @Column(name = "FECHAAPROBACION")
  private LocalDate fechaAprobacion;

  @Column(name = "ACUERDO")
  private String acuerdo;

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

  @ManyToOne
  @JoinColumn(name = "OIDPROGRAMA", nullable = false)
  private Programa programa;

  @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  private List<Materia> materias;
}

