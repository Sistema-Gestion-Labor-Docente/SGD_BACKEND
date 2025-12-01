package co.edu.unicauca.sgd.api.service.calendario.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.sgd.api.client.ClienteNotificacion;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.Fecha;
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
import co.edu.unicauca.sgd.api.exception.calendario.CalendarioConsultaException;
import co.edu.unicauca.sgd.api.exception.calendario.CalendarioNoEncontradoException;
import co.edu.unicauca.sgd.api.exception.calendario.CalendarioOperacionNoPermitidaException;
import co.edu.unicauca.sgd.api.exception.calendario.CalendarioProcesoException;
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

    private final ClienteNotificacion clienteNotificacion;

    @Value("${spring.notification.habilitada:false}")
    private boolean notificacionHabilitada;

    public CalendarioServiceImpl(
            CalendarioRepository calendarioRepository,
            CalendarioMapper calendarioMapper,
            FechaService fechaService,
            FechaRepository fechaRepository,
            SeleccionadoRepository seleccionadoRepository,
            DepartamentoRepository departamentoRepository,
            UsuarioDepartamentoRepository usuarioDepartamentoRepository,
            FechaMapper fechaMapper,
            CalendarioPdfService calendarioPdfService,
            ClienteNotificacion clienteNotificacion) {
        this.calendarioRepository = calendarioRepository;
        this.calendarioMapper = calendarioMapper;
        this.fechaService = fechaService;
        this.fechaRepository = fechaRepository;
        this.seleccionadoRepository = seleccionadoRepository;
        this.departamentoRepository = departamentoRepository;
        this.usuarioDepartamentoRepository = usuarioDepartamentoRepository;
        this.fechaMapper = fechaMapper;
        this.calendarioPdfService = calendarioPdfService;
        this.clienteNotificacion = clienteNotificacion;
    }

    private List<FechaDTOResponse> obtenerFechasDto(Integer oidCalendario) {
        if (oidCalendario == null) {
            return List.of();
        }
        return fechaRepository.findByCalendario_OidcalendarioOrderByFechaInicialAsc(oidCalendario)
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
            boolean hasContent = responsePage.hasContent();
            String message = hasContent
                    ? "Calendarios encontrados correctamente."
                    : "No se encontraron calendarios.";
            return new ApiResponse<>(200, message, responsePage);
        } catch (Exception e) {
            CalendarioConsultaException ex =
                    new CalendarioConsultaException("Error al recuperar los calendarios", e);
            return new ApiResponse<>(500, ex.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<CalendarioDTOResponse> buscarPorId(Integer oid) {
        try {
            Calendario calendario = calendarioRepository.findById(oid)
                    .orElseThrow(() -> new CalendarioNoEncontradoException(oid));

            CalendarioDTOResponse dto = calendarioMapper.toResponse(calendario, obtenerFechasDto(calendario.getOidcalendario()));

            logger.info("Calendario encontrado con ID: {}", oid);

            return new ApiResponse<>(200, "Calendario encontrado correctamente.", dto);
        } catch (CalendarioNoEncontradoException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            CalendarioConsultaException ex =
                    new CalendarioConsultaException("Error interno al recuperar el calendario", e);
            return new ApiResponse<>(500, ex.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<CalendarioDTOResponse> guardar(CalendarioDTORequest request) {
        try {
            Calendario calendario = calendarioMapper.convertToEntity(request);

            if (calendarioRepository.existsByAnioCalendarioAndNumeroCalendario(
                    calendario.getAnioCalendario(), calendario.getNumeroCalendario())) {
                throw new CalendarioOperacionNoPermitidaException(
                        "Ya existe un calendario para el año " + calendario.getAnioCalendario()
                        + " y número " + calendario.getNumeroCalendario() + ".");
            }

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
        } catch (CalendarioOperacionNoPermitidaException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (DataIntegrityViolationException e) {
            String msg = "Ya existe un calendario para el año " + request.getAnioCalendario()
                    + " y número " + request.getNumeroCalendario() + ".";
            logger.warn("Violación de integridad al guardar calendario duplicado: {}", e.getMessage());
            return new ApiResponse<>(400, msg, null);
        } catch (Exception e) {
            CalendarioProcesoException ex =
                    new CalendarioProcesoException("Error al guardar el calendario", e);
            return new ApiResponse<>(500, ex.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<CalendarioDTOResponse> actualizar(Integer oid, CalendarioDTORequest request) {
        try {
            Calendario existente = calendarioRepository.findById(oid)
                    .orElseThrow(() -> new CalendarioNoEncontradoException(oid));

            String estadoAnterior = existente.getEstado() != null ? existente.getEstado().toUpperCase() : null;

            if (request.getAnioCalendario() != null && !request.getAnioCalendario().equals(existente.getAnioCalendario())) {
                throw new CalendarioOperacionNoPermitidaException("El anio (anio) no es editable.");
            }
            if (request.getNumeroCalendario() != null && !request.getNumeroCalendario().equals(existente.getNumeroCalendario())) {
                throw new CalendarioOperacionNoPermitidaException("El numero (numero) no es editable.");
            }

            calendarioMapper.actualizarCamposBasicos(existente, request);
            existente.setUsuarioActualizacion("UsuarioActualizacion");

            Calendario actualizado = calendarioRepository.save(existente);
            CalendarioDTOResponse dto = calendarioMapper.toResponse(actualizado, obtenerFechasDto(actualizado.getOidcalendario()));

            logger.info("Calendario actualizado con ID: {}", oid);

            // Notificar cuando el calendario pasa de PENDIENTE a APROBADO
            String estadoNuevo = actualizado.getEstado() != null ? actualizado.getEstado().toUpperCase() : null;
            if (notificacionHabilitada
                    && "PENDIENTE".equals(estadoAnterior)
                    && "APROBADO".equals(estadoNuevo)) {
                notificarActivacionCalendario(actualizado, "El calendario ha sido aprobado");
            }
            return new ApiResponse<>(200, "Calendario actualizado correctamente.", dto);
        } catch (CalendarioOperacionNoPermitidaException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (CalendarioNoEncontradoException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            CalendarioProcesoException ex =
                    new CalendarioProcesoException("Error interno al actualizar el calendario", e);
            return new ApiResponse<>(500, ex.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            if (!calendarioRepository.existsById(oid)) {
                throw new CalendarioNoEncontradoException(oid);
            }
            calendarioRepository.deleteById(oid);
            logger.info("Calendario eliminado con ID: {}", oid);
            return new ApiResponse<>(204, "Calendario eliminado correctamente.", null);
        } catch (CalendarioNoEncontradoException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            CalendarioProcesoException ex =
                    new CalendarioProcesoException("Error al eliminar el calendario", e);
            return new ApiResponse<>(500, ex.getMessage(), null);
        }
    }

    /**
     * Tarea programada que revisa diariamente las fechas de cada calendario
     * y realiza transiciones automaticas de estado:
     * <ul>
     *   <li>Si el calendario esta APROBADO y la fecha actual esta entre
     *       la fecha de inicio (OIDNOMBREFECHA = 1) y la ultima fecha registrada
     *       (inclusive), pasa a ACTIVO.</li>
     *   <li>Si el calendario esta ACTIVO y la fecha actual es posterior
     *       a la ultima fecha registrada, pasa a DESHABILITADO.</li>
     * </ul>
     * Estados como PENDIENTE u otros se gestionan por otros flujos y no se modifican aqui.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void actualizarEstadosCalendariosPorFechas() {
        LocalDateTime ahora = LocalDateTime.now();

        List<Calendario> calendarios = calendarioRepository.findAll();
        for (Calendario calendario : calendarios) {
            try {
                String estadoActual = calendario.getEstado() != null ? calendario.getEstado().toUpperCase() : "";

                // Solo gestionamos automatico para estados APROBADO o ACTIVO
                if (!"APROBADO".equals(estadoActual) && !"ACTIVO".equals(estadoActual)) {
                    continue;
                }

                Integer oidCalendario = calendario.getOidcalendario();

                // Fecha de inicio: "Inicio del periodo" (OIDNOMBREFECHA = 1)
                LocalDateTime fechaInicio = fechaRepository
                        .findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(oidCalendario, 1)
                        .map(Fecha::getFechaInicial)
                        .orElse(null);

                List<Fecha> fechas = fechaRepository.findByCalendario_Oidcalendario(oidCalendario);
                if (fechas == null || fechas.isEmpty() || fechaInicio == null) {
                    continue;
                }

                // Última fecha agregada: se toma el máximo entre FECHAFIN y FECHAINICIAL
                LocalDateTime ultimaFecha = fechas.stream()
                        .map(f -> f.getFechaFin() != null ? f.getFechaFin() : f.getFechaInicial())
                        .filter(d -> d != null)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);

                if (ultimaFecha == null) {
                    continue;
                }

                String nuevoEstado = null;

                boolean dentroDeRango = (ahora.isAfter(fechaInicio) || ahora.isEqual(fechaInicio))
                        && (ahora.isBefore(ultimaFecha) || ahora.isEqual(ultimaFecha));

                // APROBADO -> ACTIVO cuando ya esta dentro del rango de fechas
                if ("APROBADO".equals(estadoActual) && dentroDeRango) {
                    nuevoEstado = "ACTIVO";
                }

                // ACTIVO -> DESHABILITADO cuando ya paso la ultima fecha
                if ("ACTIVO".equals(estadoActual) && ahora.isAfter(ultimaFecha)) {
                    nuevoEstado = "DESHABILITADO";
                }

                if (nuevoEstado != null && !nuevoEstado.equalsIgnoreCase(estadoActual)) {
                    calendario.setEstado(nuevoEstado);
                    calendario.setUsuarioActualizacion("SYSTEM_SCHEDULER");
                    Calendario actualizado = calendarioRepository.save(calendario);

                    logger.info("Estado de calendario {} actualizado de {} a {}",
                            oidCalendario, estadoActual, nuevoEstado);

                    // Notificar cuando se activa un calendario
                    if (notificacionHabilitada && "APROBADO".equals(estadoActual) && "ACTIVO".equals(nuevoEstado)) {
                        notificarActivacionCalendario(actualizado, "Nuevo calendario académico activo");
                    }
                }
            } catch (Exception e) {
                logger.error("Error actualizando estado para calendario {}: {}",
                        calendario.getOidcalendario(), e.getMessage());
            }
        }
    }

    @Override
    public ByteArrayOutputStream generarCalendarioPdf(Integer oidCalendario) throws IOException {
        if (oidCalendario == null) {
            throw new CalendarioOperacionNoPermitidaException("El identificador del calendario es obligatorio.");
        }
        logger.info("Generando PDF del calendario ID: {}", oidCalendario);
        Calendario calendario = calendarioRepository.findById(oidCalendario)
                .orElseThrow(() -> new CalendarioNoEncontradoException(oidCalendario));
        List<FechaDTOResponse> fechas = obtenerFechasDto(oidCalendario).stream()
                .filter(f -> f.getTipo() == TipoFechaEnum.RESALTADAS
                        || f.getTipo() == TipoFechaEnum.NO_RESALTADAS
                        || f.getTipo() == TipoFechaEnum.ADMINISTRATIVAS
                        || f.getTipo() == TipoFechaEnum.CLASES)
                .toList();
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
        final List<String> rolesExcluidos = List.of("ESTUDIANTE", "SECRETARIO", "FACULTAD", "DECANO");

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
                        if (usuario.getUsuarioDetalle() != null) {
                            s.setDedicacion(usuario.getUsuarioDetalle().getDedicacion());
                        }

                        s.setTipo(determinarTipoContratacion(usuario));

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

    private ContratacionEnum determinarTipoContratacion(Usuario usuario) {
        String contratacion = (usuario != null && usuario.getUsuarioDetalle() != null)
                ? usuario.getUsuarioDetalle().getContratacion()
                : null;
        ContratacionEnum tipo = parseContratacion(contratacion);
        return tipo != null ? tipo : ContratacionEnum.PLANTA;
    }

    private ContratacionEnum parseContratacion(String contratacion) {
        if (!StringUtils.hasText(contratacion)) {
            return null;
        }
        String normalizado = normalizarEtiqueta(contratacion);
        return Arrays.stream(ContratacionEnum.values())
                .filter(valor -> normalizado.equals(normalizarEtiqueta(valor.name()))
                        || normalizado.equals(normalizarEtiqueta(valor.getValor())))
                .findFirst()
                .orElse(null);
    }

    private String normalizarEtiqueta(String valor) {
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[\\s_]+", "")
                .toUpperCase();
    }

    /**
     * Envía notificaciones por correo a secretarios, jefes, coordinadores y profesores
     * cuando un calendario es aprobado o pasa a estado ACTIVO.
     */
    private void notificarActivacionCalendario(Calendario calendario, String evento) {
        try {
            // Reutilizamos la lógica de seleccionados: contiene los docentes asociados al calendario
            List<Seleccionado> seleccionados = seleccionadoRepository.findAll().stream()
                    .filter(s -> s.getCalendario() != null
                            && calendario.getOidcalendario().equals(s.getCalendario().getOidcalendario())
                            && s.getUsuario() != null
                            && StringUtils.hasText(s.getUsuario().getCorreo()))
                    .toList();

            if (seleccionados.isEmpty()) {
                logger.warn("No hay seleccionados para enviar notificación del calendario {}", calendario.getOidcalendario());
                return;
            }

            List<String> correos = seleccionados.stream()
                    .map(s -> s.getUsuario().getCorreo())
                    .distinct()
                    .toList();

            String asunto = String.format("Calendario %s %s - %s",
                    calendario.getAnioCalendario(),
                    calendario.getNumeroCalendario(),
                    evento);

            String mensaje = String.format(
                    "Se informa que el calendario académico %s - %s ha cambiado de estado a '%s'.",
                    calendario.getAnioCalendario(),
                    calendario.getNumeroCalendario(),
                    calendario.getEstado()
            );

            clienteNotificacion.enviarNotificacion(correos, asunto, mensaje);
        } catch (Exception e) {
            logger.error("Error al enviar notificaciones para el calendario {}: {}",
                    calendario.getOidcalendario(), e.getMessage());
        }
    }
}










