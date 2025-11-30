package co.edu.unicauca.sgd.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Fecha;
import co.edu.unicauca.sgd.api.domain.NombreFecha;
import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;

@DataJpaTest
class FechaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FechaRepository repository;

    @Test
    void findByCalendario_Oidcalendario_recuperaFechas() {
        Calendario cal = persistCalendario();
        NombreFecha nf = persistNombreFecha();
        Fecha f1 = persistFecha(cal, TipoFechaEnum.PLANIFICACION, nf);
        Fecha f2 = persistFecha(cal, TipoFechaEnum.PLANIFICACION, nf);

        List<Fecha> fechas = repository.findByCalendario_Oidcalendario(cal.getOidcalendario());

        assertThat(fechas).extracting(Fecha::getOidFecha)
                .containsExactlyInAnyOrder(f1.getOidFecha(), f2.getOidFecha());
    }

    @Test
    void countByCalendarioAndTipo_cuentaCorrectamente() {
        Calendario cal = persistCalendario();
        NombreFecha nf = persistNombreFecha();
        persistFecha(cal, TipoFechaEnum.PLANIFICACION, nf);
        persistFecha(cal, TipoFechaEnum.PLANIFICACION, nf);

        long total = repository.countByCalendario_OidcalendarioAndTipo(cal.getOidcalendario(), TipoFechaEnum.PLANIFICACION);

        assertThat(total).isEqualTo(2L);
    }

    @Test
    void findByCalendarioAndNombreFecha_buscaEspecifica() {
        Calendario cal = persistCalendario();
        NombreFecha nombreFecha = persistNombreFecha();
        Fecha fecha = persistFecha(cal, TipoFechaEnum.PLANIFICACION, nombreFecha);

        Optional<Fecha> result = repository.findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(
                cal.getOidcalendario(), nombreFecha.getOidNombreFecha());

        assertThat(result).isPresent();
        assertThat(result.get().getOidFecha()).isEqualTo(fecha.getOidFecha());
    }

    @Test
    void existsByCalendarioAndNombreFecha_devuelveTrueCuandoExiste() {
        Calendario cal = persistCalendario();
        NombreFecha nombreFecha = persistNombreFecha();
        persistFecha(cal, TipoFechaEnum.PLANIFICACION, nombreFecha);

        Boolean exists = repository.existsByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(
                cal.getOidcalendario(), nombreFecha.getOidNombreFecha());

        assertThat(exists).isTrue();
    }

    private Calendario persistCalendario() {
        Calendario cal = new Calendario();
        cal.setAnioCalendario("2024");
        cal.setNumeroCalendario(1);
        cal.setEstado("ACTIVO");
        cal.setUsuarioCreacion("test");
        return entityManager.persistFlushFind(cal);
    }

    private NombreFecha persistNombreFecha() {
        NombreFecha nombreFecha = new NombreFecha();
        nombreFecha.setNombre("PERIODO");
        nombreFecha.setUsuarioCreacion("test");
        return entityManager.persistFlushFind(nombreFecha);
    }

    private Fecha persistFecha(Calendario cal, TipoFechaEnum tipo, NombreFecha nombreFecha) {
        Fecha fecha = new Fecha();
        fecha.setCalendario(cal);
        fecha.setTipo(tipo);
        fecha.setNombreFecha(nombreFecha);
        fecha.setFechaInicial(LocalDateTime.now());
        fecha.setFechaCreacion(LocalDateTime.now());
        fecha.setUsuarioCreacion("test");
        return entityManager.persistFlushFind(fecha);
    }
}
