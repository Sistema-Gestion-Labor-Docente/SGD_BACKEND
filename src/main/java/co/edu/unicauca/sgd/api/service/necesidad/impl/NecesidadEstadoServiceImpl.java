package co.edu.unicauca.sgd.api.service.necesidad.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Materia;
import co.edu.unicauca.sgd.api.domain.Necesidad;
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
import co.edu.unicauca.sgd.api.repository.NecesidadRepository;
import co.edu.unicauca.sgd.api.service.necesidad.NecesidadEstadoService;

@Service
public class NecesidadEstadoServiceImpl implements NecesidadEstadoService {

    private static final Logger logger = LoggerFactory.getLogger(NecesidadEstadoServiceImpl.class);

    private final NecesidadRepository necesidadRepository;
    private final CalendarioRepository calendarioRepository;

    public NecesidadEstadoServiceImpl(NecesidadRepository necesidadRepository,
                                      CalendarioRepository calendarioRepository) {
        this.necesidadRepository = necesidadRepository;
        this.calendarioRepository = calendarioRepository;
    }

    @Override
    @Transactional
    public ApiResponse<Map<String, Object>> cambiarEstadoMasivo(Integer oidCalendario,
                                                                EstadoNecesidad estadoOrigen,
                                                                EstadoNecesidad estadoDestino,
                                                                Integer oidPrograma,
                                                                Integer oidDepartamento) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("calendario", oidCalendario);
        metadata.put("estadoOrigen", estadoOrigen != null ? estadoOrigen.name() : null);
        metadata.put("estadoDestino", estadoDestino != null ? estadoDestino.name() : null);
        if (oidPrograma != null) {
            metadata.put("oidPrograma", oidPrograma);
        }
        if (oidDepartamento != null) {
            metadata.put("oidDepartamento", oidDepartamento);
        }

        try {
            validarCalendario(oidCalendario);

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
            if (necesidades.isEmpty()) {
                metadata.put("totalNecesidades", 0);
                throw new NecesidadNotFoundException("No se encontraron necesidades para la transición solicitada.");
            }

            boolean debeValidarDepartamento = EstadoNecesidad.EN_REVISION_JEFE.equals(estadoDestino);

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

            necesidades.forEach(necesidad -> {
                necesidad.setEstado(estadoDestino);
                necesidad.setUsuarioActualizacion("system");
            });
            necesidadRepository.saveAll(necesidades);

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

            necesidades.forEach(necesidad -> {
                necesidad.setEstado(estadoDestino);
                necesidad.setUsuarioActualizacion("system");
            });
            necesidadRepository.saveAll(necesidades);

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
                || (EstadoNecesidad.EN_REVISION_JEFE.equals(origen) && EstadoNecesidad.NO_ASIGNADA.equals(destino));
    }

    private boolean requierePrograma(EstadoNecesidad origen, EstadoNecesidad destino) {
        return (EstadoNecesidad.BORRADOR.equals(origen) && EstadoNecesidad.EN_REVISION_SECRETARIO.equals(destino))
                || (EstadoNecesidad.EN_REVISION_SECRETARIO.equals(origen) && EstadoNecesidad.BORRADOR.equals(destino));
    }

    private boolean requiereDepartamento(EstadoNecesidad origen, EstadoNecesidad destino) {
        return EstadoNecesidad.EN_REVISION_JEFE.equals(origen) && EstadoNecesidad.NO_ASIGNADA.equals(destino);
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
}
