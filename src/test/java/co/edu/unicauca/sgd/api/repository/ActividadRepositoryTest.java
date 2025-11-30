package co.edu.unicauca.sgd.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.EstadoActividad;
import co.edu.unicauca.sgd.api.domain.TipoActividad;

@DataJpaTest
class ActividadRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ActividadRepository repository;

    @Test
    void findByTipoActividad_Nombre_filtraPorNombre() {
        TipoActividad docencia = persistTipoActividad("DOCENCIA");
        TipoActividad investigacion = persistTipoActividad("INVESTIGACION");

        persistActividad("A1", docencia);
        persistActividad("A2", investigacion);

        Page<Actividad> page = repository.findByTipoActividad_Nombre("DOCENCIA", PageRequest.of(0, 10));

        List<Actividad> contenido = page.getContent();
        assertThat(contenido).hasSize(1);
        assertThat(contenido.get(0).getTipoActividad().getNombre()).isEqualTo("DOCENCIA");
    }

    private TipoActividad persistTipoActividad(String nombre) {
        TipoActividad tipo = new TipoActividad();
        tipo.setNombre(nombre);
        tipo.setDescripcion(nombre + " desc");
        return entityManager.persistFlushFind(tipo);
    }

    private Actividad persistActividad(String nombre, TipoActividad tipo) {
        EstadoActividad estado = new EstadoActividad();
        estado.setNombre("ACTIVO");
        estado = entityManager.persistFlushFind(estado);

        Actividad actividad = new Actividad();
        actividad.setNombreActividad(nombre);
        actividad.setTipoActividad(tipo);
        actividad.setEstadoActividad(estado);
        return entityManager.persistFlushFind(actividad);
    }
}
