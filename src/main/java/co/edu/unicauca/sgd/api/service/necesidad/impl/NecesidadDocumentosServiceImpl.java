package co.edu.unicauca.sgd.api.service.necesidad.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.sgd.api.domain.Asignacion;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Fecha;
import co.edu.unicauca.sgd.api.domain.Materia;
import co.edu.unicauca.sgd.api.domain.Necesidad;
import co.edu.unicauca.sgd.api.domain.Plan;
import co.edu.unicauca.sgd.api.domain.Programa;
import co.edu.unicauca.sgd.api.domain.Seleccionado;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.enums.ContratacionEnum;
import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;
import co.edu.unicauca.sgd.api.exception.necesidad.NecesidadValidationException;
import co.edu.unicauca.sgd.api.repository.AsignacionRepository;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.DepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.FechaRepository;
import co.edu.unicauca.sgd.api.repository.NecesidadRepository;
import co.edu.unicauca.sgd.api.service.necesidad.NecesidadDocumentosService;

@Service
public class NecesidadDocumentosServiceImpl implements NecesidadDocumentosService {

    private static final Logger logger = LoggerFactory.getLogger(NecesidadDocumentosServiceImpl.class);
    private static final String TEMPLATE_PATH = "formatos/Formato_Necesidades.xlsx";
    private static final int HEADER_ROW_INDEX = 1;
    private static final int DATA_START_ROW_INDEX = 2;

    private final NecesidadRepository necesidadRepository;
    private final AsignacionRepository asignacionRepository;
    private final CalendarioRepository calendarioRepository;
    private final FechaRepository fechaRepository;
    private final DepartamentoRepository departamentoRepository;

    public NecesidadDocumentosServiceImpl(NecesidadRepository necesidadRepository,
                                          AsignacionRepository asignacionRepository,
                                          CalendarioRepository calendarioRepository,
                                          FechaRepository fechaRepository,
                                          DepartamentoRepository departamentoRepository) {
        this.necesidadRepository = necesidadRepository;
        this.asignacionRepository = asignacionRepository;
        this.calendarioRepository = calendarioRepository;
        this.fechaRepository = fechaRepository;
        this.departamentoRepository = departamentoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generarFormatoNecesidades(Integer oidCalendario, Integer oidDepartamento) throws IOException {
        logger.info("Generando formato de necesidades para oidCalendario {} y oidDepartamento {}", oidCalendario, oidDepartamento);

        Calendario calendario = calendarioRepository.findById(oidCalendario)
                .orElseThrow(() -> new NecesidadValidationException("No existe calendario con OID " + oidCalendario));

        validarDepartamentoSiAplica(oidDepartamento);
        List<Necesidad> necesidades = cargarNecesidades(oidCalendario, oidDepartamento);
        necesidades.sort(Comparator
                .comparing((Necesidad n) -> {
                    Materia materia = n.getMateria();
                    Plan plan = materia != null ? materia.getPlan() : null;
                    return obtenerNombrePrograma(plan != null ? plan.getPrograma() : null);
                }, Comparator.nullsLast(String::compareTo))
                .thenComparing(n -> n.getMateria() != null ? n.getMateria().getSemestre() : null,
                        Comparator.nullsLast(Integer::compareTo))
                .thenComparing(n -> n.getMateria() != null ? n.getMateria().getCodigo() : null,
                        Comparator.nullsLast(String::compareTo))
                .thenComparing(Necesidad::getGrupo, Comparator.nullsLast(String::compareTo)));

        Map<Integer, List<Asignacion>> asignacionesPorNecesidad = obtenerAsignacionesPorNecesidad(necesidades);

        try (InputStream templateStream = getClass().getClassLoader().getResourceAsStream(TEMPLATE_PATH);
             Workbook workbook = templateStream != null ? new XSSFWorkbook(templateStream) : new XSSFWorkbook()) {
            if (templateStream == null) {
                logger.warn("No se encontró plantilla {}, se creará un workbook vacío.", TEMPLATE_PATH);
            }

            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : workbook.createSheet("NECESIDADES");
            String sufijo = (oidDepartamento != null && oidDepartamento > 0) ? " (por dpto)" : " (todo)";
            workbook.setSheetName(workbook.getSheetIndex(sheet), "NECESIDADES" + sufijo);
            Sheet parametros = asegurarHojaParametros(workbook);
            sheet.createFreezePane(0, DATA_START_ROW_INDEX);
            sheet.setAutoFilter(new CellRangeAddress(HEADER_ROW_INDEX, HEADER_ROW_INDEX, 0, 12));

            CellStyle percentStyle = crearEstiloPorcentaje(workbook);
            String periodo = construirPeriodo(calendario);

            int rowIdx = DATA_START_ROW_INDEX;
            for (Necesidad necesidad : necesidades) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) {
                    row = sheet.createRow(rowIdx);
                }

                Materia materia = necesidad.getMateria();
                Plan plan = materia != null ? materia.getPlan() : null;
                Programa programa = plan != null ? plan.getPrograma() : null;

                crearCeldaTexto(row, 0, periodo);
                crearCeldaTexto(row, 1, obtenerNombrePrograma(programa));
                crearCeldaNumero(row, 2, materia != null ? materia.getSemestre() : null);
                crearCeldaTexto(row, 3, materia != null ? materia.getOidMateria() : null);
                crearCeldaTexto(row, 4, materia != null ? materia.getCodigo() : null);
                crearCeldaTexto(row, 5, materia != null ? materia.getNombre() : null);
                crearCeldaTexto(row, 6, necesidad.getGrupo());
                crearCeldaNumero(row, 7, necesidad.getCupo());
                crearCeldaNumero(row, 8, materia != null ? materia.getHorasSemana() : null);

                List<Asignacion> asignaciones = asignacionesPorNecesidad.getOrDefault(necesidad.getOidNecesidad(), List.of());
                Asignacion asignacion1 = asignaciones.size() > 0 ? asignaciones.get(0) : null;
                Asignacion asignacion2 = asignaciones.size() > 1 ? asignaciones.get(1) : null;

                crearCeldaTexto(row, 9, obtenerNombreDocente(asignacion1));
                crearCeldaPorcentaje(row, 10, calcularPorcentaje(asignacion1, asignaciones), percentStyle);
                crearCeldaTexto(row, 11, obtenerNombreDocente(asignacion2));
                crearCeldaPorcentaje(row, 12, calcularPorcentaje(asignacion2, asignaciones), percentStyle);

                rowIdx++;
            }

            poblarHojaParametros(parametros, calendario, oidCalendario, asignacionesPorNecesidad);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream;
        } catch (Exception e) {
            logger.error("Error generando el formato de necesidades", e);
            throw new IOException("Error generando el formato de necesidades", e);
        }
    }

    private List<Necesidad> cargarNecesidades(Integer oidCalendario, Integer oidDepartamento) {
        if (oidDepartamento != null && oidDepartamento > 0) {
            return necesidadRepository.findAllByCalendario_OidcalendarioAndMateria_Departamento_OidDepartamento(
                    oidCalendario, oidDepartamento);
        }
        return necesidadRepository.findAllByCalendario_Oidcalendario(oidCalendario);
    }

    private void validarDepartamentoSiAplica(Integer oidDepartamento) {
        if (oidDepartamento == null) {
            return;
        }
        if (oidDepartamento <= 0) {
            throw new NecesidadValidationException("El oidDepartamento debe ser mayor que cero.");
        }
        if (!departamentoRepository.existsById(oidDepartamento)) {
            throw new NecesidadValidationException("No existe departamento con OID " + oidDepartamento);
        }
    }

    private Map<Integer, List<Asignacion>> obtenerAsignacionesPorNecesidad(List<Necesidad> necesidades) {
        List<Integer> oids = necesidades.stream()
                .map(Necesidad::getOidNecesidad)
                .filter(Objects::nonNull)
                .toList();
        if (oids.isEmpty()) {
            return new HashMap<>();
        }
        List<Asignacion> asignaciones = asignacionRepository.findAllByNecesidad_OidNecesidadIn(oids);
        Map<Integer, List<Asignacion>> asignacionesPorNecesidad = new HashMap<>();
        for (Asignacion asignacion : asignaciones) {
            if (asignacion.getNecesidad() == null || asignacion.getNecesidad().getOidNecesidad() == null) {
                continue;
            }
            Integer oidNecesidad = asignacion.getNecesidad().getOidNecesidad();
            asignacionesPorNecesidad.computeIfAbsent(oidNecesidad, ignored -> new ArrayList<>()).add(asignacion);
        }
        asignacionesPorNecesidad.values()
                .forEach(list -> list.sort(Comparator.comparing(Asignacion::getOidAsignacion, Comparator.nullsLast(Integer::compareTo))));
        return asignacionesPorNecesidad;
    }

    private String construirPeriodo(Calendario calendario) {
        if (calendario == null) {
            return "";
        }
        String anio = calendario.getAnioCalendario();
        Integer numero = calendario.getNumeroCalendario();
        if (anio == null || anio.isBlank() || numero == null) {
            return "";
        }
        return anio.trim() + "." + numero;
    }

    private String obtenerNombrePrograma(Programa programa) {
        if (programa == null) {
            return "";
        }
        String nombreCorto = programa.getNombreCorto();
        if (nombreCorto != null && !nombreCorto.isBlank()) {
            return nombreCorto.trim();
        }
        return programa.getNombre() != null ? programa.getNombre().trim() : "";
    }

    private String obtenerNombreDocente(Asignacion asignacion) {
        if (asignacion == null) {
            return "";
        }
        Seleccionado seleccionado = asignacion.getSeleccionado();
        Usuario usuario = seleccionado != null ? seleccionado.getUsuario() : null;
        if (usuario == null) {
            return "";
        }
        String nombres = usuario.getNombres() != null ? usuario.getNombres().trim() : "";
        String apellidos = usuario.getApellidos() != null ? usuario.getApellidos().trim() : "";
        if (!nombres.isEmpty() || !apellidos.isEmpty()) {
            return (nombres + " " + apellidos).trim();
        }
        String correo = usuario.getCorreo() != null ? usuario.getCorreo().trim() : "";
        if (!correo.isEmpty()) {
            return correo;
        }
        Integer oidUsuario = usuario.getOidUsuario();
        return oidUsuario != null ? "Usuario " + oidUsuario : "";
    }

    private Double calcularPorcentaje(Asignacion asignacion, List<Asignacion> asignaciones) {
        if (asignacion == null || asignaciones == null || asignaciones.isEmpty()) {
            return null;
        }
        double totalHoras = asignaciones.stream()
                .mapToDouble(a -> a.getHorasDocencia() != null ? a.getHorasDocencia().doubleValue() : 0d)
                .sum();
        if (totalHoras > 0d) {
            Double horasAsignacion = asignacion.getHorasDocencia() != null ? asignacion.getHorasDocencia().doubleValue() : null;
            return horasAsignacion != null ? horasAsignacion / totalHoras : null;
        }
        return 1d / asignaciones.size();
    }

    private CellStyle crearEstiloPorcentaje(Workbook workbook) {
        DataFormat format = workbook.createDataFormat();
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(format.getFormat("0%"));
        return style;
    }

    private void crearCeldaTexto(Row row, int column, String value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        if (value != null && !value.isBlank()) {
            cell.setCellValue(value);
        }
    }

    private void crearCeldaNumero(Row row, int column, Integer value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        if (value != null) {
            cell.setCellValue(value);
        }
    }

    private void crearCeldaPorcentaje(Row row, int column, Double value, CellStyle style) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        if (value != null) {
            cell.setCellValue(value);
            cell.setCellStyle(style);
        }
    }

    private Sheet asegurarHojaParametros(Workbook workbook) {
        Sheet parametros = workbook.getSheet("parametros");
        if (parametros == null) {
            parametros = workbook.createSheet("parametros");
        } else {
            workbook.setSheetName(workbook.getSheetIndex(parametros), "parametros");
        }
        return parametros;
    }

    private void poblarHojaParametros(Sheet sheet,
                                      Calendario calendario,
                                      Integer oidCalendario,
                                      Map<Integer, List<Asignacion>> asignacionesPorNecesidad) {
        int rowIdx = 0;

        Row row = sheet.createRow(rowIdx++);
        crearCeldaTexto(row, 0, "Calendario");
        crearCeldaTexto(row, 1, construirPeriodo(calendario));

        LocalDate inicioPeriodo = obtenerFechaEspecial(oidCalendario, 1);
        LocalDate finPeriodo = obtenerFechaEspecial(oidCalendario, 10);
        LocalDate inicioClases = obtenerFechaEspecial(oidCalendario, 3);
        LocalDate finClases = obtenerFechaEspecial(oidCalendario, 7);

        row = sheet.createRow(rowIdx++);
        crearCeldaTexto(row, 0, "Inicio calendario");
        crearCeldaTexto(row, 1, formatoFecha(inicioPeriodo));
        row = sheet.createRow(rowIdx++);
        crearCeldaTexto(row, 0, "Fin calendario");
        crearCeldaTexto(row, 1, formatoFecha(finPeriodo));
        row = sheet.createRow(rowIdx++);
        crearCeldaTexto(row, 0, "Inicio clases");
        crearCeldaTexto(row, 1, formatoFecha(inicioClases));
        row = sheet.createRow(rowIdx++);
        crearCeldaTexto(row, 0, "Fin clases");
        crearCeldaTexto(row, 1, formatoFecha(finClases));

        rowIdx++; // separador

        Map<ContratacionEnum, Double> horasDocencia = obtenerHorasDocenciaPorContratacion(asignacionesPorNecesidad);
        Map<TipoFechaEnum, Long> semanasPorTipo = obtenerSemanasPorTipo(oidCalendario);

        row = sheet.createRow(rowIdx++);
        crearCeldaTexto(row, 0, "Semanas por tipo de profesor");
        row = sheet.createRow(rowIdx++);
        crearCeldaTexto(row, 0, "Tipo");
        crearCeldaTexto(row, 1, "Semanas");

        for (Map.Entry<TipoFechaEnum, Long> entry : semanasPorTipo.entrySet()) {
            ContratacionEnum contratacion = mapearContratacion(entry.getKey());
            if (contratacion == null || !horasDocencia.containsKey(contratacion)) {
                continue;
            }
            row = sheet.createRow(rowIdx++);
            crearCeldaTexto(row, 0, nombreTipo(entry.getKey()));
            crearCeldaNumero(row, 1, entry.getValue() != null ? entry.getValue().intValue() : null);
        }

        rowIdx++; // separador

        row = sheet.createRow(rowIdx++);
        crearCeldaTexto(row, 0, "Profesores asignados");
        row = sheet.createRow(rowIdx++);
        crearCeldaTexto(row, 0, "Nombre");
        crearCeldaTexto(row, 1, "Tipo contratacion");
        crearCeldaTexto(row, 2, "Hrs docencia");

        List<DocenteInfo> docentes = construirDocentes(asignacionesPorNecesidad);
        docentes.sort(Comparator.comparing(info -> info.nombre, Comparator.nullsLast(String::compareTo)));
        for (DocenteInfo docente : docentes) {
            row = sheet.createRow(rowIdx++);
            crearCeldaTexto(row, 0, docente.nombre);
            crearCeldaTexto(row, 1, docente.tipoContratacion);
            crearCeldaNumeroDecimal(row, 2, docente.horasDocencia);
        }
    }

    private LocalDate obtenerFechaEspecial(Integer oidCalendario, Integer oidNombreFecha) {
        return fechaRepository.findByCalendario_OidcalendarioAndNombreFecha_OidNombreFecha(oidCalendario, oidNombreFecha)
                .map(Fecha::getFechaInicial)
                .map(LocalDateTime::toLocalDate)
                .orElse(null);
    }

    private String formatoFecha(LocalDate fecha) {
        return fecha != null ? fecha.toString() : "";
    }

    private Map<TipoFechaEnum, Long> obtenerSemanasPorTipo(Integer oidCalendario) {
        Map<TipoFechaEnum, Long> semanas = new LinkedHashMap<>();
        List<Fecha> fechas = fechaRepository.findByCalendario_Oidcalendario(oidCalendario);
        for (TipoFechaEnum tipo : List.of(TipoFechaEnum.PLANTA, TipoFechaEnum.OCASIONAL, TipoFechaEnum.CATEDRA,
                TipoFechaEnum.BECARIO_Y_PRACTICANTE)) {
            Fecha fecha = fechas.stream().filter(f -> f.getTipo() == tipo).findFirst().orElse(null);
            semanas.put(tipo, calcularSemanas(fecha));
        }
        return semanas;
    }

    private Long calcularSemanas(Fecha fecha) {
        if (fecha == null || fecha.getFechaInicial() == null || fecha.getFechaFin() == null) {
            return null;
        }
        return ChronoUnit.WEEKS.between(fecha.getFechaInicial().toLocalDate(), fecha.getFechaFin().toLocalDate()) + 1;
    }

    private Map<ContratacionEnum, Double> obtenerHorasDocenciaPorContratacion(
            Map<Integer, List<Asignacion>> asignacionesPorNecesidad) {
        Map<ContratacionEnum, Double> horas = new HashMap<>();
        for (List<Asignacion> asignaciones : asignacionesPorNecesidad.values()) {
            for (Asignacion asignacion : asignaciones) {
                Seleccionado seleccionado = asignacion.getSeleccionado();
                ContratacionEnum tipo = seleccionado != null ? seleccionado.getTipo() : null;
                if (tipo == null) {
                    continue;
                }
                double horasDocencia = asignacion.getHorasDocencia() != null ? asignacion.getHorasDocencia() : 0d;
                horas.merge(tipo, horasDocencia, Double::sum);
            }
        }
        return horas;
    }

    private List<DocenteInfo> construirDocentes(Map<Integer, List<Asignacion>> asignacionesPorNecesidad) {
        Map<Integer, DocenteInfo> docentes = new HashMap<>();
        for (List<Asignacion> asignaciones : asignacionesPorNecesidad.values()) {
            for (Asignacion asignacion : asignaciones) {
                Seleccionado seleccionado = asignacion.getSeleccionado();
                if (seleccionado == null || seleccionado.getOidSeleccionado() == null) {
                    continue;
                }
                int oidSeleccionado = seleccionado.getOidSeleccionado();
                DocenteInfo info = docentes.computeIfAbsent(oidSeleccionado, ignored -> {
                    DocenteInfo nuevo = new DocenteInfo();
                    nuevo.nombre = obtenerNombreDocente(asignacion);
                    ContratacionEnum tipo = seleccionado.getTipo();
                    nuevo.tipoContratacion = tipo != null ? tipo.getValor() : "";
                    nuevo.horasDocencia = 0d;
                    return nuevo;
                });
                info.horasDocencia += asignacion.getHorasDocencia() != null ? asignacion.getHorasDocencia() : 0d;
            }
        }
        return new ArrayList<>(docentes.values());
    }

    private String nombreTipo(TipoFechaEnum tipo) {
        if (tipo == null) {
            return "";
        }
        return switch (tipo) {
            case PLANTA -> "PLANTA";
            case OCASIONAL -> "OCASIONAL";
            case CATEDRA -> "CÁTEDRA";
            case BECARIO_Y_PRACTICANTE -> "BECARIO Y PRACTICANTE";
            default -> tipo.name();
        };
    }

    private ContratacionEnum mapearContratacion(TipoFechaEnum tipo) {
        if (tipo == null) {
            return null;
        }
        return switch (tipo) {
            case PLANTA -> ContratacionEnum.PLANTA;
            case OCASIONAL -> ContratacionEnum.OCASIONAL;
            case CATEDRA -> ContratacionEnum.CATEDRA;
            case BECARIO_Y_PRACTICANTE -> ContratacionEnum.BECARIOS_Y_PRACTICANTES;
            default -> null;
        };
    }

    private void crearCeldaNumeroDecimal(Row row, int column, Double value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        if (value != null) {
            cell.setCellValue(value);
        }
    }

    private static class DocenteInfo {
        private String nombre;
        private String tipoContratacion;
        private Double horasDocencia;
    }
}
