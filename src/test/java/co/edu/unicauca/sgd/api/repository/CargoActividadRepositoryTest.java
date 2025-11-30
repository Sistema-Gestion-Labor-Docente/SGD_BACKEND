package co.edu.unicauca.sgd.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import co.edu.unicauca.sgd.api.domain.CargoActividad;
import co.edu.unicauca.sgd.api.domain.TipoActividad;

@DataJpaTest
class CargoActividadRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CargoActividadRepository repository;

    @Test
    void findByTipoActividad_OidTipoActividad_filtraPorTipo() {
        TipoActividad tipo1 = persistTipoActividad("T1");
        TipoActividad tipo2 = persistTipoActividad("T2");

        persistCargo("C1", tipo1);
        persistCargo("C2", tipo2);

        List<CargoActividad> resultado =
                repository.findByTipoActividad_OidTipoActividad(tipo1.getOidTipoActividad());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipoActividad().getOidTipoActividad())
                .isEqualTo(tipo1.getOidTipoActividad());
    }

    private TipoActividad persistTipoActividad(String nombre) {
        TipoActividad tipo = new TipoActividad();
        tipo.setNombre(nombre);
        tipo.setDescripcion(nombre + " desc");
        return entityManager.persistFlushFind(tipo);
    }

    private CargoActividad persistCargo(String nombre, TipoActividad tipo) {
        CargoActividad cargo = new CargoActividad();
        cargo.setNombre(nombre);
        cargo.setTipo("TIPO");
        cargo.setMaxHorasSemana(10f);
        cargo.setTipoActividad(tipo);
        cargo.setUsuarioCreacion("test");
        return entityManager.persistFlushFind(cargo);
    }
}

