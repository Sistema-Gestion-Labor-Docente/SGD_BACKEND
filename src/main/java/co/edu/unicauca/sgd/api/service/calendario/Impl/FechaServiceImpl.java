package co.edu.unicauca.sgd.api.service.calendario.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Fecha;
import co.edu.unicauca.sgd.api.domain.NombreFecha;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTOResponse;
import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;
import co.edu.unicauca.sgd.api.exception.calendario.CalendarioNoEncontradoException;
import co.edu.unicauca.sgd.api.exception.calendario.FechaConsultaException;
import co.edu.unicauca.sgd.api.exception.calendario.FechaNoEncontradaException;
import co.edu.unicauca.sgd.api.exception.calendario.FechaOperacionNoPermitidaException;
import co.edu.unicauca.sgd.api.exception.calendario.FechaProcesoException;
import co.edu.unicauca.sgd.api.exception.calendario.NombreFechaNoEncontradoException;
import co.edu.unicauca.sgd.api.mapper.FechaMapper;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.FechaRepository;
import co.edu.unicauca.sgd.api.repository.NombreFechaRepository;
import co.edu.unicauca.sgd.api.service.calendario.FechaService;

@Service
public class FechaServiceImpl implements FechaService {

    // IDs fijos en la tabla NombreFecha
    private static final int NOMBRE_PERIODO_INICIO = 1;   // "Inicio de periodo"
    private static final int NOMBRE_PERIODO_FIN    = 10;  // "Finalización de periodo"
    private static final int NOMBRE_CLASES_INICIO  = 3;   // "Inicio de clases"
    private static final int NOMBRE_CLASES_FIN     = 7;   // "Finalización de clases"

    private static final Sort SORT_FECHA_INICIAL_ASC = Sort.by("fechaInicial").ascending();


    private final FechaRepository fechaRepository;

    private final CalendarioRepository calendarioRepository;

    private final NombreFechaRepository nombreFechaRepository;

    private final FechaMapper fechaMapper;

    public FechaServiceImpl(FechaRepository fechaRepository, CalendarioRepository calendarioRepository,
            NombreFechaRepository nombreFechaRepository, FechaMapper fechaMapper) {
        this.fechaRepository = fechaRepository;
        this.calendarioRepository = calendarioRepository;
        this.nombreFechaRepository = nombreFechaRepository;
        this.fechaMapper = fechaMapper;
    }

    
    @Override
    public ApiResponse<Page<FechaDTOResponse>> obtenerTodas(TipoFechaEnum tipo, Pageable pageable) {
        try {
            Specification<Fecha> spec = (root, query, cb) -> cb.conjunction();
            if (tipo != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("tipo"), tipo));
            }

            Pageable sortedPageable = ensureSortedPageable(pageable);
            Page<Fecha> fechas;
            if (sortedPageable == null) {
                List<Fecha> ordenadas = fechaRepository.findAll(spec, SORT_FECHA_INICIAL_ASC);
                fechas = new PageImpl<>(ordenadas, Pageable.unpaged(), ordenadas.size());
            } else {
                fechas = fechaRepository.findAll(spec, sortedPageable);
            }

            Page<FechaDTOResponse> responsePage = fechas.map(fechaMapper::toResponse);
            boolean hasContent = responsePage.hasContent();
            String message = hasContent ? "Fechas obtenidas correctamente" : "No se encontraron fechas.";
            return new ApiResponse<>(200, message, responsePage);
        } catch (Exception e) {
            FechaConsultaException ex =
                    new FechaConsultaException("Error al obtener fechas", e);
            return new ApiResponse<>(500, ex.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<FechaDTOResponse> buscarPorId(Integer oid) {
        try {
            Fecha fecha = fechaRepository.findById(oid)
                    .orElseThrow(() -> new FechaNoEncontradaException(oid));
            return new ApiResponse<>(200, "Fecha encontrada correctamente", fechaMapper.toResponse(fecha));
        } catch (FechaNoEncontradaException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            FechaConsultaException ex =
                    new FechaConsultaException("Error interno al buscar la fecha", e);
            return new ApiResponse<>(500, ex.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<FechaDTOResponse> guardar(FechaDTORequest dto) {
        try {
            validarAlAgregarFechaEspecial(dto, dto.getOidCalendario());
            validarUnicidadFechasEspeciales(dto);
            validarRangoSiPresentes(dto.getFechaInicial(), dto.getFechaFin());

            Calendario calendario = calendarioRepository.findById(dto.getOidCalendario())
                    .orElseThrow(() -> new CalendarioNoEncontradoException(dto.getOidCalendario()));

            validarAnioConCalendarioSiPresente(dto.getFechaInicial(), calendario, "fechaInicial");
            validarAnioConCalendarioSiPresente(dto.getFechaFin(), calendario, "fechaFin");

            validarPeriodoDefinidoYLimites(dto, calendario.getOidcalendario());
            validarRelacionesClases(dto, calendario.getOidcalendario(), null);
            validarCapacidadPorTipo(dto.getTipo(), calendario.getOidcalendario(), null);

            NombreFecha nombreFecha = nombreFechaRepository.findById(dto.getOidNombreFecha())
                    .orElseThrow(() -> new NombreFechaNoEncontradoException(dto.getOidNombreFecha()));

            validarPeriodoDefinidoYLimites(dto, calendario.getOidcalendario());
            validarRelacionesClases(dto, calendario.getOidcalendario(), null);
            validarCapacidadPorTipo(dto.getTipo(), calendario.getOidcalendario(), null);

            Fecha entidad = fechaMapper.convertToEntity(dto, calendario, nombreFecha);
            Fecha guardada = fechaRepository.save(entidad);
            ajustarDatosCalendarioSiAplica(dto, calendario);
            return new ApiResponse<>(200, "Fecha guardada correctamente", fechaMapper.toResponse(guardada));
        } catch (FechaOperacionNoPermitidaException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (CalendarioNoEncontradoException | NombreFechaNoEncontradoException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            FechaProcesoException ex =
                    new FechaProcesoException("Error al guardar la fecha", e);
            return new ApiResponse<>(500, ex.getMessage(), null);
        }
    }
    @Override
    @Transactional
    public ApiResponse<FechaDTOResponse> actualizar(Integer id, FechaDTORequest dto) {
        try {
            validarUnicidadFechasEspeciales(dto);
            validarRangoSiPresentes(dto.getFechaInicial(), dto.getFechaFin());

            Fecha existente = fechaRepository.findById(id)
                    .orElseThrow(() -> new FechaNoEncontradaException(id));

            if (dto.getOidCalendario() != null
                    && !dto.getOidCalendario().equals(existente.getCalendario().getOidcalendario())) {
                throw new FechaOperacionNoPermitidaException("El calendario no puede cambiar en una actualización.");
            }

            Calendario calendario = existente.getCalendario();

            validarAnioConCalendarioSiPresente(dto.getFechaInicial(), calendario, "fechaInicial");
            validarAnioConCalendarioSiPresente(dto.getFechaFin(), calendario, "fechaFin");

            NombreFecha nombreFecha = nombreFechaRepository.findById(dto.getOidNombreFecha())
                    .orElseThrow(() -> new NombreFechaNoEncontradoException(dto.getOidNombreFecha()));

            validarPeriodoDefinidoYLimites(dto, calendario.getOidcalendario());
            validarRelacionesClases(dto, calendario.getOidcalendario(), id);
            validarCapacidadPorTipo(dto.getTipo(), calendario.getOidcalendario(), id);

            fechaMapper.actualizarCamposBasicos(existente, dto, calendario, nombreFecha);
            Fecha actualizada = fechaRepository.save(existente);
            ajustarDatosCalendarioSiAplica(dto, calendario);
            return new ApiResponse<>(200, "Fecha actualizada correctamente", fechaMapper.toResponse(actualizada));
        } catch (FechaOperacionNoPermitidaException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (FechaNoEncontradaException | NombreFechaNoEncontradoException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            FechaProcesoException ex =
                    new FechaProcesoException("Error al actualizar la fecha", e);
            return new ApiResponse<>(500, ex.getMessage(), null);
        }
    }
    @Override
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            Fecha fecha = fechaRepository.findById(oid)
                .orElseThrow(() -> new FechaNoEncontradaException(oid));
    
            // Validar si es especial
            if (fecha.getNombreFecha().getOidNombreFecha() == NOMBRE_PERIODO_INICIO ||
                fecha.getNombreFecha().getOidNombreFecha() == NOMBRE_PERIODO_FIN ||
                fecha.getNombreFecha().getOidNombreFecha() == NOMBRE_CLASES_INICIO ||
                fecha.getNombreFecha().getOidNombreFecha() == NOMBRE_CLASES_FIN) {
                throw new FechaOperacionNoPermitidaException("No se permite eliminar esta fecha especial: " + fecha.getNombreFecha().getNombre());
            }

            fechaRepository.deleteById(oid);
            return new ApiResponse<>(200, "Fecha eliminada correctamente", null);
        } catch (FechaOperacionNoPermitidaException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (FechaNoEncontradaException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            FechaProcesoException ex =
                    new FechaProcesoException("Error al eliminar la fecha", e);
            return new ApiResponse<>(500, ex.getMessage(), null);
        }
    }

    private Pageable ensureSortedPageable(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return null;
        }
        Sort sort = pageable.getSort().isUnsorted()
                ? SORT_FECHA_INICIAL_ASC
                : pageable.getSort().and(SORT_FECHA_INICIAL_ASC);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    /* ------------ Helpers ------------ */
    private static boolean esRango(FechaDTORequest dto) {
        return dto.getFechaFin() != null;
    }

    private static String etiquetaInicio(boolean esRango) {
        return esRango ? "El inicio del rango" : "La fecha";
    }

    private static String etiquetaFin() {
        return "El fin del rango";
    }

    private static String msgAntesDe(String etiqueta, String hito) {
        return etiqueta + " no puede ser anterior a " + hito + ".";
    }

    private static String msgDespuesDe(String etiqueta, String hito) {
        return etiqueta + " no puede ser posterior a " + hito + ".";
    }

    /**
     * Valida que si ambos inician y fin están presentes, entonces inicio <= fin.
     * Si uno o ambos son nulos, no hace nada.
     */
    private void validarRangoSiPresentes(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio != null && fin != null && inicio.isAfter(fin)) {
            throw new FechaOperacionNoPermitidaException("La fecha inicial no puede ser mayor que la fecha fin.");
        }
    }

    /**
     * Valida que la fecha (si está presente) tenga el mismo año que el calendario.
     */
    private void validarAnioConCalendarioSiPresente(LocalDateTime fecha, Calendario cal, String etiquetaCampo) {
        if (fecha == null) return;

        int anioCalendario;
        try {
            anioCalendario = Integer.parseInt(cal.getAnioCalendario());
        } catch (NumberFormatException ex) {
            throw new FechaOperacionNoPermitidaException("El año del calendario es inválido: " + cal.getAnioCalendario());
        }

        if (fecha.getYear() != anioCalendario) {
            throw new FechaOperacionNoPermitidaException(
                String.format("El año de %s (%d) debe coincidir con el año del calendario (%d).",
                              etiquetaCampo, fecha.getYear(), anioCalendario)
            );
        }
    }

    /**
     * Valida la capacidad máxima de fechas por tipo en un calendario.
     * - CLASES: máximo 2 (Inicio y Fin de clases)
     */
    private void validarCapacidadPorTipo(TipoFechaEnum tipo, Integer oidCalendario, Integer oidFechaExcluida) {
        if (tipo == TipoFechaEnum.RESALTADAS 
            || tipo == TipoFechaEnum.NO_RESALTADAS 
            || tipo == TipoFechaEnum.ADMINISTRATIVAS) {
            return; // no aplica validación de capacidad
        }

        int maxPorCalendario = (tipo == TipoFechaEnum.CLASES) ? 2 : 1;

        long existentes = (oidFechaExcluida == null)
            ? fechaRepository.countByCalendario_OidcalendarioAndTipo(oidCalendario, tipo)
            : fechaRepository.countByCalendario_OidcalendarioAndTipoAndOidFechaNot(oidCalendario, tipo, oidFechaExcluida);

        if (existentes >= maxPorCalendario) {
            String detalle = (tipo == TipoFechaEnum.CLASES)
                ? "Solo se permiten 2 registros de tipo CLASES por calendario."
                : String.format("Solo se permite 1 registro de tipo %s por calendario.", tipo.name());
            throw new FechaOperacionNoPermitidaException(detalle);
        }
    }

    /**
     * 1) Exige que el Inicio de período (Nombre 1) exista antes de crear/editar cualquier otra fecha.
     * 2) Valida que las fechas no sean anteriores al Inicio de período.
     * 3) Si Fin de período (Nombre 10) existe, valida que no excedan ese fin.
     */
    private void validarPeriodoDefinidoYLimites(FechaDTORequest dto, Integer oidCalendario) {
        final Integer nombre = dto.getOidNombreFecha();
        final boolean esNombreInicio = java.util.Objects.equals(nombre, NOMBRE_PERIODO_INICIO);
        final boolean esNombreFin    = java.util.Objects.equals(nombre, NOMBRE_PERIODO_FIN);
        final boolean rango          = esRango(dto);

        if (dto.getFechaInicial() == null && dto.getFechaFin() == null) return;

        // --- Límite inferior (Nombre 1) ---
        Optional<Fecha> inicioPeriodoOpt =
            fechaRepository.findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(
                oidCalendario, NOMBRE_PERIODO_INICIO);

        if (!esNombreInicio) {
            Fecha inicio = inicioPeriodoOpt.orElseThrow(() ->
                new FechaOperacionNoPermitidaException("Debe definir primero la fecha con Nombre 1 (Inicio de período) para este calendario.")
            );

            LocalDateTime min = (inicio.getFechaInicial() != null) ? inicio.getFechaInicial() : inicio.getFechaFin();
            if (min == null) {
                throw new FechaOperacionNoPermitidaException("El 'Inicio de período' (Nombre 1) debe tener al menos una fecha definida.");
            }

            if (dto.getFechaInicial() != null && dto.getFechaInicial().isBefore(min)) {
                throw new FechaOperacionNoPermitidaException(msgAntesDe(etiquetaInicio(rango), "el Inicio de período (Nombre 1)"));
            }
            if (dto.getFechaFin() != null && dto.getFechaFin().isBefore(min)) {
                throw new FechaOperacionNoPermitidaException(msgAntesDe(etiquetaFin(), "el Inicio de período (Nombre 1)"));
            }
        }

        // --- Límite superior (Nombre 10) ---
        Optional<Fecha> finPeriodoOpt =
            fechaRepository.findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(
                oidCalendario, NOMBRE_PERIODO_FIN);

        if (!esNombreFin && finPeriodoOpt.isPresent()) {
            Fecha fin = finPeriodoOpt.get();
            LocalDateTime max = (fin.getFechaFin() != null) ? fin.getFechaFin() : fin.getFechaInicial();

            if (max != null) {
                if (dto.getFechaInicial() != null && dto.getFechaInicial().isAfter(max)) {
                    throw new FechaOperacionNoPermitidaException(msgDespuesDe(etiquetaInicio(rango), "la Finalización de período (Nombre 10)"));
                }
                if (dto.getFechaFin() != null && dto.getFechaFin().isAfter(max)) {
                    throw new FechaOperacionNoPermitidaException(msgDespuesDe(etiquetaFin(), "la Finalización de período (Nombre 10)"));
                }
            }
        }
    }


    /**
     * Reglas específicas para CLASES:
     * - Si guardo/actualizo Nombre 3 (inicio de clases) y ya existe Nombre 7 (fin de clases),
     *   entonces las fechas de Nombre 3 no pueden quedar después de las de Nombre 7.
     * - Si guardo/actualizo Nombre 7 (fin de clases) y ya existe Nombre 3 (inicio de clases),
     *   entonces las fechas de Nombre 7 no pueden quedar antes de las de Nombre 3.
     * Nota: valida los campos presentes; si vienen nulos, se omite esa comparación.
     */
    private void validarRelacionesClases(FechaDTORequest dto, Integer oidCalendario, Integer oidFechaExcluida) {
        if (dto.getTipo() != TipoFechaEnum.CLASES) return;

        final boolean rango = esRango(dto);
        Integer nombre = dto.getOidNombreFecha();
        if (nombre == null) return;

        if (nombre == NOMBRE_CLASES_INICIO) {
            Optional<Fecha> finClasesOpt =
                fechaRepository.findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(oidCalendario, NOMBRE_CLASES_FIN);

            if (finClasesOpt.isPresent() && (oidFechaExcluida == null || !finClasesOpt.get().getOidFecha().equals(oidFechaExcluida))) {
                Fecha finClases = finClasesOpt.get();
                LocalDateTime max = (finClases.getFechaFin() != null) ? finClases.getFechaFin() : finClases.getFechaInicial();

                if (max != null) {
                    if (dto.getFechaInicial() != null && dto.getFechaInicial().isAfter(max)) {
                        throw new FechaOperacionNoPermitidaException(msgDespuesDe(etiquetaInicio(rango), "el Fin de clases (Nombre 7) existente"));
                    }
                    if (dto.getFechaFin() != null && dto.getFechaFin().isAfter(max)) {
                        throw new FechaOperacionNoPermitidaException(msgDespuesDe(etiquetaFin(), "el Fin de clases (Nombre 7) existente"));
                    }
                }
            }

        } else if (nombre == NOMBRE_CLASES_FIN) {
            Optional<Fecha> inicioClasesOpt =
                fechaRepository.findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(oidCalendario, NOMBRE_CLASES_INICIO);

            if (inicioClasesOpt.isPresent() && (oidFechaExcluida == null || !inicioClasesOpt.get().getOidFecha().equals(oidFechaExcluida))) {
                Fecha inicioClases = inicioClasesOpt.get();
                LocalDateTime min = (inicioClases.getFechaInicial() != null) ? inicioClases.getFechaInicial() : inicioClases.getFechaFin();

                if (min != null) {
                    if (dto.getFechaInicial() != null && dto.getFechaInicial().isBefore(min)) {
                        throw new FechaOperacionNoPermitidaException(msgAntesDe(etiquetaInicio(rango), "el Inicio de clases (Nombre 3) existente"));
                    }
                    if (dto.getFechaFin() != null && dto.getFechaFin().isBefore(min)) {
                        throw new FechaOperacionNoPermitidaException(msgAntesDe(etiquetaFin(), "el Inicio de clases (Nombre 3) existente"));
                    }
                }
            }
        }
    }

    /**
     * Ajusta datos del calendario relacionados con semanas y horas según las fechas de ciertos tipos.
     * Se llama después de guardar o actualizar una fecha.
     */
    private void ajustarDatosCalendarioSiAplica(FechaDTORequest dto, Calendario calendario) {
        // Para obtener otros registros si faltan fechas.
        Optional<Fecha> inicioClasesOpt = fechaRepository.findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(
            calendario.getOidcalendario(), NOMBRE_CLASES_INICIO);
        Optional<Fecha> finClasesOpt = fechaRepository.findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(
            calendario.getOidcalendario(), NOMBRE_CLASES_FIN);

        // 1. Calcular semanas de clases
        if (dto.getTipo() == TipoFechaEnum.CLASES) {
            // ¿Estoy guardando inicio o fin?
            LocalDateTime inicio = (dto.getOidNombreFecha() == NOMBRE_CLASES_INICIO) ? dto.getFechaInicial() : inicioClasesOpt.map(Fecha::getFechaInicial).orElse(null);
            LocalDateTime fin = (dto.getOidNombreFecha() == NOMBRE_CLASES_FIN) ? dto.getFechaInicial() : finClasesOpt.map(Fecha::getFechaFin).orElse(null);

            if (inicio != null && fin != null) {
                long semanas = java.time.temporal.ChronoUnit.WEEKS.between(inicio.toLocalDate(), fin.toLocalDate()) + 1;
                calendario.setSemanasClase((float) semanas);
            }
        }

        // 2. Calcular semanas de preparación (del inicio de periodo al fin de clases)
        if (dto.getTipo() == TipoFechaEnum.CLASES || dto.getOidNombreFecha() == NOMBRE_PERIODO_INICIO) {
            Optional<Fecha> inicioPeriodoOpt = fechaRepository.findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(
                calendario.getOidcalendario(), NOMBRE_PERIODO_INICIO);

            LocalDateTime inicioPeriodo = (dto.getOidNombreFecha() == NOMBRE_PERIODO_INICIO)
                ? dto.getFechaInicial()
                : inicioPeriodoOpt.map(Fecha::getFechaInicial).orElse(null);
            LocalDateTime finClases = (dto.getOidNombreFecha() == NOMBRE_CLASES_FIN)
                ? dto.getFechaInicial()
                : finClasesOpt.map(Fecha::getFechaInicial).orElse(null);

            if (inicioPeriodo != null && finClases != null) {
                long semanasPrep = java.time.temporal.ChronoUnit.WEEKS.between(
                    inicioPeriodo.toLocalDate(), finClases.toLocalDate());
                calendario.setSemanasPreparacion((float) semanasPrep);
            }
        }

        // 3. Horas según tipo
        if (dto.getTipo() == TipoFechaEnum.OCASIONAL || dto.getTipo() == TipoFechaEnum.CATEDRA ||
            dto.getTipo() == TipoFechaEnum.PLANTA || dto.getTipo() == TipoFechaEnum.BECARIO_Y_PRACTICANTE) {

            // Si no hay fecha fin, no se puede calcular
            if (dto.getFechaInicial() != null && dto.getFechaFin() != null) {
                long dias = java.time.temporal.ChronoUnit.DAYS.between(dto.getFechaInicial().toLocalDate(), dto.getFechaFin().toLocalDate()) + 1;
                // Suponiendo 8 horas por día (ajusta esto si tu lógica es diferente)
                float horas = dias * 8.0f;

                switch (dto.getTipo()) {
                    case OCASIONAL:
                        calendario.setHorasOcasionales(horas);
                        break;
                    case CATEDRA:
                        calendario.setHorasCatedra(horas);
                        break;
                    case BECARIO_Y_PRACTICANTE:
                        calendario.setHorasBecarioPracticante(horas);
                        break;
                    case PLANTA:
                        calendario.setHorasPlanta(horas);
                        break;
                }
            }
        }

        // Guardar cambios en el calendario
        calendarioRepository.save(calendario);
    }

    /**
     * Valida que ciertas fechas especiales (Inicio de período y Finalización de período)
     * no tengan fechaFin definida.
     */
    private void validarUnicidadFechasEspeciales(FechaDTORequest dto) {
        if (dto.getOidNombreFecha() == NOMBRE_PERIODO_FIN
            || dto.getOidNombreFecha() == NOMBRE_PERIODO_INICIO
            || dto.getOidNombreFecha() == NOMBRE_CLASES_INICIO
            || dto.getOidNombreFecha() == NOMBRE_CLASES_FIN) {
            if (dto.getFechaFin() != null) {
                throw new FechaOperacionNoPermitidaException("No se permite fechaFin para esta fecha especial, solo fechaInicial.");
            }
        }
    }
    
    /**
     * Versión para validación al agregar (sin ID de exclusión).
     */
    private void validarAlAgregarFechaEspecial(FechaDTORequest dto, Integer oidCalendario) {
        if (dto.getOidNombreFecha() == NOMBRE_PERIODO_FIN
            || dto.getOidNombreFecha() == NOMBRE_PERIODO_INICIO
            || dto.getOidNombreFecha() == NOMBRE_CLASES_INICIO
            || dto.getOidNombreFecha() == NOMBRE_CLASES_FIN) {
            // No permitir fechaFin
            if (dto.getFechaFin() != null) {
                throw new FechaOperacionNoPermitidaException("No se permite fechaFin para este tipo de fecha especial, solo fechaInicial.");
            }

            // No permitir duplicados en el mismo calendario
            boolean yaExiste = fechaRepository.existsByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(
                oidCalendario, dto.getOidNombreFecha());
            if (yaExiste) {
                throw new FechaOperacionNoPermitidaException("Ya existe una fecha especial de este tipo para este calendario.");
            }
        }
}

}
