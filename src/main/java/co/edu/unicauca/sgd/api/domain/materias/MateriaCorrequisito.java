package co.edu.unicauca.sgd.api.domain.materias;

import co.edu.unicauca.sgd.api.domain.Materia;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MATERIACORREQUISITO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MateriaCorrequisito {

    @EmbeddedId
    private MateriaCorrequisitoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idMateriaA")
    @JoinColumn(name = "IDMATERIA_A")
    private Materia materiaA;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idMateriaB")
    @JoinColumn(name = "IDMATERIA_B")
    private Materia materiaB;
}
