package co.edu.unicauca.sgd.api.service.reportes.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import co.edu.unicauca.sgd.api.dto.reportes.ReporteDatoGraficoRequest;
import co.edu.unicauca.sgd.api.dto.reportes.ReporteEstadisticasPdfSeleccionRequest;
import co.edu.unicauca.sgd.api.dto.reportes.ReporteGraficoPdfRequest;
import co.edu.unicauca.sgd.api.service.reportes.EstadisticasPdfService;
import co.edu.unicauca.sgd.api.utils.StringUtils;

@Service
public class EstadisticasPdfServiceImpl implements EstadisticasPdfService {

    private static final Logger logger = LoggerFactory.getLogger(EstadisticasPdfServiceImpl.class);
    private static final String TEMPLATE_PATH = "formatos/Formato_EstadisticasPDF.html";
    private static final Locale LOCALE_ES_CO = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", LOCALE_ES_CO);

    private static final int CHART_WIDTH = 900;
    private static final int CHART_HEIGHT = 360;

    @Override
    public ByteArrayOutputStream generarReportePdf(ReporteEstadisticasPdfSeleccionRequest request) throws IOException {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de reporte no puede ser nula.");
        }
        String html = construirHtml(request);
        try {
            return renderizarPdf(html);
        } catch (Exception e) {
            logger.error("Error renderizando el PDF de estadisticas: {}", e.getMessage(), e);
            throw new IOException("No se pudo renderizar el PDF de estadisticas.", e);
        }
    }

    private String construirHtml(ReporteEstadisticasPdfSeleccionRequest request) throws IOException {
        String plantilla = cargarPlantilla();
        String titulo = "Reporte de estadisticas academicas";
        String subtitulo = "Resumen ejecutivo de indicadores";
        String meta = construirMeta(request);
        String footer = "Reporte generado automaticamente por el sistema SGD";
        String secciones = construirSeccionesDesdeCatalogo(request.getGraficos());

        return plantilla
                .replace("{{REPORTE_TITULO}}", escapeText(titulo))
                .replace("{{REPORTE_SUBTITULO}}", escapeText(subtitulo))
                .replace("{{REPORTE_META}}", escapeText(meta))
                .replace("{{REPORTE_SECCIONES}}", secciones)
                .replace("{{REPORTE_FOOTER}}", escapeText(footer));
    }

    private String construirSeccionesDesdeCatalogo(List<String> graficosSolicitados) {
        if (graficosSolicitados == null || graficosSolicitados.isEmpty()) {
            return construirSeccionVacia();
        }
        StringBuilder builder = new StringBuilder();
        int indice = 1;
        for (String idGrafico : graficosSolicitados) {
            ReporteGraficoPdfRequest grafico = construirGraficoPorId(idGrafico);
            builder.append(construirSeccionGrafico(grafico, indice));
            indice++;
        }
        return builder.toString();
    }

    private String construirSeccionVacia() {
        return new StringBuilder()
                .append("<div class=\"section\">")
                .append("<h2 class=\"section-title\">Sin graficos disponibles</h2>")
                .append("<p class=\"section-summary\">No se recibio informacion para generar graficos.</p>")
                .append("<div class=\"chart\">")
                .append("<img src=\"").append(generarGraficoVacioBase64("Sin datos para graficar")).append("\" ")
                .append("alt=\"Grafico vacio\" />")
                .append("</div>")
                .append("</div>")
                .toString();
    }

    private String construirSeccionGrafico(ReporteGraficoPdfRequest grafico, int indice) {
        String titulo = grafico != null ? valorOrDefault(grafico.getTitulo(), "Grafico " + indice) : "Grafico " + indice;
        String resumen = grafico != null ? valorOrDefault(grafico.getResumen(), "Sin descripcion disponible.") : "Sin descripcion disponible.";
        String resumenHtml = escapeText(resumen).replace("\n", "<br />");
        String imagen = generarGraficoBase64(grafico != null ? grafico.getDatos() : null, titulo);

        return new StringBuilder()
                .append("<div class=\"section\">")
                .append("<h2 class=\"section-title\">").append(escapeText(titulo)).append("</h2>")
                .append("<p class=\"section-summary\">").append(resumenHtml).append("</p>")
                .append("<div class=\"chart\">")
                .append("<img src=\"").append(imagen).append("\" alt=\"").append(escapeText(titulo)).append("\" />")
                .append("</div>")
                .append("</div>")
                .toString();
    }

    private String generarGraficoBase64(List<ReporteDatoGraficoRequest> datos, String titulo) {
        if (datos == null || datos.isEmpty()) {
            return generarGraficoVacioBase64("Sin datos disponibles");
        }
        BufferedImage chart = renderizarGraficoBarras(datos, titulo);
        return convertirImagenBase64(chart);
    }

    private ReporteGraficoPdfRequest construirGraficoPorId(String idGrafico) {
        if (!StringUtils.hasText(idGrafico)) {
            return crearGraficoGenerico("Grafico", "Sin descripcion disponible.", List.of());
        }
        String id = idGrafico.trim().toLowerCase();
        switch (id) {
            case "carga_actividades":
                return crearGraficoGenerico(
                        "Carga de actividades por usuario",
                        "Muestra el total de actividades por responsable para identificar sobrecargas.",
                        List.of(
                                crearDato("Ana", 18d),
                                crearDato("Luis", 12d),
                                crearDato("Maria", 22d),
                                crearDato("Carlos", 9d)));
            case "ocupacion_cupos":
                return crearGraficoGenerico(
                        "Ocupacion de horas vs. cupo permitido",
                        "Compara horas asignadas frente al cupo disponible por cargo y persona.",
                        List.of(
                                crearDato("Planta", 78d),
                                crearDato("Catedra", 92d),
                                crearDato("Ocasional", 64d)));
            case "cobertura_docente":
                return crearGraficoGenerico(
                        "Cobertura docente por semestre",
                        "Presenta horas planificadas y asignadas por semestre para detectar vacantes.",
                        List.of(
                                crearDato("2025-1", 85d),
                                crearDato("2025-2", 73d),
                                crearDato("2026-1", 91d)));
            case "demanda_necesidades":
                return crearGraficoGenerico(
                        "Demanda de necesidades por programa",
                        "Muestra las necesidades pendientes por programa y materia.",
                        List.of(
                                crearDato("Ingenieria", 34d),
                                crearDato("Telematica", 21d),
                                crearDato("Sistemas", 28d)));
            case "flujo_necesidades":
                return crearGraficoGenerico(
                        "Flujo de necesidades y tiempos de aprobacion",
                        "Resume la cantidad de necesidades por estado y tiempos promedio.",
                        List.of(
                                crearDato("Borrador", 40d),
                                crearDato("Revision", 22d),
                                crearDato("No asignada", 18d)));
            case "cobertura_necesidades_actividades":
                return crearGraficoGenerico(
                        "Cobertura de necesidades vs. actividades",
                        "Muestra necesidades cubiertas frente a pendientes por asignar.",
                        List.of(
                                crearDato("Cubiertas", 62d),
                                crearDato("Pendientes", 15d)));
            default:
                return crearGraficoGenerico(
                        "Grafico no reconocido",
                        "El identificador solicitado no existe en el catalogo.",
                        List.of());
        }
    }

    private ReporteGraficoPdfRequest crearGraficoGenerico(String titulo, String resumen,
            List<ReporteDatoGraficoRequest> datos) {
        ReporteGraficoPdfRequest grafico = new ReporteGraficoPdfRequest();
        grafico.setTitulo(titulo);
        grafico.setResumen(resumen);
        grafico.setDatos(datos);
        return grafico;
    }

    private ReporteDatoGraficoRequest crearDato(String etiqueta, Double valor) {
        ReporteDatoGraficoRequest dato = new ReporteDatoGraficoRequest();
        dato.setEtiqueta(etiqueta);
        dato.setValor(valor);
        return dato;
    }

    private String generarGraficoVacioBase64(String mensaje) {
        BufferedImage chart = new BufferedImage(CHART_WIDTH, CHART_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = chart.createGraphics();
        configurarCalidad(g2d);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, CHART_WIDTH, CHART_HEIGHT);
        g2d.setColor(new Color(95, 108, 123));
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 18));
        FontMetrics metrics = g2d.getFontMetrics();
        int x = (CHART_WIDTH - metrics.stringWidth(mensaje)) / 2;
        int y = CHART_HEIGHT / 2;
        g2d.drawString(mensaje, Math.max(x, 10), y);
        g2d.dispose();
        return convertirImagenBase64(chart);
    }

    private BufferedImage renderizarGraficoBarras(List<ReporteDatoGraficoRequest> datos, String titulo) {
        BufferedImage chart = new BufferedImage(CHART_WIDTH, CHART_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = chart.createGraphics();
        configurarCalidad(g2d);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, CHART_WIDTH, CHART_HEIGHT);

        int paddingLeft = 60;
        int paddingRight = 30;
        int paddingTop = 50;
        int paddingBottom = 70;

        int plotWidth = CHART_WIDTH - paddingLeft - paddingRight;
        int plotHeight = CHART_HEIGHT - paddingTop - paddingBottom;

        double maxValor = datos.stream()
                .map(dato -> dato != null && dato.getValor() != null ? dato.getValor() : 0d)
                .max(Double::compareTo)
                .orElse(0d);
        if (maxValor <= 0) {
            maxValor = 1d;
        }

        g2d.setColor(new Color(15, 76, 92));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics titleMetrics = g2d.getFontMetrics();
        int titleX = (CHART_WIDTH - titleMetrics.stringWidth(titulo)) / 2;
        g2d.drawString(titulo, Math.max(titleX, 10), 30);

        g2d.setColor(new Color(175, 189, 205));
        g2d.setStroke(new BasicStroke(1.2f));
        int axisX = paddingLeft;
        int axisY = paddingTop + plotHeight;
        g2d.drawLine(axisX, paddingTop, axisX, axisY);
        g2d.drawLine(axisX, axisY, paddingLeft + plotWidth, axisY);

        int cantidad = datos.size();
        int gap = Math.max(8, plotWidth / (cantidad * 12));
        int barWidth = (plotWidth - (gap * (cantidad - 1))) / Math.max(cantidad, 1);

        Color[] colores = new Color[] {
                new Color(15, 76, 92),
                new Color(68, 98, 133),
                new Color(95, 143, 159),
                new Color(120, 168, 146),
                new Color(176, 196, 122)
        };

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics labelMetrics = g2d.getFontMetrics();

        for (int i = 0; i < cantidad; i++) {
            ReporteDatoGraficoRequest dato = datos.get(i);
            double valor = dato != null && dato.getValor() != null ? dato.getValor() : 0d;
            String etiqueta = dato != null && StringUtils.hasText(dato.getEtiqueta())
                    ? dato.getEtiqueta()
                    : "Item " + (i + 1);

            int barHeight = (int) Math.round((valor / maxValor) * plotHeight);
            int x = paddingLeft + (i * (barWidth + gap));
            int y = paddingTop + plotHeight - barHeight;

            g2d.setColor(colores[i % colores.length]);
            g2d.fillRect(x, y, barWidth, barHeight);

            g2d.setColor(new Color(45, 55, 72));
            String valorTexto = formatearNumero(valor);
            int valorX = x + (barWidth - labelMetrics.stringWidth(valorTexto)) / 2;
            int valorY = Math.max(y - 6, paddingTop - 6);
            g2d.drawString(valorTexto, Math.max(valorX, 5), valorY);

            int labelX = x + (barWidth - labelMetrics.stringWidth(etiqueta)) / 2;
            int labelY = axisY + 20;
            g2d.drawString(etiqueta, Math.max(labelX, 5), labelY);
        }

        g2d.dispose();
        return chart;
    }

    private void configurarCalidad(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private String convertirImagenBase64(BufferedImage imagen) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(imagen, "png", output);
            String base64 = Base64.getEncoder().encodeToString(output.toByteArray());
            return "data:image/png;base64," + base64;
        } catch (IOException e) {
            logger.error("No se pudo convertir el grafico a base64: {}", e.getMessage(), e);
            return "";
        }
    }

    private String cargarPlantilla() throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(TEMPLATE_PATH)) {
            if (inputStream == null) {
                throw new IOException("No se encontro la plantilla HTML en " + TEMPLATE_PATH);
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

    private String valorOrDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private String construirMeta(ReporteEstadisticasPdfSeleccionRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("Generado el ").append(formatearFecha(LocalDate.now()));
        if (request.getOidCalendario() != null) {
            builder.append(" | Calendario ").append(request.getOidCalendario());
        }
        if (request.getOidDepartamento() != null) {
            builder.append(" | Departamento ").append(request.getOidDepartamento());
        }
        if (request.getOidPrograma() != null) {
            builder.append(" | Programa ").append(request.getOidPrograma());
        }
        return builder.toString();
    }

    private String formatearNumero(Double valor) {
        if (valor == null) {
            return "0";
        }
        if (Math.abs(valor - Math.round(valor)) < 0.001d) {
            return String.valueOf(Math.round(valor));
        }
        return String.format(Locale.US, "%.2f", valor);
    }

    private String formatearFecha(LocalDate fecha) {
        return fecha != null ? DATE_FORMATTER.format(fecha) : "";
    }
}
