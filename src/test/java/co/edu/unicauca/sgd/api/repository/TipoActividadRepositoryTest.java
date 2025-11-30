package co.edu.unicauca.sgd.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import co.edu.unicauca.sgd.api.domain.TipoActividad;

@DataJpaTest
class TipoActividadRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TipoActividadRepository repository;

    @Test
    void findDistinctNombre_retornarNombresSinDuplicados() {
        persistTipoActividad("DOCENCIA");
        persistTipoActividad("INVESTIGACION");
        persistTipoActividad("DOCENCIA");

        List<String> nombres = repository.findDistinctNombre();

        assertThat(nombres)
                .contains("DOCENCIA", "INVESTIGACION");
    }

    @Test
    void findByNombreIgnoreCase_buscaSinImportarMayusculas() {
        persistTipoActividad("DOCENCIA");

        Optional<TipoActividad> result = repository.findByNombreIgnoreCase("docencia");

        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("DOCENCIA");
    }

    private TipoActividad persistTipoActividad(String nombre) {
        TipoActividad tipo = new TipoActividad();
        tipo.setNombre(nombre);
        tipo.setDescripcion(nombre + " desc");
        return entityManager.persistFlushFind(tipo);
    }
}

