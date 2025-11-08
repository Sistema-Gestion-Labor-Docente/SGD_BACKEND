package co.edu.unicauca.sgd.api.service.calendario.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.Seleccionado;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioDepartamento;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTOResponse;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTOResponse;
import co.edu.unicauca.sgd.api.enums.ContratacionEnum;
import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;
import co.edu.unicauca.sgd.api.mapper.CalendarioMapper;
import co.edu.unicauca.sgd.api.mapper.FechaMapper;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.DepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.FechaRepository;
import co.edu.unicauca.sgd.api.repository.SeleccionadoRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioDepartamentoRepository;
import co.edu.unicauca.sgd.api.service.calendario.CalendarioPdfService;
import co.edu.unicauca.sgd.api.service.calendario.CalendarioService;
import co.edu.unicauca.sgd.api.service.calendario.FechaService;
import co.edu.unicauca.sgd.api.specification.CalendarioSpecs;
import co.edu.unicauca.sgd.api.utils.StringUtils;

@Service
public class CalendarioServiceImpl implements CalendarioService {

    private static final Logger logger = LoggerFactory.getLogger(CalendarioServiceImpl.class);

    private static final List<Integer> OIDS_FECHAS_INICIALES = List.of(
        1, // Inicio del periodo
        2, // Matriculas academicas estudiantes regulares
        3, // Inicio de clases
        4, // Plazo maximo para presentar solicitudes ...
        5, // Registro de Notas 70% en SIMCA
        6, // Evaluacion docente {identificador del periodo}
        7, // Finalizacion de clases
        8, // Plazo maximo para finales...
        9, // Cierre de SIMCA para registro de calificaciones
        10, // Finalizacion de periodo academico {identificador del periodo}
        26, 
        27, 
        28
    );

    private final CalendarioRepository calendarioRepository;

    private final CalendarioMapper calendarioMapper;

    private final FechaService fechaService;

    private final FechaRepository fechaRepository;

    private final FechaMapper fechaMapper;

    private final SeleccionadoRepository seleccionadoRepository;

    private final DepartamentoRepository departamentoRepository;

    private final UsuarioDepartamentoRepository usuarioDepartamentoRepository;

    private final CalendarioPdfService calendarioPdfService;

    public CalendarioServiceImpl(
            CalendarioRepository calendarioRepository,
            CalendarioMapper calendarioMapper,
            FechaService fechaService,
            FechaRepository fechaRepository,
            SeleccionadoRepository seleccionadoRepository,
            DepartamentoRepository departamentoRepository,
            UsuarioDepartamentoRepository usuarioDepartamentoRepository,
            FechaMapper fechaMapper,
            CalendarioPdfService calendarioPdfService) {
        this.calendarioRepository = calendarioRepository;
        this.calendarioMapper = calendarioMapper;
        this.fechaService = fechaService;
        this.fechaRepository = fechaRepository;
        this.seleccionadoRepository = seleccionadoRepository;
        this.departamentoRepository = departamentoRepository;
        this.usuarioDepartamentoRepository = usuarioDepartamentoRepository;
        this.fechaMapper = fechaMapper;
        this.calendarioPdfService = calendarioPdfService;
    }

    private List<FechaDTOResponse> obtenerFechasDto(Integer oidCalendario) {
        if (oidCalendario == null) {
            return List.of();
        }
        return fechaRepository.findByCalendario_Oidcalendario(oidCalendario)
                .stream()
                .map(fechaMapper::toResponse)
                .toList();
    }

    @Override
    public ApiResponse<Page<CalendarioDTOResponse>> obtenerTodos(String anioCalendario, Integer numeroCalendario, String estado, Pageable pageable) {
        try {
            Specification<Calendario> spec = Specification
                    .where(CalendarioSpecs.anioEq(anioCalendario))
                    .and(CalendarioSpecs.numeroEq(numeroCalendario))
                    .and(CalendarioSpecs.estadoEq(estado));

            Page<Calendario> calendarios = calendarioRepository.findAll(spec, pageable);
            Page<CalendarioDTOResponse> responsePage = calendarios.map(calendario ->
                    calendarioMapper.toResponse(calendario, obtenerFechasDto(calendario.getOidcalendario())));

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

            CalendarioDTOResponse dto = calendarioMapper.toResponse(calendario, obtenerFechasDto(calendario.getOidcalendario()));

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
                crearSeleccionadosIniciales(guardado);
            }

            CalendarioDTOResponse dto = calendarioMapper.toResponse(guardado, obtenerFechasDto(guardado.getOidcalendario()));

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

            if (request.getAnioCalendario() != null && !request.getAnioCalendario().equals(existente.getAnioCalendario())) {
                throw new RuntimeException("El anio (anio) no es editable.");
            }
            if (request.getNumeroCalendario() != null && !request.getNumeroCalendario().equals(existente.getNumeroCalendario())) {
                throw new RuntimeException("El numero (numero) no es editable.");
            }

            calendarioMapper.actualizarCamposBasicos(existente, request);
            existente.setUsuarioActualizacion("UsuarioActualizacion");

            Calendario actualizado = calendarioRepository.save(existente);
            CalendarioDTOResponse dto = calendarioMapper.toResponse(actualizado, obtenerFechasDto(actualizado.getOidcalendario()));

            logger.info("Calendario actualizado con ID: {}", oid);
            return new ApiResponse<>(200, "Calendario actualizado correctamente.", dto);
        } catch (RuntimeException e) {
            return new ApiResponse<>(400, "Error en la actualizacion: " + e.getMessage(), null);
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

    @Override
    public ByteArrayOutputStream generarCalendarioPdf(Integer oidCalendario) throws IOException {
        if (oidCalendario == null) {
            throw new IllegalArgumentException("El identificador del calendario es obligatorio.");
        }
        logger.info("Generando PDF del calendario ID: {}", oidCalendario);
        Calendario calendario = calendarioRepository.findById(oidCalendario)
                .orElseThrow(() -> new IllegalArgumentException("Calendario no encontrado con ID: " + oidCalendario));
        List<FechaDTOResponse> fechas = obtenerFechasDto(oidCalendario);
        try {
            return calendarioPdfService.generarCalendarioPdf(calendario, fechas);
        } catch (IOException e) {
            logger.error("Error generando el PDF del calendario {}", oidCalendario, e);
            throw e;
        }
    }

    /* Funciones auxiliares */

    /**
     * Crea, para el calendario dado, todas las fechas con tipo RESALTADAS
     * usando los IDs definidos en OIDS_FECHAS_RESALTADAS.
     */
    private void crearFechasResaltadasIniciales(Integer oidCalendario) {
        logger.info("Creando fechas resaltadas iniciales para calendario ID: {}", oidCalendario);

        for (Integer oidNombreFecha : OIDS_FECHAS_INICIALES) {
            FechaDTORequest fecha = new FechaDTORequest();
            fecha.setOidCalendario(oidCalendario);
            fecha.setOidNombreFecha(oidNombreFecha);
            if (oidNombreFecha.equals(3) || oidNombreFecha.equals(7)) {
                fecha.setTipo(TipoFechaEnum.CLASES);
            } else if (oidNombreFecha.equals(26)) {
                fecha.setTipo(TipoFechaEnum.OCASIONAL);
            } else if (oidNombreFecha.equals(27)) {
                fecha.setTipo(TipoFechaEnum.CATEDRA);
            } else if (oidNombreFecha.equals(28)) {
                fecha.setTipo(TipoFechaEnum.BECARIO_Y_PRACTICANTE);
            } else {
                fecha.setTipo(TipoFechaEnum.RESALTADAS);
            }

            try {
                fechaService.guardar(fecha);
                logger.debug("Fecha resaltada creada (oidNombreFecha={}): calendario={}", oidNombreFecha, oidCalendario);
            } catch (Exception e) {
                // Continuamos con las demas para no abortar todo el proceso
                logger.error("No se pudo crear la fecha (oidNombreFecha={}): {}", oidNombreFecha, e.getMessage());
            }
        }
        logger.info("Fechas resaltadas iniciales creadas para calendario ID: {}", oidCalendario);
    }

    /**
     * Crea listas de seleccionados iniciales para cada departamento.
     * Regla: todos los usuarios del departamento que NO tengan roles ESTUDIANTE, SECRETARIA, FACULTAD, DECANO.
     */
    private void crearSeleccionadosIniciales(Calendario calendario) {
        logger.info("Creando listas de seleccionados iniciales para calendario ID: {}", calendario.getOidcalendario());

        // Roles a excluir (case-insensitive)
        final List<String> rolesExcluidos = List.of("ESTUDIANTE", "SECRETARIA", "FACULTAD", "DECANO");

        // Obtenemos todos los departamentos
        List<Departamento> departamentos = departamentoRepository.findAll();

        for (Departamento dept : departamentos) {
            try {
                // Obtenemos las relaciones UsuarioDepartamento para el departamento
                List<UsuarioDepartamento> uds = usuarioDepartamentoRepository.findByDepartamento(dept);

                if (uds == null || uds.isEmpty()) {
                    logger.debug("Departamento sin usuarios, saltando: {}", dept);
                    continue;
                }

                for (UsuarioDepartamento ud : uds) {
                    Usuario usuario = ud.getUsuario();
                    if (usuario == null) {
                        logger.debug("UsuarioDepartamento sin usuario asociado, salto. ud={}", ud);
                        continue;
                    }

                    // Verificamos roles: si tiene algun rol excluido, no lo seleccionamos
                    boolean tieneRolExcluido = usuario.getRoles() != null
                            && usuario.getRoles().stream()
                                .map(r -> r.getNombre() == null ? "" : r.getNombre().toUpperCase())
                                .anyMatch(rolesExcluidos::contains);

                    if (tieneRolExcluido) {
                        // No lo incluimos
                        continue;
                    }

                    // Evitar duplicados por (calendario, usuario)
                    try {
                        Integer oidCalendario = calendario.getOidcalendario();
                        Integer oidUsuario = usuario.getOidUsuario();

                        if (seleccionadoRepository.existsByCalendarioOidcalendarioAndUsuarioOidUsuario(oidCalendario, oidUsuario)) {
                            logger.debug("Seleccionado ya existe (calendario={}, usuario={}), skip", oidCalendario, oidUsuario);
                            continue;
                        }

                        // Crear seleccionado (usamos objeto Calendario y Usuario ya existentes para persistencia)
                        Seleccionado s = new Seleccionado();
                        s.setCalendario(calendario);
                        s.setUsuario(usuario);

                        // Default para TIPO: PLANTA (ajusta si prefieres otra logica)
                        s.setTipo(ContratacionEnum.PLANTA);

                        // UsuarioCreacion: usamos quien creo el calendario si esta, si no "SYSTEM"
                        s.setUsuarioCreacion(StringUtils.hasText(calendario.getUsuarioCreacion()) ? calendario.getUsuarioCreacion() : "SYSTEM");

                        seleccionadoRepository.save(s);
                        logger.debug("Seleccionado creado: calendario={}, usuario={}", oidCalendario, oidUsuario);
                    } catch (Exception exUsuario) {
                        // Capturamos por usuario para no abortar todo el proceso
                        logger.error("No se pudo crear seleccionado para usuario {} en departamento {}: {}",
                                usuario.getOidUsuario(), dept, exUsuario.getMessage());
                    }
                }
            } catch (Exception exDept) {
                // Capturamos por departamento para que un fallo no detenga los demas
                logger.error("Error creando seleccionados para departamento {}: {}", dept, exDept.getMessage());
            }
        }

        logger.info("Terminado de crear listas de seleccionados para calendario ID: {}", calendario.getOidcalendario());
    }
}










