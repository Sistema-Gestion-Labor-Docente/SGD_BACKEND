package co.edu.unicauca.sgd.api.domain.materias;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class MateriaCorrequisitoId implements Serializable {

    @Column(name = "IDMATERIA_A")
    private Integer idMateriaA;

    @Column(name = "IDMATERIA_B")
    private Integer idMateriaB;

}
