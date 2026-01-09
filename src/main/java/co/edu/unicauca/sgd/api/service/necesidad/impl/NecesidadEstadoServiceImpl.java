package co.edu.unicauca.sgd.api.service.necesidad.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import co.edu.unicauca.sgd.api.client.ClienteNotificacion;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.Materia;
import co.edu.unicauca.sgd.api.domain.Necesidad;
import co.edu.unicauca.sgd.api.domain.Plan;
import co.edu.unicauca.sgd.api.domain.Programa;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;
import co.edu.unicauca.sgd.api.exception.necesidad.NecesidadCalendarioObligatorioException;
import co.edu.unicauca.sgd.api.exception.necesidad.NecesidadException;
import co.edu.unicauca.sgd.api.exception.necesidad.NecesidadListaOidInvalidaException;
import co.edu.unicauca.sgd.api.exception.necesidad.NecesidadNotFoundException;
import co.edu.unicauca.sgd.api.exception.necesidad.NecesidadTransicionDepartamentoObligatorioException;
import co.edu.unicauca.sgd.api.exception.necesidad.NecesidadTransicionNoPermitidaException;
import co.edu.unicauca.sgd.api.exception.necesidad.NecesidadTransicionProgramaObligatorioException;
import co.edu.unicauca.sgd.api.exception.necesidad.NecesidadValidationException;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.AsignacionRepository;
import co.edu.unicauca.sgd.api.repository.NecesidadRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioRepository;
import co.edu.unicauca.sgd.api.service.necesidad.NecesidadEstadoService;

@Service
public class NecesidadEstadoServiceImpl implements NecesidadEstadoService {

    private static final Logger logger = LoggerFactory.getLogger(NecesidadEstadoServiceImpl.class);
    private static final String ROL_SECRETARIA_FACULTAD = "SECRETARIA/O FACULTAD";

    private final NecesidadRepository necesidadRepository;
    private final CalendarioRepository calendarioRepository;
    private final ClienteNotificacion clienteNotificacion;
    private final UsuarioRepository usuarioRepository;
    private final AsignacionRepository asignacionRepository;

    public NecesidadEstadoServiceImpl(NecesidadRepository necesidadRepository,
                                      CalendarioRepository calendarioRepository,
                                      ClienteNotificacion clienteNotificacion,
                                      UsuarioRepository usuarioRepository,
                                      AsignacionRepository asignacionRepository) {
        this.necesidadRepository = necesidadRepository;
        this.calendarioRepository = calendarioRepository;
        this.clienteNotificacion = clienteNotificacion;
        this.usuarioRepository = usuarioRepository;
        this.asignacionRepository = asignacionRepository;
    }

    @Override
    @Transactional
    public ApiResponse<Map<String, Object>> cambiarEstadoMasivo(Integer oidCalendario,
                                                                EstadoNecesidad estadoOrigen,
                                                                EstadoNecesidad estadoDestino,
                                                                Integer oidPrograma,
                                                                Integer oidDepartamento,
                                                                List<Integer> oidNecesidades) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("calendario", oidCalendario);
        metadata.put("estadoOrigen", estadoOrigen != null ? estadoOrigen.name() : null);
        metadata.put("estadoDestino", estadoDestino != null ? estadoDestino.name() : null);
        if (oidNecesidades != null) {
            metadata.put("necesidadesSolicitadas", oidNecesidades);
        }
        if (oidPrograma != null) {
            metadata.put("oidPrograma", oidPrograma);
        }
        if (oidDepartamento != null) {
            metadata.put("oidDepartamento", oidDepartamento);
        }

        try {
            Calendario calendario = validarCalendario(oidCalendario);

            if (!esTransicionPermitida(estadoOrigen, estadoDestino)) {
                throw new NecesidadTransicionNoPermitidaException();
            }

            if (requierePrograma(estadoOrigen, estadoDestino) && oidPrograma == null) {
                throw new NecesidadTransicionProgramaObligatorioException();
            }

            if (requiereDepartamento(estadoOrigen, estadoDestino) && oidDepartamento == null) {
                throw new NecesidadTransicionDepartamentoObligatorioException();
            }

            List<Necesidad> necesidades = obtenerNecesidadesParaTransicion(
                    oidCalendario, estadoOrigen, oidPrograma, oidDepartamento);

            List<Integer> oidsFiltrados = oidNecesidades == null ? List.of() : oidNecesidades.stream()
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (!oidsFiltrados.isEmpty()) {
                Set<Integer> filtro = new HashSet<>(oidsFiltrados);
                List<Necesidad> filtradas = necesidades.stream()
                        .filter(necesidad -> filtro.contains(necesidad.getOidNecesidad()))
                        .collect(Collectors.toList());
                if (filtradas.isEmpty()) {
                    metadata.put("totalNecesidades", 0);
                    metadata.put("faltantes", oidsFiltrados);
                    throw new NecesidadNotFoundException("No se encontraron necesidades para la transición solicitada.");
                }
                Set<Integer> encontrados = filtradas.stream()
                        .map(Necesidad::getOidNecesidad)
                        .collect(Collectors.toSet());
                List<Integer> faltantes = oidsFiltrados.stream()
                        .filter(id -> !encontrados.contains(id))
                        .collect(Collectors.toList());
                if (!faltantes.isEmpty()) {
                    metadata.put("faltantes", faltantes);
                }
                necesidades = filtradas;
            }
            if (necesidades.isEmpty()) {
                metadata.put("totalNecesidades", 0);
                throw new NecesidadNotFoundException("No se encontraron necesidades para la transición solicitada.");
            }

            boolean debeValidarDepartamento = EstadoNecesidad.EN_REVISION_JEFE.equals(estadoDestino);
            boolean requiereValidarSinAsignaciones = EstadoNecesidad.NO_ASIGNADA.equals(estadoOrigen)
                    && EstadoNecesidad.EN_REVISION_JEFE.equals(estadoDestino);

            Map<String, List<Integer>> inconsistencias = validarRequisitosPrevios(necesidades, debeValidarDepartamento);
            if (!inconsistencias.getOrDefault("sinGrupo", List.of()).isEmpty()
                    || !inconsistencias.getOrDefault("sinCupo", List.of()).isEmpty()) {
                return new ApiResponse<>(400,
                        "Todas las necesidades deben tener grupo y cupo definidos antes de cambiar de estado.",
                        Map.of("inconsistencias", inconsistencias));
            }
            if (debeValidarDepartamento && !inconsistencias.getOrDefault("sinDepartamento", List.of()).isEmpty()) {
                return new ApiResponse<>(400,
                        "Cada necesidad debe tener un departamento asociado antes de pasar a EN REVISION JEFE.",
                        Map.of("inconsistencias", inconsistencias));
            }
            if (requiereValidarSinAsignaciones) {
                Map<String, List<Integer>> relaciones = validarSinAsignacionesSeleccionados(necesidades);
                if (!relaciones.getOrDefault("conAsignaciones", List.of()).isEmpty()
                        || !relaciones.getOrDefault("conSeleccionados", List.of()).isEmpty()) {
                    return new ApiResponse<>(400,
                            "No se puede reabrir necesidades con asignaciones o seleccionados asociados.",
                            Map.of("inconsistencias", relaciones));
                }
            }

            necesidades.forEach(necesidad -> {
                necesidad.setEstado(estadoDestino);
                necesidad.setUsuarioActualizacion("system");
            });
            necesidadRepository.saveAll(necesidades);
            notificarCambioEstado(necesidades, calendario, estadoOrigen, estadoDestino);

            metadata.put("totalNecesidades", necesidades.size());
            return new ApiResponse<>(200, "Estados actualizados correctamente.", metadata);
        } catch (NecesidadException e) {
            logger.warn("Error al cambiar estados masivamente: {}", e.getMessage());
            return new ApiResponse<>(e.getStatus().value(), e.getMessage(), metadata);
        } catch (Exception e) {
            logger.error("Error interno al cambiar estados masivamente", e);
            return new ApiResponse<>(500, "Error al cambiar el estado de las necesidades: " + e.getMessage(), metadata);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Map<String, Object>> cambiarEstadoPorOids(List<Integer> oidNecesidades,
                                                                 EstadoNecesidad estadoOrigen,
                                                                 EstadoNecesidad estadoDestino) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("estadoOrigen", estadoOrigen != null ? estadoOrigen.name() : null);
        metadata.put("estadoDestino", estadoDestino != null ? estadoDestino.name() : null);
        metadata.put("necesidadesSolicitadas", oidNecesidades);

        try {
            if (oidNecesidades == null || oidNecesidades.isEmpty()) {
                throw new NecesidadListaOidInvalidaException();
            }
            if (!esTransicionPermitida(estadoOrigen, estadoDestino)) {
                throw new NecesidadValidationException("Transición de estado no permitida.");
            }

            List<Integer> oidSinDuplicados = oidNecesidades.stream()
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (oidSinDuplicados.isEmpty()) {
                throw new NecesidadListaOidInvalidaException();
            }

            List<Necesidad> necesidades = necesidadRepository.findAllById(oidSinDuplicados);
            if (necesidades.isEmpty()) {
                metadata.put("totalNecesidades", 0);
                throw new NecesidadNotFoundException("No se encontraron necesidades para los OID indicados.");
            }

            Set<Integer> encontrados = necesidades.stream()
                    .map(Necesidad::getOidNecesidad)
                    .collect(Collectors.toSet());
            List<Integer> faltantes = oidSinDuplicados.stream()
                    .filter(id -> !encontrados.contains(id))
                    .collect(Collectors.toList());
            if (!faltantes.isEmpty()) {
                metadata.put("faltantes", faltantes);
                throw new NecesidadNotFoundException("Algunas necesidades no existen: " + faltantes);
            }

            List<Integer> estadosInvalidos = necesidades.stream()
                    .filter(necesidad -> !estadoOrigen.equals(necesidad.getEstado()))
                    .map(Necesidad::getOidNecesidad)
                    .collect(Collectors.toList());
            if (!estadosInvalidos.isEmpty()) {
                return new ApiResponse<>(400,
                        "Solo se pueden actualizar necesidades que estén en " + estadoOrigen,
                        Map.of("inconsistencias", Map.of("estadoInvalido", estadosInvalidos)));
            }

            boolean debeValidarDepartamento = EstadoNecesidad.EN_REVISION_JEFE.equals(estadoDestino);
            boolean requiereValidarSinAsignaciones = EstadoNecesidad.NO_ASIGNADA.equals(estadoOrigen)
                    && EstadoNecesidad.EN_REVISION_JEFE.equals(estadoDestino);
            Map<String, List<Integer>> inconsistencias = validarRequisitosPrevios(necesidades, debeValidarDepartamento);
            if (!inconsistencias.getOrDefault("sinGrupo", List.of()).isEmpty()
                    || !inconsistencias.getOrDefault("sinCupo", List.of()).isEmpty()) {
                return new ApiResponse<>(400,
                        "Todas las necesidades deben tener grupo y cupo definidos antes de cambiar de estado.",
                        Map.of("inconsistencias", inconsistencias));
            }
            if (debeValidarDepartamento && !inconsistencias.getOrDefault("sinDepartamento", List.of()).isEmpty()) {
                return new ApiResponse<>(400,
                        "Cada necesidad debe tener un departamento asociado antes de pasar a EN REVISION JEFE.",
                        Map.of("inconsistencias", inconsistencias));
            }
            if (requiereValidarSinAsignaciones) {
                Map<String, List<Integer>> relaciones = validarSinAsignacionesSeleccionados(necesidades);
                if (!relaciones.getOrDefault("conAsignaciones", List.of()).isEmpty()
                        || !relaciones.getOrDefault("conSeleccionados", List.of()).isEmpty()) {
                    return new ApiResponse<>(400,
                            "No se puede reabrir necesidades con asignaciones o seleccionados asociados.",
                            Map.of("inconsistencias", relaciones));
                }
            }

            necesidades.forEach(necesidad -> {
                necesidad.setEstado(estadoDestino);
                necesidad.setUsuarioActualizacion("system");
            });
            necesidadRepository.saveAll(necesidades);
            Calendario calendario = necesidades.get(0).getCalendario();
            notificarCambioEstado(necesidades, calendario, estadoOrigen, estadoDestino);

            metadata.put("totalNecesidades", necesidades.size());
            return new ApiResponse<>(200, "Estados actualizados correctamente.", metadata);
        } catch (NecesidadException e) {
            logger.warn("Error al cambiar estados por OID: {}", e.getMessage());
            return new ApiResponse<>(e.getStatus().value(), e.getMessage(), metadata);
        } catch (Exception e) {
            logger.error("Error interno al cambiar estados por OID", e);
            return new ApiResponse<>(500, "Error al cambiar el estado de las necesidades: " + e.getMessage(), metadata);
        }
    }

    private Calendario validarCalendario(Integer oidCalendario) {
        if (oidCalendario == null) {
            throw new NecesidadCalendarioObligatorioException();
        }
        return calendarioRepository.findById(oidCalendario)
                .orElseThrow(() -> new NecesidadNotFoundException("Calendario no encontrado con ID: " + oidCalendario));
    }

    private boolean esTransicionPermitida(EstadoNecesidad origen, EstadoNecesidad destino) {
        if (origen == null || destino == null) {
            return false;
        }
        return (EstadoNecesidad.BORRADOR.equals(origen) && EstadoNecesidad.EN_REVISION_SECRETARIO.equals(destino))
                || (EstadoNecesidad.EN_REVISION_SECRETARIO.equals(origen) && EstadoNecesidad.BORRADOR.equals(destino))
                || (EstadoNecesidad.EN_REVISION_SECRETARIO.equals(origen) && EstadoNecesidad.EN_REVISION_JEFE.equals(destino))
                || (EstadoNecesidad.EN_REVISION_JEFE.equals(origen) && EstadoNecesidad.EN_REVISION_SECRETARIO.equals(destino))
                || (EstadoNecesidad.EN_REVISION_JEFE.equals(origen) && EstadoNecesidad.NO_ASIGNADA.equals(destino))
                || (EstadoNecesidad.NO_ASIGNADA.equals(origen) && EstadoNecesidad.EN_REVISION_JEFE.equals(destino));
    }

    private boolean requierePrograma(EstadoNecesidad origen, EstadoNecesidad destino) {
        return (EstadoNecesidad.BORRADOR.equals(origen) && EstadoNecesidad.EN_REVISION_SECRETARIO.equals(destino))
                || (EstadoNecesidad.EN_REVISION_SECRETARIO.equals(origen) && EstadoNecesidad.BORRADOR.equals(destino));
    }

    private boolean requiereDepartamento(EstadoNecesidad origen, EstadoNecesidad destino) {
        return (EstadoNecesidad.EN_REVISION_JEFE.equals(origen) && EstadoNecesidad.NO_ASIGNADA.equals(destino))
                || (EstadoNecesidad.NO_ASIGNADA.equals(origen) && EstadoNecesidad.EN_REVISION_JEFE.equals(destino));
    }

    private List<Necesidad> obtenerNecesidadesParaTransicion(Integer oidCalendario,
                                                             EstadoNecesidad estadoOrigen,
                                                             Integer oidPrograma,
                                                             Integer oidDepartamento) {
        List<Necesidad> necesidades = necesidadRepository.findAllByCalendario_OidcalendarioAndEstado(oidCalendario, estadoOrigen);

        return necesidades.stream()
                .filter(necesidad -> {
                    if (oidPrograma == null) {
                        return true;
                    }
                    Materia materia = necesidad.getMateria();
                    if (materia == null || materia.getPlan() == null || materia.getPlan().getPrograma() == null) {
                        return false;
                    }
                    return oidPrograma.equals(materia.getPlan().getPrograma().getOidPrograma());
                })
                .filter(necesidad -> {
                    if (oidDepartamento == null) {
                        return true;
                    }
                    Materia materia = necesidad.getMateria();
                    if (materia == null || materia.getDepartamento() == null) {
                        return false;
                    }
                    return oidDepartamento.equals(materia.getDepartamento().getOidDepartamento());
                })
                .collect(Collectors.toList());
    }

    private Map<String, List<Integer>> validarRequisitosPrevios(List<Necesidad> necesidades,
                                                                boolean validarDepartamento) {
        List<Integer> sinGrupo = necesidades.stream()
                .filter(necesidad -> necesidad.getGrupo() == null || necesidad.getGrupo().trim().isEmpty())
                .map(Necesidad::getOidNecesidad)
                .collect(Collectors.toList());

        List<Integer> sinCupo = necesidades.stream()
                .filter(necesidad -> necesidad.getCupo() == null || necesidad.getCupo() <= 0)
                .map(Necesidad::getOidNecesidad)
                .collect(Collectors.toList());

        Map<String, List<Integer>> inconsistencias = new HashMap<>();
        inconsistencias.put("sinGrupo", sinGrupo);
        inconsistencias.put("sinCupo", sinCupo);

        if (validarDepartamento) {
            List<Integer> sinDepartamento = necesidades.stream()
                    .filter(necesidad -> {
                        Materia materia = necesidad.getMateria();
                        return materia == null || materia.getDepartamento() == null
                                || materia.getDepartamento().getOidDepartamento() == null;
                    })
                    .map(Necesidad::getOidNecesidad)
                    .collect(Collectors.toList());
            inconsistencias.put("sinDepartamento", sinDepartamento);
        } else {
            inconsistencias.put("sinDepartamento", List.of());
        }
        return inconsistencias;
    }

    private Map<String, List<Integer>> validarSinAsignacionesSeleccionados(List<Necesidad> necesidades) {
        List<Integer> conAsignaciones = new ArrayList<>();
        List<Integer> conSeleccionados = new ArrayList<>();
        for (Necesidad necesidad : necesidades) {
            if (necesidad == null || necesidad.getOidNecesidad() == null) {
                continue;
            }
            Integer oidNecesidad = necesidad.getOidNecesidad();
            long totalAsignaciones = asignacionRepository.countByNecesidad_OidNecesidad(oidNecesidad);
            if (totalAsignaciones > 0) {
                conAsignaciones.add(oidNecesidad);
                boolean tieneSeleccionados = asignacionRepository.findByNecesidad_OidNecesidad(oidNecesidad)
                        .stream()
                        .anyMatch(asignacion -> asignacion.getSeleccionado() != null);
                if (tieneSeleccionados) {
                    conSeleccionados.add(oidNecesidad);
                }
            }
        }
        Map<String, List<Integer>> inconsistencias = new HashMap<>();
        inconsistencias.put("conAsignaciones", conAsignaciones);
        inconsistencias.put("conSeleccionados", conSeleccionados);
        return inconsistencias;
    }

    private void notificarCambioEstado(List<Necesidad> necesidades,
                                       Calendario calendario,
                                       EstadoNecesidad estadoOrigen,
                                       EstadoNecesidad estadoDestino) {
        if (necesidades == null || necesidades.isEmpty()) {
            return;
        }
        Calendario calendarioReferencia = calendario != null
                ? calendario
                : necesidades.get(0).getCalendario();
        Usuario actor = obtenerUsuarioEjecutor();

        if (EstadoNecesidad.BORRADOR.equals(estadoOrigen)
                && EstadoNecesidad.EN_REVISION_SECRETARIO.equals(estadoDestino)) {
            notificarSecretario(necesidades, calendarioReferencia, actor, estadoOrigen, estadoDestino,
                    "Secretario/a de Facultad");
            return;
        }
        if (EstadoNecesidad.EN_REVISION_JEFE.equals(estadoOrigen)
                && EstadoNecesidad.EN_REVISION_SECRETARIO.equals(estadoDestino)) {
            notificarSecretario(necesidades, calendarioReferencia, actor, estadoOrigen, estadoDestino,
                    "Secretario/a de Facultad");
            return;
        }
        if (EstadoNecesidad.EN_REVISION_SECRETARIO.equals(estadoOrigen)
                && EstadoNecesidad.BORRADOR.equals(estadoDestino)) {
            notificarCoordinadores(necesidades, calendarioReferencia, actor, estadoOrigen, estadoDestino);
            return;
        }
        if (EstadoNecesidad.EN_REVISION_SECRETARIO.equals(estadoOrigen)
                && EstadoNecesidad.EN_REVISION_JEFE.equals(estadoDestino)) {
            notificarJefes(necesidades, calendarioReferencia, actor, estadoOrigen, estadoDestino);
            return;
        }
        if (EstadoNecesidad.EN_REVISION_JEFE.equals(estadoOrigen)
                && EstadoNecesidad.NO_ASIGNADA.equals(estadoDestino)) {
            notificarJefes(necesidades, calendarioReferencia, actor, estadoOrigen, estadoDestino);
        }
    }

    private void notificarSecretario(List<Necesidad> necesidades,
                                     Calendario calendario,
                                     Usuario actor,
                                     EstadoNecesidad origen,
                                     EstadoNecesidad destino,
                                     String destinatarioRol) {
        Optional<Usuario> secretarioOpt = usuarioRepository.findFirstActiveByRolNombre(ROL_SECRETARIA_FACULTAD);
        if (secretarioOpt.isEmpty()) {
            logger.warn("No se encontró usuario con rol {}", ROL_SECRETARIA_FACULTAD);
            return;
        }
        Usuario secretario = secretarioOpt.get();
        if (secretario.getCorreo() == null) {
            logger.warn("El secretario de facultad no tiene correo configurado para enviar notificaciones.");
            return;
        }
        ResumenNecesidades resumen = construirResumen(necesidades);
        enviarCorreo(
                List.of(secretario.getCorreo()),
                construirAsunto(origen, destino, calendario, resumen),
                construirMensaje(destinatarioRol, secretario, actor, origen, destino, calendario, resumen));
    }

    private void notificarCoordinadores(List<Necesidad> necesidades,
                                        Calendario calendario,
                                        Usuario actor,
                                        EstadoNecesidad origen,
                                        EstadoNecesidad destino) {
        Map<Integer, List<Necesidad>> porPrograma = agruparPorPrograma(necesidades);
        if (porPrograma.isEmpty()) {
            logger.warn("No se encontraron programas asociados para la notificación {} → {}", origen, destino);
            return;
        }
        for (Map.Entry<Integer, List<Necesidad>> entry : porPrograma.entrySet()) {
            List<Necesidad> lote = entry.getValue();
            if (lote == null || lote.isEmpty()) {
                continue;
            }
            Programa programa = extraerPrograma(lote.get(0));
            if (programa == null) {
                logger.warn("No fue posible identificar el programa para la notificación {} → {}", origen, destino);
                continue;
            }
            Usuario coordinador = programa.getCoordinador();
            if (coordinador == null || coordinador.getCorreo() == null) {
                logger.warn("El programa {} no tiene un coordinador configurado para enviar notificación.", programa.getNombre());
                continue;
            }
            ResumenNecesidades resumen = construirResumen(lote);
            String rol = programa.getNombre() != null
                    ? "Coordinador del programa " + programa.getNombre()
                    : "Coordinador del programa";
            enviarCorreo(
                    List.of(coordinador.getCorreo()),
                    construirAsunto(origen, destino, calendario, resumen),
                    construirMensaje(rol, coordinador, actor, origen, destino, calendario, resumen));
        }
    }

    private void notificarJefes(List<Necesidad> necesidades,
                                Calendario calendario,
                                Usuario actor,
                                EstadoNecesidad origen,
                                EstadoNecesidad destino) {
        Map<Integer, List<Necesidad>> porDepartamento = agruparPorDepartamento(necesidades);
        if (porDepartamento.isEmpty()) {
            logger.warn("No se encontraron departamentos asociados para la notificación {} → {}", origen, destino);
            return;
        }
        for (Map.Entry<Integer, List<Necesidad>> entry : porDepartamento.entrySet()) {
            List<Necesidad> lote = entry.getValue();
            if (lote == null || lote.isEmpty()) {
                continue;
            }
            Departamento departamento = extraerDepartamento(lote.get(0));
            if (departamento == null) {
                logger.warn("No fue posible identificar el departamento para la notificación {} → {}", origen, destino);
                continue;
            }
            Usuario jefe = departamento.getJefe();
            if (jefe == null || jefe.getCorreo() == null) {
                logger.warn("El departamento {} no tiene un jefe configurado para enviar notificación.", departamento.getNombre());
                continue;
            }
            ResumenNecesidades resumen = construirResumen(lote);
            String rol = departamento.getNombre() != null
                    ? "Jefe del Departamento " + departamento.getNombre()
                    : "Jefe de Departamento";
            enviarCorreo(
                    List.of(jefe.getCorreo()),
                    construirAsunto(origen, destino, calendario, resumen),
                    construirMensaje(rol, jefe, actor, origen, destino, calendario, resumen));
        }
    }

    private Map<Integer, List<Necesidad>> agruparPorPrograma(List<Necesidad> necesidades) {
        Map<Integer, List<Necesidad>> resultado = new HashMap<>();
        for (Necesidad necesidad : necesidades) {
            Programa programa = extraerPrograma(necesidad);
            if (programa == null || programa.getOidPrograma() == null) {
                continue;
            }
            resultado.computeIfAbsent(programa.getOidPrograma(), key -> new ArrayList<>()).add(necesidad);
        }
        return resultado;
    }

    private Map<Integer, List<Necesidad>> agruparPorDepartamento(List<Necesidad> necesidades) {
        Map<Integer, List<Necesidad>> resultado = new HashMap<>();
        for (Necesidad necesidad : necesidades) {
            Departamento departamento = extraerDepartamento(necesidad);
            if (departamento == null || departamento.getOidDepartamento() == null) {
                continue;
            }
            resultado.computeIfAbsent(departamento.getOidDepartamento(), key -> new ArrayList<>()).add(necesidad);
        }
        return resultado;
    }

    private Programa extraerPrograma(Necesidad necesidad) {
        if (necesidad == null || necesidad.getMateria() == null) {
            return null;
        }
        Plan plan = necesidad.getMateria().getPlan();
        if (plan == null) {
            return null;
        }
        return plan.getPrograma();
    }

    private Departamento extraerDepartamento(Necesidad necesidad) {
        if (necesidad == null || necesidad.getMateria() == null) {
            return null;
        }
        return necesidad.getMateria().getDepartamento();
    }

    private Usuario obtenerUsuarioEjecutor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        return usuarioRepository.findByCorreo(authentication.getName()).orElse(null);
    }

    private String construirAsunto(EstadoNecesidad origen,
                                   EstadoNecesidad destino,
                                   Calendario calendario,
                                   ResumenNecesidades resumen) {
        String calendarioTexto = calendario != null
                ? String.format("%s - %s", calendario.getAnioCalendario(), calendario.getNumeroCalendario())
                : "Calendario no especificado";
        int total = resumen != null ? resumen.totalNecesidades : 0;
        return String.format("Transición %s → %s - %s (%d necesidades)",
                origen.getValor(),
                destino.getValor(),
                calendarioTexto,
                total);
    }

    private String construirMensaje(String destinatarioRol,
                                    Usuario destinatario,
                                    Usuario actor,
                                    EstadoNecesidad origen,
                                    EstadoNecesidad destino,
                                    Calendario calendario,
                                    ResumenNecesidades resumen) {
        String saludo = destinatario != null && destinatario.getNombres() != null
                ? "Hola " + destinatario.getNombres() + ","
                : "Hola,";
        String actorDescripcion = describirActor(actor);
        String programas = formatearListado(resumen != null ? resumen.programas : Set.of(), "No asociados");
        String departamentos = formatearListado(resumen != null ? resumen.departamentos : Set.of(), "No asociados");
        long totalMaterias = resumen != null ? resumen.totalMaterias : 0;
        int totalNecesidades = resumen != null ? resumen.totalNecesidades : 0;
        String calendarioTexto = calendario != null
                ? String.format("%s - %s", calendario.getAnioCalendario(), calendario.getNumeroCalendario())
                : "No especificado";

        return new StringBuilder()
                .append(saludo).append("\n\n")
                .append("El usuario ").append(actorDescripcion)
                .append(" ha ejecutado una transición de necesidades en el sistema.\n\n")
                .append("Transición: ").append(origen.getValor()).append(" → ").append(destino.getValor()).append("\n")
                .append("Dirigido a: ").append(destinatarioRol).append("\n")
                .append("Calendario: ").append(calendarioTexto).append("\n")
                .append("Programas involucrados: ").append(programas).append("\n")
                .append("Departamentos involucrados: ").append(departamentos).append("\n")
                .append("Total de necesidades: ").append(totalNecesidades).append("\n")
                .append("Total de materias: ").append(totalMaterias).append("\n\n")
                .append("Por favor, revise el detalle en el Sistema de Gestión Docente.\n\n")
                .append("Atentamente,\n")
                .append("Sistema de Gestión Docente")
                .toString();
    }

    private String describirActor(Usuario actor) {
        if (actor == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                return authentication.getName();
            }
            return "Sistema";
        }
        String nombres = actor.getNombres() != null ? actor.getNombres() : "";
        String apellidos = actor.getApellidos() != null ? actor.getApellidos() : "";
        String correo = actor.getCorreo() != null ? actor.getCorreo() : "";
        return String.format("%s %s (%s)", nombres.strip(), apellidos.strip(), correo).trim();
    }

    private String formatearListado(Set<String> valores, String vacio) {
        if (valores == null || valores.isEmpty()) {
            return vacio;
        }
        return String.join(", ", valores);
    }

    private void enviarCorreo(List<String> destinatarios, String asunto, String mensaje) {
        if (destinatarios == null || destinatarios.isEmpty()) {
            logger.warn("Intento de enviar notificación sin destinatarios. Asunto: {}", asunto);
            return;
        }
        try {
            clienteNotificacion.enviarNotificacion(destinatarios, asunto, mensaje);
        } catch (Exception e) {
            logger.error("Error al enviar la notificación '{}': {}", asunto, e.getMessage());
        }
    }

    private ResumenNecesidades construirResumen(List<Necesidad> necesidades) {
        if (necesidades == null || necesidades.isEmpty()) {
            return new ResumenNecesidades(Set.of(), Set.of(), 0, 0);
        }
        Set<String> programas = new LinkedHashSet<>();
        Set<String> departamentos = new LinkedHashSet<>();
        Set<Integer> materias = new HashSet<>();

        for (Necesidad necesidad : necesidades) {
            if (necesidad == null) {
                continue;
            }
            Materia materia = necesidad.getMateria();
            if (materia != null) {
                if (materia.getPlan() != null && materia.getPlan().getPrograma() != null
                        && materia.getPlan().getPrograma().getNombre() != null) {
                    programas.add(materia.getPlan().getPrograma().getNombre());
                }
                if (materia.getDepartamento() != null && materia.getDepartamento().getNombre() != null) {
                    departamentos.add(materia.getDepartamento().getNombre());
                }
                if (materia.getIdMateria() != null) {
                    materias.add(materia.getIdMateria());
                }
            }
        }

        return new ResumenNecesidades(programas, departamentos, materias.size(), necesidades.size());
    }

    private static class ResumenNecesidades {
        private final Set<String> programas;
        private final Set<String> departamentos;
        private final long totalMaterias;
        private final int totalNecesidades;

        private ResumenNecesidades(Set<String> programas,
                                   Set<String> departamentos,
                                   long totalMaterias,
                                   int totalNecesidades) {
            this.programas = programas;
            this.departamentos = departamentos;
            this.totalMaterias = totalMaterias;
            this.totalNecesidades = totalNecesidades;
        }
    }
}
