package co.edu.unicauca.sed.api.domain;

import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "USUARIO")
@Data
public class Usuario {

    public Usuario() {
    }
    
    public Usuario(Integer oidUsuario) {
        this.oidUsuario = oidUsuario;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuarioSeq")
    @SequenceGenerator(name = "usuarioSeq", sequenceName = "SEQ_OIDUSUARIO", allocationSize = 1)
    @Column(name = "OIDUSUARIO", nullable = false)
    private Integer oidUsuario;

    @ManyToOne
    @JoinColumn(name = "OIDUSUARIODETALLE", nullable = false)
    private UsuarioDetalle usuarioDetalle;

    @ManyToOne
    @JoinColumn(name = "OIDESTADOUSUARIO")
    private EstadoUsuario estadoUsuario;

    @Column(name = "IDENTIFICACION", nullable = false)
    private String identificacion;

    @Column(name = "NOMBRES", nullable = false)
    private String nombres;

    @Column(name = "APELLIDOS", nullable = false)
    private String apellidos;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "CORREO", nullable = false)
    private String correo;

    @Column(name = "FECHACREACION")
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "ULTIMOINGRESO")
    @UpdateTimestamp
    private LocalDateTime ultimoIngreso;

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "ROLUSUARIO", joinColumns = @JoinColumn(name = "OIDUSUARIO"), inverseJoinColumns = @JoinColumn(name = "OIDROL"))
    private List<Rol> roles;

    @JsonIgnore
    @OneToMany(mappedBy = "evaluador", cascade = CascadeType.REMOVE)
    private List<Proceso> procesosEvaluados;

    @JsonIgnore
    @OneToMany(mappedBy = "evaluado", cascade = CascadeType.REMOVE)
    private List<Proceso> procesosEvaluado;
}
