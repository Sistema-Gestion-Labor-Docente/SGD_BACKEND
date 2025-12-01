package co.edu.unicauca.sgd.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.ActividadCalendario;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.CargoActividad;
import co.edu.unicauca.sgd.api.domain.TipoActividad;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioActividadCalendario;
import co.edu.unicauca.sgd.api.dto.AtributoDTO;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioUsuarioDTO;
import co.edu.unicauca.sgd.api.repository.UsuarioDepartamentoRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioActividadCalendarioMapperTest {

    @Mock
    private UsuarioDepartamentoRepository usuarioDepartamentoRepository;

    @Test
    void toResponse_conMismoCargoEnTodasLasRelaciones_exponeCargoEnActividadYUsuariosActividad() {
        UsuarioActividadCalendarioMapper mapper = new UsuarioActividadCalendarioMapper(usuarioDepartamentoRepository);

        Actividad actividad = buildActividad(1);
        Calendario calendario = buildCalendario(10);
        CargoActividad cargo = buildCargo(100, "PROFESOR");

        UsuarioActividadCalendario rel1 = buildRelacion(1, actividad, calendario, cargo, 4f);
        UsuarioActividadCalendario rel2 = buildRelacion(2, actividad, calendario, cargo, 6f);

        List<AtributoDTO> atributos = List.of();

        UsuarioActividadCalendarioDTOResponse dto = mapper.toResponse(
                actividad,
                List.of(rel1, rel2),
                calendario,
                atributos
        );

        assertNotNull(dto.getActividad());
        assertNotNull(dto.getActividad().getCargoActividad());
        assertEquals(100, dto.getActividad().getCargoActividad().getOidCargoActividad());

        List<UsuarioActividadCalendarioUsuarioDTO> usuariosActividad = dto.getUsuariosActividad();
        assertNotNull(usuariosActividad);
        assertEquals(2, usuariosActividad.size());

        UsuarioActividadCalendarioUsuarioDTO u1 = usuariosActividad.get(0);
        UsuarioActividadCalendarioUsuarioDTO u2 = usuariosActividad.get(1);

        assertEquals(1, u1.getOidUsuario());
        assertEquals(100, u1.getOidCargoActividad());
        assertEquals(4f, u1.getHoras());

        assertEquals(2, u2.getOidUsuario());
        assertEquals(100, u2.getOidCargoActividad());
        assertEquals(6f, u2.getHoras());
    }

    @Test
    void toResponse_conCargosDistintos_noExponeCargoEnActividadPeroSiPorUsuario() {
        UsuarioActividadCalendarioMapper mapper = new UsuarioActividadCalendarioMapper(usuarioDepartamentoRepository);

        Actividad actividad = buildActividad(2);
        Calendario calendario = buildCalendario(20);
        CargoActividad cargo1 = buildCargo(201, "COORDINADOR");
        CargoActividad cargo2 = buildCargo(202, "PROFESOR");

        UsuarioActividadCalendario rel1 = buildRelacion(5, actividad, calendario, cargo1, 3f);
        UsuarioActividadCalendario rel2 = buildRelacion(6, actividad, calendario, cargo2, 7f);

        UsuarioActividadCalendarioDTOResponse dto = mapper.toResponse(
                actividad,
                List.of(rel1, rel2),
                calendario,
                List.of()
        );

        // Como hay cargos distintos por usuario, el cargo a nivel de actividad debe ser null
        assertNotNull(dto.getActividad());
        assertNull(dto.getActividad().getCargoActividad());

        List<UsuarioActividadCalendarioUsuarioDTO> usuariosActividad = dto.getUsuariosActividad();
        assertNotNull(usuariosActividad);
        assertEquals(2, usuariosActividad.size());

        UsuarioActividadCalendarioUsuarioDTO u1 = usuariosActividad.get(0);
        UsuarioActividadCalendarioUsuarioDTO u2 = usuariosActividad.get(1);

        assertEquals(5, u1.getOidUsuario());
        assertEquals(201, u1.getOidCargoActividad());
        assertEquals(3f, u1.getHoras());

        assertEquals(6, u2.getOidUsuario());
        assertEquals(202, u2.getOidCargoActividad());
        assertEquals(7f, u2.getHoras());
    }

    private Actividad buildActividad(Integer oid) {
        Actividad actividad = new Actividad();
        actividad.setOidActividad(oid);
        TipoActividad tipo = new TipoActividad();
        tipo.setOidTipoActividad(9);
        tipo.setNombre("DOCENCIA");
        actividad.setTipoActividad(tipo);
        actividad.setNombreActividad("Actividad " + oid);
        actividad.setSemanas(4f);
        actividad.setFechaCreacion(LocalDateTime.now());
        actividad.setFechaActualizacion(LocalDateTime.now());
        return actividad;
    }

    private Calendario buildCalendario(Integer oid) {
        Calendario calendario = new Calendario();
        calendario.setOidcalendario(oid);
        calendario.setAnioCalendario("2024");
        calendario.setNumeroCalendario(1);
        return calendario;
    }

    private CargoActividad buildCargo(Integer oid, String nombre) {
        CargoActividad cargo = new CargoActividad();
        cargo.setOidCargoActividad(oid);
        cargo.setNombre(nombre);
        cargo.setMaxHorasSemana(10f);
        cargo.setMaxActividades(5);
        return cargo;
    }

    private UsuarioActividadCalendario buildRelacion(Integer oidUsuario,
                                                     Actividad actividad,
                                                     Calendario calendario,
                                                     CargoActividad cargo,
                                                     Float horas) {
        Usuario usuario = new Usuario();
        usuario.setOidUsuario(oidUsuario);
        usuario.setIdentificacion("ID-" + oidUsuario);
        usuario.setNombres("Nombre " + oidUsuario);
        usuario.setApellidos("Apellido " + oidUsuario);

        ActividadCalendario actividadCalendario = new ActividadCalendario();
        actividadCalendario.setActividad(actividad);
        actividadCalendario.setCalendario(calendario);

        UsuarioActividadCalendario relacion = new UsuarioActividadCalendario();
        relacion.setUsuario(usuario);
        relacion.setActividadCalendario(actividadCalendario);
        relacion.setCargoActividad(cargo);
        relacion.setHorasActividad(horas);
        return relacion;
    }
}

