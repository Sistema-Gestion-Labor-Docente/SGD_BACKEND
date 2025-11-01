package co.edu.unicauca.sgd.api.service.calendario.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTOResponse;
import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;
import co.edu.unicauca.sgd.api.service.calendario.CalendarioPdfService;
import co.edu.unicauca.sgd.api.utils.StringUtils;

@Service
public class CalendarioPdfServiceImpl implements CalendarioPdfService {

    private static final Logger logger = LoggerFactory.getLogger(CalendarioPdfServiceImpl.class);
    private static final String CALENDARIO_TEMPLATE_PATH = "formatos/Formato_CalendarioPDF.html";
    private static final String FACULTAD_DEFAULT = "Facultad de Ingenier\u00EDa Electr\u00F3nica y Telecomunicaciones";
    private static final Locale LOCALE_ES_CO = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", LOCALE_ES_CO);

    @Override
    public ByteArrayOutputStream generarCalendarioPdf(Calendario calendario, List<FechaDTOResponse> fechas) throws IOException {
        if (calendario == null) {
            throw new IllegalArgumentException("El calendario no puede ser nulo.");
        }
        logger.info("Generando PDF del calendario ID: {}", calendario.getOidcalendario());
        String html = construirHtmlCalendario(calendario, fechas != null ? fechas : List.of());
        try {
            return renderizarPdf(html);
        } catch (Exception e) {
            logger.error("Error renderizando el PDF del calendario {}: {}", calendario.getOidcalendario(), e.getMessage(), e);
            throw new IOException("No se pudo renderizar el PDF del calendario.", e);
        }
    }

    private String construirHtmlCalendario(Calendario calendario, List<FechaDTOResponse> fechas) throws IOException {
        String plantilla = cargarPlantillaCalendario();
        String periodo = crearDescripcionPeriodo(calendario);
        String intro = construirIntro(calendario, periodo);
        String filas = construirFilasCalendario(fechas);
        String notas = construirNotasCalendario(calendario);
        String footer = construirFooter(calendario);

        return plantilla
                .replace("{{CALENDARIO_TITULO}}", escapeText("Calendario de actividades"))
                .replace("{{CALENDARIO_FACULTAD}}", escapeText(FACULTAD_DEFAULT))
                .replace("{{CALENDARIO_PERIODO}}", escapeText(periodo.toUpperCase()))
                .replace("{{CALENDARIO_INTRO}}", intro)
                .replace("{{CALENDARIO_FILAS}}", filas)
                .replace("{{CALENDARIO_NOTAS}}", notas)
                .replace("{{CALENDARIO_FOOTER}}", footer);
    }

    private String construirIntro(Calendario calendario, String periodo) {
        if (StringUtils.hasText(calendario.getObservacion())) {
            String normalizada = calendario.getObservacion()
                    .replace("\r\n", "\n")
                    .replace("\r", "\n");
            return escapeText(normalizada).replace("\n", "<br />");
        }
        return escapeText("Calendario acad\u00E9mico correspondiente al " + periodo + ".");
    }

    private String crearDescripcionPeriodo(Calendario calendario) {
        String anio = StringUtils.hasText(calendario.getAnioCalendario()) ? calendario.getAnioCalendario() : "-";
        String numero = calendario.getNumeroCalendario() != null ? calendario.getNumeroCalendario().toString() : "-";
        return "Per\u00EDodo acad\u00E9mico " + anio + " - " + numero;
    }

    private String construirFilasCalendario(List<FechaDTOResponse> fechas) {
        if (fechas == null || fechas.isEmpty()) {
            return "<tr><td class=\"col-label\">Sin actividades registradas</td><td class=\"col-date\">-</td></tr>";
        }

        return fechas.stream()
                .sorted(Comparator
                        .comparing(this::obtenerFechaOrden, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(f -> textoParaOrdenar(f.getNombre()), String.CASE_INSENSITIVE_ORDER))
                .map(this::construirFila)
                .collect(Collectors.joining("\n"));
    }

    private String construirFila(FechaDTOResponse fecha) {
        boolean esResaltada = fecha != null && fecha.getTipo() == TipoFechaEnum.RESALTADAS;

        String nombre = escapeTextOrDash(fecha != null ? fecha.getNombre() : null);
        String rango = escapeTextOrDash(construirRangoFechas(fecha));

        if (esResaltada) {
            nombre = envolverEnFuerte(nombre);
            rango = envolverEnFuerte(rango);
        }

        return new StringBuilder()
                .append("<tr>")
                .append("<td class=\"col-label\">").append(nombre).append("</td>")
                .append("<td class=\"col-date\">").append(rango).append("</td>")
                .append("</tr>")
                .toString();
    }

    private LocalDateTime obtenerFechaOrden(FechaDTOResponse fecha) {
        if (fecha == null) {
            return null;
        }
        return fecha.getFechaInicial() != null ? fecha.getFechaInicial() : fecha.getFechaFin();
    }

    private String construirRangoFechas(FechaDTOResponse fecha) {
        if (fecha == null) {
            return "-";
        }
        LocalDate inicio = fecha.getFechaInicial() != null ? fecha.getFechaInicial().toLocalDate() : null;
        LocalDate fin = fecha.getFechaFin() != null ? fecha.getFechaFin().toLocalDate() : null;

        if (inicio != null && fin != null) {
            if (inicio.equals(fin) || Boolean.TRUE.equals(fecha.getUniqueDate())) {
                return formatearFecha(inicio);
            }
            return "Del " + formatearFecha(inicio) + " al " + formatearFecha(fin);
        }
        if (inicio != null) {
            return formatearFecha(inicio);
        }
        if (fin != null) {
            return "Hasta " + formatearFecha(fin);
        }
        return "-";
    }

    private String formatearFecha(LocalDate fecha) {
        return fecha != null ? DATE_FORMATTER.format(fecha) : "";
    }

    private String formatearFecha(LocalDateTime fecha) {
        return fecha != null ? DATE_FORMATTER.format(fecha.toLocalDate()) : "";
    }

    private String construirNotasCalendario(Calendario calendario) {
        List<String> notas = new ArrayList<>();
        agregarNota(notas, "Semanas de clase", calendario.getSemanasClase());
        agregarNota(notas, "Semanas de preparaci\u00F3n", calendario.getSemanasPreparacion());
        agregarNota(notas, "Horas planta", calendario.getHorasPlanta());
        agregarNota(notas, "Horas c\u00E1tedra", calendario.getHorasCatedra());
        agregarNota(notas, "Horas ocasionales", calendario.getHorasOcasionales());
        agregarNota(notas, "Horas becario y practicante", calendario.getHorasBecarioPracticante());

        if (StringUtils.hasText(calendario.getEstado())) {
            notas.add("<li><strong>Estado:</strong> " + escapeText(calendario.getEstado()) + "</li>");
        }

        if (notas.isEmpty()) {
            return "";
        }

        return "<ul>" + String.join("", notas) + "</ul>";
    }

    private void agregarNota(List<String> notas, String etiqueta, Float valor) {
        if (valor == null) {
            return;
        }
        notas.add("<li><strong>" + escapeText(etiqueta) + ":</strong> " + escapeText(formatearNumero(valor)) + "</li>");
    }

    private String formatearNumero(Float valor) {
        if (valor == null) {
            return "";
        }
        if (Math.abs(valor - Math.round(valor)) < 0.001f) {
            return String.valueOf(Math.round(valor));
        }
        return String.format(Locale.US, "%.2f", valor);
    }

    private String construirFooter(Calendario calendario) {
        LocalDateTime referencia = calendario.getFechaActualizacion() != null
                ? calendario.getFechaActualizacion()
                : calendario.getFechaCreacion();
        if (referencia == null) {
            referencia = LocalDateTime.now();
        }
        String estado = StringUtils.hasText(calendario.getEstado()) ? calendario.getEstado() : "Sin estado";
        String responsable = StringUtils.hasText(calendario.getUsuarioCreacion()) ? calendario.getUsuarioCreacion() : "Sistema";

        return "Generado el " + formatearFecha(referencia)
                + " | Estado: " + escapeText(estado)
                + " | Responsable: " + escapeText(responsable);
    }

    private String cargarPlantillaCalendario() throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CALENDARIO_TEMPLATE_PATH)) {
            if (inputStream == null) {
                throw new IOException("No se encontr\u00F3 la plantilla HTML del calendario en " + CALENDARIO_TEMPLATE_PATH);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private ByteArrayOutputStream renderizarPdf(String html) throws Exception {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(salida);
        builder.run();
        return salida;
    }

    private String escapeText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String escapeTextOrDash(String value) {
        String escaped = escapeText(value);
        return escaped.isEmpty() ? "-" : escaped;
    }

    private String envolverEnFuerte(String contenido) {
        return "<strong>" + contenido + "</strong>";
    }

    private String textoParaOrdenar(String valor) {
        if (!StringUtils.hasText(valor)) {
            return "";
        }
        return valor;
    }
}
