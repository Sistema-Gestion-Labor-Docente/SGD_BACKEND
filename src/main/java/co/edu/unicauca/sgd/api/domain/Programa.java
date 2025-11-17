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
@Table(name = "PROGRAMA") 
public class Programa {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "programaSeq")
  @SequenceGenerator(name = "programaSeq", sequenceName = "SEQ_OIDPROGRAMA", allocationSize = 1)
  @Column(name = "OIDPROGRAMA")
  private Integer oidPrograma;

  @Column(name = "NOMBRE", nullable = false, unique = true)
  private String nombre;

  @Column(name = "NOMBRE_CORTO", nullable = false)
  private String nombreCorto;

  @Column(name = "CODIGOKIRA", unique = true)
  private String codigoKira;

  @OneToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "COORDINADOR_OIDUSUARIO", unique = true)
  private Usuario coordinador;

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

  @OneToMany(mappedBy = "programa") @JsonIgnore
  private List<Plan> planes;
}
