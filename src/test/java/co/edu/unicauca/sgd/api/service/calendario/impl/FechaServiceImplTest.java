package co.edu.unicauca.sgd.api.service.calendario.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Fecha;
import co.edu.unicauca.sgd.api.domain.NombreFecha;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTOResponse;
import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;
import co.edu.unicauca.sgd.api.mapper.FechaMapper;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.FechaRepository;
import co.edu.unicauca.sgd.api.repository.NombreFechaRepository;

@ExtendWith(MockitoExtension.class)
class FechaServiceImplTest {

    @Mock
    private FechaRepository fechaRepository;
    @Mock
    private CalendarioRepository calendarioRepository;
    @Mock
    private NombreFechaRepository nombreFechaRepository;
    @Mock
    private FechaMapper fechaMapper;

    private FechaServiceImpl fechaService;

    @BeforeEach
    void setUp() {
        fechaService = new FechaServiceImpl(fechaRepository, calendarioRepository, nombreFechaRepository, fechaMapper);
    }

    @Test
    void obtenerTodas_conResultadosDevuelveMensajePositivo() {
        Pageable pageable = PageRequest.of(0, 5);
        Fecha fecha = new Fecha();
        Page<Fecha> page = new PageImpl<>(List.of(fecha), pageable, 1);
        FechaDTOResponse dto = new FechaDTOResponse();

        when(fechaRepository.findAll(ArgumentMatchers.<Specification<Fecha>>any(), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(page);
        when(fechaMapper.toResponse(fecha)).thenReturn(dto);

        ApiResponse<Page<FechaDTOResponse>> response = fechaService.obtenerTodas(TipoFechaEnum.CLASES, pageable);

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(response.getMensaje()).isEqualTo("Fechas obtenidas correctamente");
        assertThat(response.getData().getContent()).containsExactly(dto);
    }

    @Test
    void obtenerTodas_sinResultadosDevuelveMensajeSinDatos() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Fecha> page = new PageImpl<>(List.of(), pageable, 0);

        when(fechaRepository.findAll(ArgumentMatchers.<Specification<Fecha>>any(), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(page);

        ApiResponse<Page<FechaDTOResponse>> response = fechaService.obtenerTodas(null, pageable);

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(response.getMensaje()).isEqualTo("No se encontraron fechas.");
        assertThat(response.getData().getTotalElements()).isZero();
    }

    @Test
    void guardar_DeberiaActualizarSemanasDelCalendarioAlRegistrarFinDeClases() {
        FechaDTORequest dto = new FechaDTORequest();
        dto.setOidCalendario(1);
        dto.setOidNombreFecha(7);
        dto.setTipo(TipoFechaEnum.CLASES);
        LocalDateTime finClases = LocalDateTime.of(2024, 6, 30, 0, 0);
        dto.setFechaInicial(finClases);

        Calendario calendario = buildCalendario(1, "2024", 1);
        NombreFecha nombreFecha = buildNombreFecha(7, "Fin de clases");

        Fecha nuevaFecha = new Fecha();
        nuevaFecha.setCalendario(calendario);
        nuevaFecha.setNombreFecha(nombreFecha);
        nuevaFecha.setFechaInicial(finClases);
        nuevaFecha.setTipo(TipoFechaEnum.CLASES);

        LocalDateTime inicioPeriodo = LocalDateTime.of(2024, 1, 8, 0, 0);
        Fecha inicioPeriodoFecha = new Fecha();
        inicioPeriodoFecha.setFechaInicial(inicioPeriodo);

        LocalDateTime inicioClases = LocalDateTime.of(2024, 2, 5, 0, 0);
        Fecha inicioClasesFecha = new Fecha();
        inicioClasesFecha.setFechaInicial(inicioClases);

        FechaDTOResponse responseDto = new FechaDTOResponse();

        when(fechaRepository.existsByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(1, 7)).thenReturn(false);
        when(calendarioRepository.findById(1)).thenReturn(Optional.of(calendario));
        when(nombreFechaRepository.findById(7)).thenReturn(Optional.of(nombreFecha));
        when(fechaRepository.findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(1, 1))
                .thenReturn(Optional.of(inicioPeriodoFecha));
        when(fechaRepository.findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(1, 10))
                .thenReturn(Optional.empty());
        when(fechaRepository.findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(1, 3))
                .thenReturn(Optional.of(inicioClasesFecha));
        when(fechaRepository.findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(1, 7))
                .thenReturn(Optional.of(nuevaFecha));
        when(fechaRepository.countByCalendario_OidcalendarioAndTipo(1, TipoFechaEnum.CLASES)).thenReturn(1L);
        when(fechaMapper.convertToEntity(dto, calendario, nombreFecha)).thenReturn(nuevaFecha);
        when(fechaRepository.save(nuevaFecha)).thenReturn(nuevaFecha);
        when(fechaMapper.toResponse(nuevaFecha)).thenReturn(responseDto);
        when(calendarioRepository.save(calendario)).thenReturn(calendario);

        ApiResponse<FechaDTOResponse> result = fechaService.guardar(dto);

        long semanasClaseEsperadas = ChronoUnit.WEEKS.between(inicioClases.toLocalDate(), finClases.toLocalDate()) + 1;
        long semanasPrepEsperadas = ChronoUnit.WEEKS.between(inicioPeriodo.toLocalDate(), finClases.toLocalDate());

        assertThat(result.getCodigo()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(responseDto);
        assertThat(calendario.getSemanasClase()).isEqualTo((float) semanasClaseEsperadas);
        assertThat(calendario.getSemanasPreparacion()).isEqualTo((float) semanasPrepEsperadas);

        verify(fechaRepository).save(nuevaFecha);
        verify(calendarioRepository).save(calendario);
    }

    @Test
    void guardar_DeberiaRetornarErrorCuandoElAnioNoCoincide() {
        FechaDTORequest dto = new FechaDTORequest();
        dto.setOidCalendario(5);
        dto.setOidNombreFecha(1);
        dto.setTipo(TipoFechaEnum.ADMINISTRATIVAS);
        dto.setFechaInicial(LocalDateTime.of(2025, 1, 10, 0, 0));

        Calendario calendario = buildCalendario(5, "2024", 2);
        NombreFecha nombreFecha = buildNombreFecha(1, "Inicio periodo");

        when(fechaRepository.existsByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(5, 1)).thenReturn(false);
        when(calendarioRepository.findById(5)).thenReturn(Optional.of(calendario));

        ApiResponse<FechaDTOResponse> result = fechaService.guardar(dto);

        assertThat(result.getCodigo()).isEqualTo(400);
        assertThat(result.getMensaje()).contains("fechaInicial");
        verify(fechaRepository, never()).save(any());
    }

    @Test
    void eliminar_NoPermiteFechasEspeciales() {
        NombreFecha nombreFecha = buildNombreFecha(1, "Inicio de periodo");
        Fecha fecha = new Fecha();
        fecha.setNombreFecha(nombreFecha);

        when(fechaRepository.findById(30)).thenReturn(Optional.of(fecha));

        ApiResponse<Void> response = fechaService.eliminar(30);

        assertThat(response.getCodigo()).isEqualTo(400);
        assertThat(response.getMensaje()).contains("No se permite eliminar");
        verify(fechaRepository, never()).deleteById(3);
    }

    @Test
    void eliminar_DebeBorrarFechasRegulares() {
        NombreFecha nombreFecha = buildNombreFecha(99, "Fecha regular");
        Fecha fecha = new Fecha();
        fecha.setNombreFecha(nombreFecha);

        when(fechaRepository.findById(8)).thenReturn(Optional.of(fecha));

        ApiResponse<Void> response = fechaService.eliminar(8);

        assertThat(response.getCodigo()).isEqualTo(200);
        assertThat(response.getMensaje()).contains("eliminada");
        verify(fechaRepository).deleteById(8);
    }

    @Test
    void buscarPorId_DebeRetornar404CuandoNoExiste() {
        when(fechaRepository.findById(15)).thenReturn(Optional.empty());

        ApiResponse<FechaDTOResponse> response = fechaService.buscarPorId(15);

        assertThat(response.getCodigo()).isEqualTo(404);
        assertThat(response.getMensaje()).contains("Fecha no encontrada");
    }

    private Calendario buildCalendario(int oid, String anio, int numero) {
        Calendario calendario = new Calendario();
        calendario.setOidcalendario(oid);
        calendario.setAnioCalendario(anio);
        calendario.setNumeroCalendario(numero);
        return calendario;
    }

    private NombreFecha buildNombreFecha(int oid, String nombre) {
        NombreFecha nombreFecha = new NombreFecha();
        nombreFecha.setOidNombreFecha(oid);
        nombreFecha.setNombre(nombre);
        return nombreFecha;
    }
}
