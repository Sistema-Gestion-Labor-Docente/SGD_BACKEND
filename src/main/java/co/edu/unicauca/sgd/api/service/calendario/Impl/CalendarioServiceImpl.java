package co.edu.unicauca.sgd.api.service.calendario.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTOResponse;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTOResponse;
import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;
import co.edu.unicauca.sgd.api.mapper.CalendarioMapper;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.FechaRepository;
import co.edu.unicauca.sgd.api.service.calendario.CalendarioService;
import co.edu.unicauca.sgd.api.service.calendario.FechaService;
import co.edu.unicauca.sgd.api.specification.CalendarioSpecs;
import co.edu.unicauca.sgd.api.utils.StringUtils;

@Service
public class CalendarioServiceImpl implements CalendarioService {

    private static final Logger logger = LoggerFactory.getLogger(CalendarioServiceImpl.class);

    private static final List<Integer> OIDS_FECHAS_RESALTADAS = List.of(
        1, // Inicio del periodo
        2, // Matrículas académicas estudiantes regulares
        3, // Inicio de clases
        4, // Plazo máximo para presentar solicitudes ...
        5, // Registro de Notas 70% en SIMCA
        6, // Evaluación docente {identificador del período}
        7, // Finalización de clases
        8, // Plazo máximo para finales...
        9, // Cierre de SIMCA para registro de calificaciones
        10 // Finalización de periodo académico {identificador del período}
    );

    private CalendarioRepository calendarioRepository;

    private CalendarioMapper calendarioMapper;

    private FechaService fechaService;

    private FechaRepository fechaRepository;

    public CalendarioServiceImpl(
            CalendarioRepository calendarioRepository,
            CalendarioMapper calendarioMapper,
            FechaService fechaService,
            FechaRepository fechaRepository) {
        this.calendarioRepository = calendarioRepository;
        this.calendarioMapper = calendarioMapper;
        this.fechaService = fechaService;
        this.fechaRepository = fechaRepository;
    }

    @Override
    public ApiResponse<Page<CalendarioDTOResponse>> obtenerTodos(String anioCalendario, Integer numeroCalendario, String estado, Pageable pageable) {
        try {
            Specification<Calendario> spec = Specification
                    .where(CalendarioSpecs.anioEq(anioCalendario))
                    .and(CalendarioSpecs.numeroEq(numeroCalendario))
                    .and(CalendarioSpecs.estadoEq(estado));

            Page<Calendario> calendarios = calendarioRepository.findAll(spec, pageable);
            Page<CalendarioDTOResponse> responsePage = calendarios.map(calendarioMapper::toResponse);

            logger.info("Calendarios encontrados: {}", responsePage.getTotalElements());
            return new ApiResponse<>(200, "Calendarios encontrados correctamente.", responsePage);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al recuperar los calendarios: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<CalendarioDTOResponse> buscarPorId(Integer oid) {
        try {
            Calendario calendario = calendarioRepository.findById(oid)
                    .orElseThrow(() -> new RuntimeException("Calendario no encontrado con ID: " + oid));

            CalendarioDTOResponse dto = calendarioMapper.toResponse(calendario);

            List<FechaDTOResponse> fechas = fechaRepository.findByCalendario_Oidcalendario(oid).stream()
                    .map(fecha -> {
                        FechaDTOResponse fechaDto = new FechaDTOResponse();
                        fechaDto.setOidFecha(fecha.getOidFecha());
                        fechaDto.setOidNombreFecha(fecha.getNombreFecha().getOidNombreFecha());
                        fechaDto.setNombre(fecha.getNombreResuelto());
                        fechaDto.setFechaInicial(fecha.getFechaInicial());
                        fechaDto.setFechaFin(fecha.getFechaFin());
                        fechaDto.setTipo(fecha.getTipo());
                        fechaDto.setOidCalendario(fecha.getCalendario().getOidcalendario());
                        return fechaDto;
                    })
                    .toList();
            dto.setFechas(fechas);

            logger.info("Calendario encontrado con ID: {}", oid);

            return new ApiResponse<>(200, "Calendario encontrado correctamente.", dto);
        } catch (RuntimeException e) {
            return new ApiResponse<>(404, "Calendario no encontrado: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno al recuperar el calendario: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<CalendarioDTOResponse> guardar(CalendarioDTORequest request) {
        try {
            Calendario calendario = calendarioMapper.convertToEntity(request);
            calendario.setUsuarioCreacion(
                StringUtils.hasText(calendario.getUsuarioCreacion()) ? calendario.getUsuarioCreacion() : "Usuario"
            );
            calendario.setEstado(
                StringUtils.hasText(calendario.getEstado()) ? calendario.getEstado() : "PENDIENTE"
            );
            Calendario guardado = calendarioRepository.save(calendario);

            if (guardado != null) {
                crearFechasResaltadasIniciales(guardado.getOidcalendario());
            }

            CalendarioDTOResponse dto = calendarioMapper.toResponse(guardado);

            logger.info("Calendario guardado con ID: {}", guardado.getOidcalendario());
            return new ApiResponse<>(201, "Calendario guardado correctamente.", dto);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al guardar el calendario: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<CalendarioDTOResponse> actualizar(Integer oid, CalendarioDTORequest request) {
        try {
            Calendario existente = calendarioRepository.findById(oid)
                    .orElseThrow(() -> new RuntimeException("Calendario no encontrado con ID: " + oid));

            calendarioMapper.actualizarCamposBasicos(existente, request);
            existente.setUsuarioActualizacion("UsuarioActualizacion");

            Calendario actualizado = calendarioRepository.save(existente);
            CalendarioDTOResponse dto = calendarioMapper.toResponse(actualizado);

            logger.info("Calendario actualizado con ID: {}", oid);
            return new ApiResponse<>(200, "Calendario actualizado correctamente.", dto);
        } catch (RuntimeException e) {
            return new ApiResponse<>(400, "Error en la actualización: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno al actualizar el calendario: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            if (!calendarioRepository.existsById(oid)) {
                return new ApiResponse<>(404, "Calendario no encontrado con ID: " + oid, null);
            }
            calendarioRepository.deleteById(oid);
            logger.info("Calendario eliminado con ID: {}", oid);
            return new ApiResponse<>(204, "Calendario eliminado correctamente.", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar el calendario: " + e.getMessage(), null);
        }
    }

    /* Funciones auxiliares */

    /**
     * Crea, para el calendario dado, todas las fechas con tipo RESALTADAS
     * usando los IDs definidos en OIDS_FECHAS_RESALTADAS.
     */
    private void crearFechasResaltadasIniciales(Integer oidCalendario) {
        logger.info("Creando fechas resaltadas iniciales para calendario ID: {}", oidCalendario);

        for (Integer oidNombreFecha : OIDS_FECHAS_RESALTADAS) {
            FechaDTORequest fecha = new FechaDTORequest();
            fecha.setOidCalendario(oidCalendario);
            fecha.setOidNombreFecha(oidNombreFecha);
            if (oidNombreFecha.equals(3) || oidNombreFecha.equals(7)) {
                fecha.setTipo(TipoFechaEnum.CLASES);
            } else {
                fecha.setTipo(TipoFechaEnum.RESALTADAS);
            }

            try {
                fechaService.guardar(fecha);
                logger.debug("Fecha resaltada creada (oidNombreFecha={}): calendario={}", oidNombreFecha, oidCalendario);
            } catch (Exception e) {
                // Continuamos con las demás para no abortar todo el proceso
                logger.error("No se pudo crear la fecha (oidNombreFecha={}): {}", oidNombreFecha, e.getMessage());
            }
        }
        logger.info("Fechas resaltadas iniciales creadas para calendario ID: {}", oidCalendario);
    }
}

