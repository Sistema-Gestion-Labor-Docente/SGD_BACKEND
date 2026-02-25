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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import co.edu.unicauca.sgd.api.domain.Asignacion;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Necesidad;
import co.edu.unicauca.sgd.api.domain.Programa;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.dto.reportes.ReporteDatoGraficoRequest;
import co.edu.unicauca.sgd.api.dto.reportes.ReporteEstadisticasPdfSeleccionRequest;
import co.edu.unicauca.sgd.api.dto.reportes.ReporteGraficoPdfRequest;
import co.edu.unicauca.sgd.api.enums.ContratacionEnum;
import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;
import co.edu.unicauca.sgd.api.repository.AsignacionRepository;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.NecesidadRepository;
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

    private final NecesidadRepository necesidadRepository;
    private final AsignacionRepository asignacionRepository;
    private final CalendarioRepository calendarioRepository;

    public EstadisticasPdfServiceImpl(NecesidadRepository necesidadRepository,
            AsignacionRepository asignacionRepository,
            CalendarioRepository calendarioRepository) {
        this.necesidadRepository = necesidadRepository;
        this.asignacionRepository = asignacionRepository;
        this.calendarioRepository = calendarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
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
        String secciones = construirSeccionesDesdeCatalogo(request);

        return plantilla
                .replace("{{REPORTE_TITULO}}", escapeText(titulo))
                .replace("{{REPORTE_SUBTITULO}}", escapeText(subtitulo))
                .replace("{{REPORTE_META}}", escapeText(meta))
                .replace("{{REPORTE_SECCIONES}}", secciones)
                .replace("{{REPORTE_FOOTER}}", escapeText(footer));
    }

    private String construirSeccionesDesdeCatalogo(ReporteEstadisticasPdfSeleccionRequest request) {
        List<String> graficosSolicitados = request.getGraficos();
        if (graficosSolicitados == null || graficosSolicitados.isEmpty()) {
            return construirSeccionVacia();
        }
        EstadisticasContext context = cargarContexto(request);
        StringBuilder builder = new StringBuilder();
        int indice = 1;
        for (String idGrafico : graficosSolicitados) {
            ReporteGraficoPdfRequest grafico = construirGraficoPorId(idGrafico, context);
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
        String descripcion = grafico != null ? valorOrDefault(grafico.getDescripcion(), grafico.getResumen()) : null;
        descripcion = valorOrDefault(descripcion, "Sin descripcion disponible.");
        String aporte = grafico != null ? valorOrDefault(grafico.getAporte(), "Sin informacion adicional.") : "Sin informacion adicional.";
        String descripcionHtml = escapeText(descripcion).replace("\n", "<br />");
        String aporteHtml = escapeText(aporte).replace("\n", "<br />");
        String imagen = generarGraficoBase64(grafico != null ? grafico.getDatos() : null, titulo);

        return new StringBuilder()
                .append("<div class=\"section\">")
                .append("<h2 class=\"section-title\">").append(escapeText(titulo)).append("</h2>")
                .append("<div class=\"section-detail\">")
                .append("<span class=\"section-label\">Descripcion:</span> ").append(descripcionHtml)
                .append("</div>")
                .append("<div class=\"section-detail\">")
                .append("<span class=\"section-label\">Aporte:</span> ").append(aporteHtml)
                .append("</div>")
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

    private ReporteGraficoPdfRequest construirGraficoPorId(String idGrafico, EstadisticasContext context) {
        if (!StringUtils.hasText(idGrafico)) {
            return crearGraficoGenerico("Grafico", "Sin descripcion disponible.", "Sin informacion adicional.", List.of());
        }
        String id = idGrafico.trim().toLowerCase();
        switch (id) {
            case "carga_actividades":
                return crearGraficoGenerico(
                        "Carga de actividades por usuario",
                        "Muestra el total de asignaciones registradas por docente.",
                        "Permite identificar docentes con mayor carga y priorizar ajustes de distribucion.",
                        construirCargaActividades(context));
            case "ocupacion_cupos":
                return crearGraficoGenerico(
                        "Ocupacion de horas vs. cupo permitido",
                        "Resume las horas asignadas por tipo de contratacion (cupo del calendario cuando aplica).",
                        "Apoya el control de cupos y el balance de horas por modalidad.",
                        construirOcupacionCupos(context));
            case "cobertura_docente":
                return crearGraficoGenerico(
                        "Cobertura docente por semestre",
                        "Porcentaje de necesidades cubiertas por semestre.",
                        "Indica niveles de cobertura y posibles brechas por semestre.",
                        construirCoberturaDocente(context));
            case "demanda_necesidades":
                return crearGraficoGenerico(
                        "Demanda de necesidades por programa",
                        "Muestra la cantidad de necesidades registradas por programa.",
                        "Ayuda a priorizar programas con mayor demanda.",
                        construirDemandaNecesidades(context));
            case "flujo_necesidades":
                return crearGraficoGenerico(
                        "Flujo de necesidades y tiempos de aprobacion",
                        "Resume la cantidad de necesidades por estado.",
                        "Permite monitorear el avance y detectar cuellos de botella.",
                        construirFlujoNecesidades(context));
            case "cobertura_necesidades_actividades":
                return crearGraficoGenerico(
                        "Cobertura de necesidades vs. actividades",
                        "Muestra necesidades cubiertas frente a pendientes por asignar.",
                        "Apoya el seguimiento de pendientes y el cierre de asignaciones.",
                        construirCoberturaNecesidades(context));
            default:
                return crearGraficoGenerico(
                        "Grafico no reconocido",
                        "El identificador solicitado no existe en el catalogo.",
                        "No se puede aportar informacion adicional con este identificador.",
                        List.of());
        }
    }

    private ReporteGraficoPdfRequest crearGraficoGenerico(String titulo, String resumen, String aporte,
            List<ReporteDatoGraficoRequest> datos) {
        ReporteGraficoPdfRequest grafico = new ReporteGraficoPdfRequest();
        grafico.setTitulo(titulo);
        grafico.setResumen(resumen);
        grafico.setDescripcion(resumen);
        grafico.setAporte(aporte);
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

    private EstadisticasContext cargarContexto(ReporteEstadisticasPdfSeleccionRequest request) {
        List<Necesidad> necesidades = necesidadRepository.findAll(construirEspecificacionNecesidades(request));
        List<Asignacion> asignaciones = asignacionRepository.findAll(construirEspecificacionAsignaciones(request));
        Calendario calendario = null;
        if (request.getOidCalendario() != null) {
            calendario = calendarioRepository.findById(request.getOidCalendario()).orElse(null);
        }
        Set<Integer> necesidadesCubiertas = asignaciones.stream()
                .map(Asignacion::getNecesidad)
                .filter(Objects::nonNull)
                .map(Necesidad::getOidNecesidad)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return new EstadisticasContext(necesidades, asignaciones, calendario, necesidadesCubiertas);
    }

    private Specification<Necesidad> construirEspecificacionNecesidades(ReporteEstadisticasPdfSeleccionRequest request) {
        Specification<Necesidad> specification = Specification.where(null);
        if (request.getOidCalendario() != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.join("calendario").get("oidcalendario"), request.getOidCalendario()));
        }
        if (request.getOidDepartamento() != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.join("materia").join("departamento").get("oidDepartamento"),
                            request.getOidDepartamento()));
        }
        if (request.getOidPrograma() != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.join("materia").join("plan").join("programa").get("oidPrograma"),
                            request.getOidPrograma()));
        }
        return specification;
    }

    private Specification<Asignacion> construirEspecificacionAsignaciones(ReporteEstadisticasPdfSeleccionRequest request) {
        Specification<Asignacion> specification = Specification.where(null);
        if (request.getOidCalendario() != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.join("necesidad").join("calendario").get("oidcalendario"),
                            request.getOidCalendario()));
        }
        if (request.getOidDepartamento() != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.join("necesidad").join("materia").join("departamento").get("oidDepartamento"),
                            request.getOidDepartamento()));
        }
        if (request.getOidPrograma() != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.join("necesidad").join("materia").join("plan").join("programa").get("oidPrograma"),
                            request.getOidPrograma()));
        }
        return specification;
    }

    private List<ReporteDatoGraficoRequest> construirCargaActividades(EstadisticasContext context) {
        Map<Integer, Long> conteo = new HashMap<>();
        Map<Integer, Usuario> usuarios = new HashMap<>();
        for (Asignacion asignacion : context.asignaciones()) {
            Usuario usuario = asignacion.getSeleccionado() != null ? asignacion.getSeleccionado().getUsuario() : null;
            if (usuario == null || usuario.getOidUsuario() == null) {
                continue;
            }
            usuarios.putIfAbsent(usuario.getOidUsuario(), usuario);
            conteo.merge(usuario.getOidUsuario(), 1L, Long::sum);
        }

        if (conteo.isEmpty()) {
            return List.of();
        }

        return conteo.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .map(entry -> crearDato(
                        construirNombreUsuario(usuarios.get(entry.getKey()), entry.getKey()),
                        entry.getValue().doubleValue()))
                .collect(Collectors.toList());
    }

    private List<ReporteDatoGraficoRequest> construirOcupacionCupos(EstadisticasContext context) {
        Map<ContratacionEnum, Double> horasAsignadas = new EnumMap<>(ContratacionEnum.class);
        for (Asignacion asignacion : context.asignaciones()) {
            ContratacionEnum tipo = asignacion.getSeleccionado() != null ? asignacion.getSeleccionado().getTipo() : null;
            if (tipo == null) {
                continue;
            }
            double horas = calcularHorasAsignadas(asignacion);
            horasAsignadas.merge(tipo, horas, Double::sum);
        }

        if (horasAsignadas.isEmpty()) {
            return List.of();
        }

        List<ReporteDatoGraficoRequest> datos = new ArrayList<>();
        for (ContratacionEnum tipo : List.of(
                ContratacionEnum.PLANTA,
                ContratacionEnum.CATEDRA,
                ContratacionEnum.OCASIONAL,
                ContratacionEnum.BECARIOS_Y_PRACTICANTES,
                ContratacionEnum.BECARIOS_POSTGRADO)) {
            double horas = horasAsignadas.getOrDefault(tipo, 0d);
            Double cupo = obtenerCupoPorTipo(tipo, context.calendario());
            String etiqueta = nombreTipoContratacion(tipo);
            if (cupo != null && cupo > 0) {
                etiqueta = etiqueta + " (cupo " + formatearNumero(cupo) + ")";
            }
            if (horas > 0d || (cupo != null && cupo > 0)) {
                datos.add(crearDato(etiqueta, horas));
            }
        }
        return datos;
    }

    private List<ReporteDatoGraficoRequest> construirCoberturaDocente(EstadisticasContext context) {
        Map<Integer, Integer> totalPorSemestre = new HashMap<>();
        Map<Integer, Integer> cubiertasPorSemestre = new HashMap<>();
        Set<Integer> cubiertas = context.necesidadesCubiertas();

        for (Necesidad necesidad : context.necesidades()) {
            if (necesidad == null || necesidad.getMateria() == null || necesidad.getMateria().getSemestre() == null) {
                continue;
            }
            Integer semestre = necesidad.getMateria().getSemestre();
            totalPorSemestre.merge(semestre, 1, Integer::sum);
            Integer oidNecesidad = necesidad.getOidNecesidad();
            if (oidNecesidad != null && cubiertas.contains(oidNecesidad)) {
                cubiertasPorSemestre.merge(semestre, 1, Integer::sum);
            }
        }

        if (totalPorSemestre.isEmpty()) {
            return List.of();
        }

        return totalPorSemestre.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    int total = entry.getValue();
                    int cubiertasTotal = cubiertasPorSemestre.getOrDefault(entry.getKey(), 0);
                    double porcentaje = total > 0 ? (cubiertasTotal * 100d) / total : 0d;
                    return crearDato("Sem " + entry.getKey(), porcentaje);
                })
                .collect(Collectors.toList());
    }

    private List<ReporteDatoGraficoRequest> construirDemandaNecesidades(EstadisticasContext context) {
        Map<String, Integer> conteo = new HashMap<>();
        for (Necesidad necesidad : context.necesidades()) {
            Programa programa = necesidad.getMateria() != null
                    && necesidad.getMateria().getPlan() != null
                            ? necesidad.getMateria().getPlan().getPrograma()
                            : null;
            String nombrePrograma = obtenerNombrePrograma(programa);
            if (!StringUtils.hasText(nombrePrograma)) {
                nombrePrograma = "Programa";
            }
            conteo.merge(nombrePrograma, 1, Integer::sum);
        }

        if (conteo.isEmpty()) {
            return List.of();
        }

        return conteo.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .map(entry -> crearDato(entry.getKey(), entry.getValue().doubleValue()))
                .collect(Collectors.toList());
    }

    private List<ReporteDatoGraficoRequest> construirFlujoNecesidades(EstadisticasContext context) {
        Map<EstadoNecesidad, Integer> conteo = new EnumMap<>(EstadoNecesidad.class);
        for (Necesidad necesidad : context.necesidades()) {
            EstadoNecesidad estado = necesidad.getEstado();
            if (estado == null) {
                continue;
            }
            conteo.merge(estado, 1, Integer::sum);
        }

        if (conteo.isEmpty()) {
            return List.of();
        }

        List<ReporteDatoGraficoRequest> datos = new ArrayList<>();
        for (EstadoNecesidad estado : EstadoNecesidad.values()) {
            Integer total = conteo.get(estado);
            if (total == null || total == 0) {
                continue;
            }
            datos.add(crearDato(normalizarEtiquetaEstado(estado), total.doubleValue()));
        }
        return datos;
    }

    private List<ReporteDatoGraficoRequest> construirCoberturaNecesidades(EstadisticasContext context) {
        Set<Integer> cubiertas = new HashSet<>();
        for (Necesidad necesidad : context.necesidades()) {
            if (necesidad != null && necesidad.getOidNecesidad() != null
                    && context.necesidadesCubiertas().contains(necesidad.getOidNecesidad())) {
                cubiertas.add(necesidad.getOidNecesidad());
            }
        }
        int total = context.necesidades().size();
        int cubiertasTotal = cubiertas.size();
        int pendientes = Math.max(total - cubiertasTotal, 0);
        if (total == 0) {
            return List.of();
        }
        return List.of(
                crearDato("Cubiertas", (double) cubiertasTotal),
                crearDato("Pendientes", (double) pendientes));
    }

    private double calcularHorasAsignadas(Asignacion asignacion) {
        if (asignacion == null) {
            return 0d;
        }
        float horasDocencia = asignacion.getHorasDocencia() != null ? asignacion.getHorasDocencia() : 0f;
        float semanasDocencia = asignacion.getSemanasDocencia() != null ? asignacion.getSemanasDocencia() : 0f;
        float horasPreparacion = asignacion.getHorasPreparacion() != null ? asignacion.getHorasPreparacion() : 0f;
        float semanasPreparacion = asignacion.getSemanasPreparacion() != null ? asignacion.getSemanasPreparacion() : 0f;
        return (horasDocencia * semanasDocencia) + (horasPreparacion * semanasPreparacion);
    }

    private Double obtenerCupoPorTipo(ContratacionEnum tipo, Calendario calendario) {
        if (tipo == null || calendario == null) {
            return null;
        }
        return switch (tipo) {
            case PLANTA -> calendario.getHorasPlanta() != null ? calendario.getHorasPlanta().doubleValue() : null;
            case CATEDRA -> calendario.getHorasCatedra() != null ? calendario.getHorasCatedra().doubleValue() : null;
            case OCASIONAL -> calendario.getHorasOcasionales() != null ? calendario.getHorasOcasionales().doubleValue() : null;
            case BECARIOS_Y_PRACTICANTES,
                 BECARIOS_POSTGRADO -> calendario.getHorasBecarioPracticante() != null
                        ? calendario.getHorasBecarioPracticante().doubleValue()
                        : null;
        };
    }

    private String nombreTipoContratacion(ContratacionEnum tipo) {
        if (tipo == null) {
            return "Contratacion";
        }
        return switch (tipo) {
            case PLANTA -> "Planta";
            case CATEDRA -> "Catedra";
            case OCASIONAL -> "Ocasional";
            case BECARIOS_Y_PRACTICANTES -> "Becarios y practicantes";
            case BECARIOS_POSTGRADO -> "Becarios postgrado";
        };
    }

    private String construirNombreUsuario(Usuario usuario, Integer oidUsuario) {
        if (usuario == null) {
            return oidUsuario != null ? "Usuario " + oidUsuario : "Usuario";
        }
        String nombres = usuario.getNombres() != null ? usuario.getNombres().trim() : "";
        String apellidos = usuario.getApellidos() != null ? usuario.getApellidos().trim() : "";
        String correo = usuario.getCorreo() != null ? usuario.getCorreo().trim() : "";
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(nombres)) {
            builder.append(nombres);
        }
        if (StringUtils.hasText(apellidos)) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(apellidos);
        }
        if (builder.length() == 0 && StringUtils.hasText(correo)) {
            builder.append(correo);
        }
        if (builder.length() == 0) {
            return oidUsuario != null ? "Usuario " + oidUsuario : "Usuario";
        }
        return builder.toString();
    }

    private String obtenerNombrePrograma(Programa programa) {
        if (programa == null) {
            return "";
        }
        String nombreCorto = programa.getNombreCorto();
        if (StringUtils.hasText(nombreCorto)) {
            return nombreCorto;
        }
        return programa.getNombre() != null ? programa.getNombre() : "";
    }

    private String normalizarEtiquetaEstado(EstadoNecesidad estado) {
        if (estado == null) {
            return "";
        }
        String base = estado.name().replace('_', ' ').toLowerCase(Locale.ROOT);
        String[] partes = base.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String parte : partes) {
            if (parte.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(Character.toUpperCase(parte.charAt(0)))
                   .append(parte.substring(1));
        }
        return builder.toString();
    }

    private static record EstadisticasContext(
            List<Necesidad> necesidades,
            List<Asignacion> asignaciones,
            Calendario calendario,
            Set<Integer> necesidadesCubiertas) {
    }
}
