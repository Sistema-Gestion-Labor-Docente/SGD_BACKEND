package co.edu.unicauca.sgd.api.service.reportes.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioActividadCalendario;
import co.edu.unicauca.sgd.api.domain.UsuarioDetalle;
import co.edu.unicauca.sgd.api.dto.AtributoDTO;
import co.edu.unicauca.sgd.api.dto.reportes.RldPdfRequest;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioActividadCalendarioRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioRepository;
import co.edu.unicauca.sgd.api.service.EavAtributoService;
import co.edu.unicauca.sgd.api.service.reportes.RldPdfService;
import co.edu.unicauca.sgd.api.utils.StringUtils;

@Service
public class RldPdfServiceImpl implements RldPdfService {

    private static final Logger logger = LoggerFactory.getLogger(RldPdfServiceImpl.class);
    private static final String TEMPLATE_PATH = "formatos/Formato_RLD.html";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.forLanguageTag("es-CO"));

    private final UsuarioRepository usuarioRepository;
    private final CalendarioRepository calendarioRepository;
    private final UsuarioActividadCalendarioRepository usuarioActividadCalendarioRepository;
    private final EavAtributoService eavAtributoService;

    public RldPdfServiceImpl(UsuarioRepository usuarioRepository,
            CalendarioRepository calendarioRepository,
            UsuarioActividadCalendarioRepository usuarioActividadCalendarioRepository,
            EavAtributoService eavAtributoService) {
        this.usuarioRepository = usuarioRepository;
        this.calendarioRepository = calendarioRepository;
        this.usuarioActividadCalendarioRepository = usuarioActividadCalendarioRepository;
        this.eavAtributoService = eavAtributoService;
    }

    @Override
    public ByteArrayOutputStream generarRldPdf(RldPdfRequest request) throws IOException {
        if (request == null || request.getOidDocente() == null || request.getOidCalendario() == null) {
            throw new IllegalArgumentException("Debe enviar oidDocente y oidCalendario.");
        }
        Usuario usuario = usuarioRepository.findById(request.getOidDocente())
                .orElseThrow(() -> new IllegalArgumentException("Docente no encontrado."));
        Calendario calendario = calendarioRepository.findById(request.getOidCalendario())
                .orElseThrow(() -> new IllegalArgumentException("Calendario no encontrado."));

        List<UsuarioActividadCalendario> relaciones = usuarioActividadCalendarioRepository
                .findByUsuario_OidUsuarioAndActividadCalendario_Calendario_Oidcalendario(
                        request.getOidDocente(), request.getOidCalendario());

        String html = construirHtml(usuario, calendario, relaciones);
        try {
            return renderizarPdf(html);
        } catch (Exception e) {
            logger.error("Error renderizando PDF RLD: {}", e.getMessage(), e);
            throw new IOException("No se pudo renderizar el PDF RLD.", e);
        }
    }

    private String construirHtml(Usuario usuario, Calendario calendario,
            List<UsuarioActividadCalendario> relaciones) throws IOException {
        String plantilla = cargarPlantilla();
        UsuarioDetalle detalle = usuario.getUsuarioDetalle();
        String periodo = construirPeriodo(calendario);
        String fecha = DATE_FORMATTER.format(LocalDateTime.now());

        List<UsuarioActividadCalendario> seguras =
                relaciones != null ? relaciones : List.of();
        String secciones = construirSecciones(seguras, detalle);

        float totalHoras = calcularTotalHoras(seguras);
        String totalHorasTexto = formatearNumero(totalHoras);

        return plantilla
                .replace("{{RLD_PERIODO}}", escapeText(periodo))
                .replace("{{RLD_TOTAL_HORAS}}", escapeText(totalHorasTexto))
                .replace("{{RLD_HORAS_LABOR}}", escapeText(totalHorasTexto))
                .replace("{{RLD_FACULTAD}}", escapeText(valorOrDash(detalle != null ? detalle.getFacultad() : null)))
                .replace("{{RLD_DEPARTAMENTO}}", escapeText(valorOrDash(detalle != null ? detalle.getDepartamento() : null)))
                .replace("{{RLD_IDENTIFICACION}}", escapeText(valorOrDash(usuario.getIdentificacion())))
                .replace("{{RLD_CONTRATACION}}", escapeText(valorOrDash(detalle != null ? detalle.getContratacion() : null)))
                .replace("{{RLD_CATEGORIA}}", escapeText(valorOrDash(detalle != null ? detalle.getCategoria() : null)))
                .replace("{{RLD_OBSERVACION}}", escapeText("-"))
                .replace("{{RLD_NOMBRES}}",
                        escapeText((valorOrDash(usuario.getNombres()) + " " + valorOrDash(usuario.getApellidos())).trim()))
                .replace("{{RLD_DEDICACION}}", escapeText(valorOrDash(detalle != null ? detalle.getDedicacion() : null)))
                .replace("{{RLD_ESTUDIOS}}", escapeText(valorOrDash(detalle != null ? detalle.getEstudios() : null)))
                .replace("{{RLD_ESTADO}}",
                        escapeText(usuario.getEstadoUsuario() != null ? usuario.getEstadoUsuario().getNombre() : "-"))
                .replace("{{RLD_SECCIONES}}", secciones)
                .replace("{{RLD_FECHA}}", escapeText(fecha));
    }

    private String construirPeriodo(Calendario calendario) {
        String anio = calendario.getAnioCalendario() != null ? calendario.getAnioCalendario() : "-";
        String numero = calendario.getNumeroCalendario() != null ? calendario.getNumeroCalendario().toString() : "-";
        return anio + " - " + numero;
    }

    private float calcularTotalHoras(List<UsuarioActividadCalendario> relaciones) {
        float total = 0f;
        if (relaciones == null) {
            return total;
        }
        for (UsuarioActividadCalendario relacion : relaciones) {
            if (relacion == null) {
                continue;
            }
            float horas = relacion.getHorasActividad() != null ? relacion.getHorasActividad() : 0f;
            float semanas = relacion.getActividadCalendario() != null
                    && relacion.getActividadCalendario().getActividad() != null
                    && relacion.getActividadCalendario().getActividad().getSemanas() != null
                            ? relacion.getActividadCalendario().getActividad().getSemanas()
                            : 0f;
            total += horas * semanas;
        }
        return total;
    }

    private String construirSecciones(List<UsuarioActividadCalendario> relaciones, UsuarioDetalle detalle) {
        if (relaciones == null || relaciones.isEmpty()) {
            return construirSeccionVacia();
        }
        Map<RldTipoActividad, List<UsuarioActividadCalendario>> agrupadas =
                relaciones.stream().filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(this::resolverTipoActividad));

        StringBuilder builder = new StringBuilder();
        builder.append(construirDocencia(agrupadas.get(RldTipoActividad.DOCENCIA)));
        builder.append(construirTablaSimple("TRABAJOS DOCENCIA", "TRAB. DOCENCIA",
                agrupadas.get(RldTipoActividad.TRABAJOS_DOCENCIA), this::filaTrabajosDocencia));
        builder.append(construirTablaSimple("CAPACITACION", "CAPACITACION",
                agrupadas.get(RldTipoActividad.CAPACITACION), this::filaCapacitacion));
        builder.append(construirTablaSimple("ADMINISTRACION", "TOTAL ADMINISTRACION",
                agrupadas.get(RldTipoActividad.ADMINISTRACION),
                relacion -> filaAdministracion(relacion, detalle)));
        builder.append(construirTablaSimple("OTROS SERVICIOS", "OTROS SERVICIOS",
                agrupadas.get(RldTipoActividad.OTROS_SERVICIOS), this::filaOtrosServicios));
        builder.append(construirTablaSimple("EXTENSION", "EXTENSION",
                agrupadas.get(RldTipoActividad.EXTENSION), this::filaExtension));
        builder.append(construirTablaSimple("TRABAJOS DE INVESTIGACION", "TRAB. INVESTIGACION",
                agrupadas.get(RldTipoActividad.TRABAJOS_INVESTIGACION), this::filaTrabajosInvestigacion));
        builder.append(construirTablaSimple("ASESORIA", "ASESORIA",
                agrupadas.get(RldTipoActividad.ASESORIA), this::filaAsesoria));
        builder.append(construirTablaSimple("SERVICIOS", "SERVICIOS",
                agrupadas.get(RldTipoActividad.SERVICIOS), this::filaServicios));
        builder.append(construirTablaSimple("SEMILLEROS DE INVESTIGACION", "S. INVESTIGACION",
                agrupadas.get(RldTipoActividad.SEMILLEROS_INVESTIGACION), this::filaSemilleros));
        builder.append(construirTablaSimple("PROYECTOS DE INVESTIGACION", "P. INVESTIGACION",
                agrupadas.get(RldTipoActividad.PROYECTO_INVESTIGACION), this::filaProyectosInvestigacion));
        return builder.toString();
    }

    private String construirSeccionVacia() {
        return new StringBuilder()
                .append("<div class=\"section\">")
                .append("<div class=\"section-header\">SIN ACTIVIDADES</div>")
                .append("<div class=\"section-summary\">No hay registros para el periodo seleccionado</div>")
                .append("</div>")
                .toString();
    }

    private String construirDocencia(List<UsuarioActividadCalendario> relaciones) {
        if (relaciones == null || relaciones.isEmpty()) {
            return "";
        }
        float total = 0f;
        float docencia = 0f;
        float preparacion = 0f;
        StringBuilder filas = new StringBuilder();

        for (UsuarioActividadCalendario relacion : relaciones) {
            Actividad actividad = obtenerActividad(relacion);
            Map<String, String> attrs = obtenerAtributos(actividad);
            boolean esPreparacion = esPreparacion(actividad);
            float horas = relacion.getHorasActividad() != null ? relacion.getHorasActividad() : 0f;
            float semanas = actividad != null && actividad.getSemanas() != null ? actividad.getSemanas() : 0f;
            float totalHoras = horas * semanas;
            total += totalHoras;
            if (esPreparacion) {
                preparacion += totalHoras;
            } else {
                docencia += totalHoras;
            }

            String smnDoc = esPreparacion ? "-" : formatearNumero(semanas);
            String hrsDoc = esPreparacion ? "-" : formatearNumero(horas);
            String ttlDoc = esPreparacion ? "-" : formatearNumero(totalHoras);
            String smnPrep = esPreparacion ? formatearNumero(semanas) : "-";
            String hrsPrep = esPreparacion ? formatearNumero(horas) : "-";
            String ttlPrep = esPreparacion ? formatearNumero(totalHoras) : "-";

            filas.append("<tr>")
                    .append(td(getAttr(attrs, "CODIGO", "COD")))
                    .append(td(getAttr(attrs, "MATERIA")))
                    .append(td(getAttr(attrs, "PROGRAMA")))
                    .append(td(getAttr(attrs, "SEMESTRE", "SMT")))
                    .append(td(getAttr(attrs, "GRUPO", "GRP")))
                    .append(td(smnDoc))
                    .append(td(hrsDoc))
                    .append(td(ttlDoc))
                    .append(td(smnPrep))
                    .append(td(hrsPrep))
                    .append(td(ttlPrep))
                    .append(td(formatearNumero(totalHoras)))
                    .append("</tr>");
        }

        return new StringBuilder()
                .append("<div class=\"section\">")
                .append("<div class=\"section-header\">DOCENCIA</div>")
                .append("<div class=\"section-summary\">TOTAL ")
                .append(formatearNumero(total))
                .append(" | DOC. DIRECTA ")
                .append(formatearNumero(docencia))
                .append(" | PREPARACION ")
                .append(formatearNumero(preparacion))
                .append("</div>")
                .append("<table>")
                .append("<tr>")
                .append(th("COD"))
                .append(th("MATERIA"))
                .append(th("PROGRAMA"))
                .append(th("SMT"))
                .append(th("GRP"))
                .append(th("SMN"))
                .append(th("HRS"))
                .append(th("TTL"))
                .append(th("SMN"))
                .append(th("HRS"))
                .append(th("TTL"))
                .append(th("TOTAL"))
                .append("</tr>")
                .append(filas)
                .append("</table>")
                .append("</div>")
                .toString();
    }

    private String filaTrabajosDocencia(UsuarioActividadCalendario relacion) {
        return filaActividadBasica(relacion,
                List.of(
                        "ACTOADMIN", "ACTO ADMIN"),
                List.of("IDESTUDIANTE"),
                List.of("NOMBREESTUDIANTE"),
                false);
    }

    private String filaCapacitacion(UsuarioActividadCalendario relacion) {
        return filaActividadBasica(relacion,
                List.of("ACTOADMIN", "ACTO ADMIN"),
                List.of("ACTIVIDAD"),
                List.of("ANIOCOMISION", "ANO COMISION"),
                true);
    }

    private String filaAdministracion(UsuarioActividadCalendario relacion, UsuarioDetalle detalle) {
        Actividad actividad = obtenerActividad(relacion);
        Map<String, String> attrs = obtenerAtributos(actividad);
        String acto = getAttr(attrs, "ACTOADMIN", "ACTO ADMIN");
        String actividadNombre = getAttr(attrs, "ACTIVIDAD");
        String cargo = detalle != null ? valorOrDash(detalle.getCategoria()) : "-";
        String area = detalle != null ? valorOrDash(detalle.getDepartamento()) : "-";
        return filaActividadCustom(relacion, List.of(acto, actividadNombre, cargo, area));
    }

    private String filaOtrosServicios(UsuarioActividadCalendario relacion) {
        return filaActividadBasica(relacion,
                List.of("ACTOADMIN", "ACTO ADMIN"),
                List.of("ACTIVIDAD"),
                List.of(),
                false);
    }

    private String filaExtension(UsuarioActividadCalendario relacion) {
        Actividad actividad = obtenerActividad(relacion);
        Map<String, String> attrs = obtenerAtributos(actividad);
        String acto = getAttr(attrs, "ACTOADMIN", "ACTO ADMIN");
        String proyecto = getAttr(attrs, "NOMBREPROYECTO");
        String fInicial = getAttr(attrs, "F_INICIAL");
        String fFinal = getAttr(attrs, "F_FINAL");
        String observaciones = getAttr(attrs, "OBSERVACIONES");
        if ("-".equals(observaciones) && actividad != null) {
            observaciones = valorOrDash(actividad.getNombreActividad());
        }
        return filaActividadCustom(relacion, List.of(acto, proyecto, fInicial, fFinal),
                true, observaciones);
    }

    private String filaTrabajosInvestigacion(UsuarioActividadCalendario relacion) {
        Actividad actividad = obtenerActividad(relacion);
        Map<String, String> attrs = obtenerAtributos(actividad);
        String acto = getAttr(attrs, "ACTOADMIN", "ACTO ADMIN");
        String id = getAttr(attrs, "IDESTUDIANTE");
        String nombre = getAttr(attrs, "NOMBREESTUDIANTE");
        String observaciones = getAttr(attrs, "OBSERVACIONES");
        if ("-".equals(observaciones) && actividad != null) {
            observaciones = valorOrDash(actividad.getNombreActividad());
        }
        return filaActividadCustom(relacion, List.of(acto, id, nombre), true, observaciones);
    }

    private String filaAsesoria(UsuarioActividadCalendario relacion) {
        return filaActividadBasica(relacion,
                List.of("ACTOADMIN", "ACTO ADMIN"),
                List.of("ACTIVIDAD"),
                List.of(),
                false);
    }

    private String filaServicios(UsuarioActividadCalendario relacion) {
        return filaActividadBasica(relacion,
                List.of("ACTOADMIN", "ACTO ADMIN"),
                List.of("ACTIVIDAD"),
                List.of(),
                false);
    }

    private String filaSemilleros(UsuarioActividadCalendario relacion) {
        return filaActividadBasica(relacion,
                List.of("ACTOADMIN", "ACTO ADMIN"),
                List.of("IDSEMILLERO"),
                List.of("SEMILLERO"),
                true,
                getAttr(obtenerAtributos(obtenerActividad(relacion)), "ROLSEMILLERO", "ROL"));
    }

    private String filaProyectosInvestigacion(UsuarioActividadCalendario relacion) {
        Actividad actividad = obtenerActividad(relacion);
        Map<String, String> attrs = obtenerAtributos(actividad);
        String vri = getAttr(attrs, "VRI", "CODVRI", "CODIGOVRI");
        String proyecto = getAttr(attrs, "NOMBREPROYECTO");
        String fInicial = getAttr(attrs, "F_INICIAL");
        String fFinal = getAttr(attrs, "F_FINAL");
        String hAprob = getAttr(attrs, "H. APROB.", "HAPROB", "HAPROB.");
        String hLabor = getAttr(attrs, "H. LABOR", "HLABOR", "HLABOR.");
        float semanas = actividad != null && actividad.getSemanas() != null ? actividad.getSemanas() : 0f;
        float horas = relacion.getHorasActividad() != null ? relacion.getHorasActividad() : 0f;
        float total = horas * semanas;
        return new StringBuilder("<tr>")
                .append(td(vri))
                .append(td(proyecto))
                .append(td(fInicial))
                .append(td(fFinal))
                .append(td(hAprob))
                .append(td(hLabor))
                .append(td(formatearNumero(semanas)))
                .append(td(formatearNumero(total)))
                .append("</tr>")
                .toString();
    }

    private String filaActividadBasica(UsuarioActividadCalendario relacion,
            List<String> keysCol1, List<String> keysCol2, List<String> keysCol3, boolean incluyeAnno) {
        return filaActividadBasica(relacion, keysCol1, keysCol2, keysCol3, incluyeAnno, null);
    }

    private String filaActividadBasica(UsuarioActividadCalendario relacion,
            List<String> keysCol1, List<String> keysCol2, List<String> keysCol3, boolean incluyeAnno,
            String extra) {
        Actividad actividad = obtenerActividad(relacion);
        Map<String, String> attrs = obtenerAtributos(actividad);
        String col1 = getAttr(attrs, keysCol1);
        String col2 = keysCol2.isEmpty() ? "-" : getAttr(attrs, keysCol2);
        String col3 = keysCol3.isEmpty() ? "-" : getAttr(attrs, keysCol3);
        List<String> columnas = new ArrayList<>();
        columnas.add(col1);
        columnas.add(col2);
        if (incluyeAnno) {
            columnas.add(col3);
        } else {
            if (!keysCol3.isEmpty()) {
                columnas.add(col3);
            }
        }
        if (extra != null) {
            columnas.add(extra);
        }
        return filaActividadCustom(relacion, columnas);
    }

    private String filaActividadCustom(UsuarioActividadCalendario relacion, List<String> columnas) {
        return filaActividadCustom(relacion, columnas, false, null);
    }

    private String filaActividadCustom(UsuarioActividadCalendario relacion, List<String> columnas, boolean incluyeObs,
            String obs) {
        Actividad actividad = obtenerActividad(relacion);
        float horas = relacion.getHorasActividad() != null ? relacion.getHorasActividad() : 0f;
        float semanas = actividad != null && actividad.getSemanas() != null ? actividad.getSemanas() : 0f;
        float total = horas * semanas;
        StringBuilder row = new StringBuilder("<tr>");
        for (String col : columnas) {
            row.append(td(col));
        }
        row.append(td(formatearNumero(horas)))
                .append(td(formatearNumero(semanas)))
                .append(td(formatearNumero(total)));
        if (incluyeObs) {
            row.append(td(valorOrDash(obs)));
        }
        row.append("</tr>");
        return row.toString();
    }

    private RldTipoActividad resolverTipoActividad(UsuarioActividadCalendario relacion) {
        Actividad actividad = obtenerActividad(relacion);
        String nombre = actividad != null && actividad.getTipoActividad() != null
                ? actividad.getTipoActividad().getNombre()
                : "";
        String normalizado = normalizar(nombre);
        if (normalizado.contains("TRABAJOSDOCENCIA")) {
            return RldTipoActividad.TRABAJOS_DOCENCIA;
        }
        if (normalizado.contains("TRABAJOSDEINVESTIGACION")) {
            return RldTipoActividad.TRABAJOS_INVESTIGACION;
        }
        if (normalizado.contains("CAPACITACION")) {
            return RldTipoActividad.CAPACITACION;
        }
        if (normalizado.contains("ADMINISTRACION")) {
            return RldTipoActividad.ADMINISTRACION;
        }
        if (normalizado.contains("OTROSSERVICIOS")) {
            return RldTipoActividad.OTROS_SERVICIOS;
        }
        if (normalizado.contains("EXTENSION")) {
            return RldTipoActividad.EXTENSION;
        }
        if (normalizado.contains("ASESORIA")) {
            return RldTipoActividad.ASESORIA;
        }
        if (normalizado.equals("SERVICIOS") || normalizado.contains("SERVICIOS")) {
            return RldTipoActividad.SERVICIOS;
        }
        if (normalizado.contains("SEMILLEROSDEINVESTIGACION")) {
            return RldTipoActividad.SEMILLEROS_INVESTIGACION;
        }
        if (normalizado.contains("PROYECTOSINVESTIGACION") || normalizado.contains("PROYECTODEINVESTIGACION")) {
            return RldTipoActividad.PROYECTO_INVESTIGACION;
        }
        if (normalizado.contains("DOCENCIA") || normalizado.contains("PREPARACION")) {
            return RldTipoActividad.DOCENCIA;
        }
        return RldTipoActividad.DESCONOCIDO;
    }

    private boolean esPreparacion(Actividad actividad) {
        if (actividad == null || actividad.getTipoActividad() == null) {
            return false;
        }
        String nombre = actividad.getTipoActividad().getNombre();
        return normalizar(nombre).contains("PREPARACION");
    }

    private Actividad obtenerActividad(UsuarioActividadCalendario relacion) {
        if (relacion == null || relacion.getActividadCalendario() == null) {
            return null;
        }
        return relacion.getActividadCalendario().getActividad();
    }

    private Map<String, String> obtenerAtributos(Actividad actividad) {
        if (actividad == null) {
            return Map.of();
        }
        List<AtributoDTO> atributos = eavAtributoService.obtenerAtributosPorActividad(actividad);
        if (atributos == null || atributos.isEmpty()) {
            return Map.of();
        }
        Map<String, String> resultado = new HashMap<>();
        for (AtributoDTO atributo : atributos) {
            if (atributo == null || atributo.getCodigoAtributo() == null) {
                continue;
            }
            resultado.put(normalizar(atributo.getCodigoAtributo()), atributo.getValor());
        }
        return resultado;
    }

    private String getAttr(Map<String, String> attrs, String... keys) {
        if (attrs == null || keys == null) {
            return "-";
        }
        for (String key : keys) {
            if (!StringUtils.hasText(key)) {
                continue;
            }
            String value = attrs.get(normalizar(key));
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "-";
    }

    private String getAttr(Map<String, String> attrs, List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return "-";
        }
        return getAttr(attrs, keys.toArray(new String[0]));
    }

    private String construirHeaderTablaTrabajosDocencia() {
        return new StringBuilder("<tr>")
                .append(th("ACTO ADMIN"))
                .append(th("ID. ESTUDIANTE"))
                .append(th("NOMBRE ESTUDIANTE"))
                .append(th("HORAS"))
                .append(th("SEMANAS"))
                .append(th("TOTAL"))
                .append("</tr>")
                .toString();
    }

    private String construirHeaderTablaCapacitacion() {
        return new StringBuilder("<tr>")
                .append(th("ACTO ADMIN"))
                .append(th("ACTIVIDAD"))
                .append(th("ANO COMISION"))
                .append(th("HORAS"))
                .append(th("SEMANAS"))
                .append(th("TOTAL"))
                .append("</tr>")
                .toString();
    }

    private String construirHeaderTablaAdministracion() {
        return new StringBuilder("<tr>")
                .append(th("ACTO ADMIN"))
                .append(th("ACTIVIDAD"))
                .append(th("CARGO"))
                .append(th("AREA"))
                .append(th("HORAS"))
                .append(th("SEMANAS"))
                .append(th("TOTAL"))
                .append("</tr>")
                .toString();
    }

    private String construirHeaderTablaOtrosServicios() {
        return new StringBuilder("<tr>")
                .append(th("ACTO ADMIN"))
                .append(th("ACTIVIDAD"))
                .append(th("HORAS"))
                .append(th("SEMANAS"))
                .append(th("TOTAL"))
                .append("</tr>")
                .toString();
    }

    private String construirHeaderTablaExtension() {
        return new StringBuilder("<tr>")
                .append(th("ACTO ADMIN"))
                .append(th("NOMBRE PROYECTO"))
                .append(th("F. INICIAL"))
                .append(th("F. FINAL"))
                .append(th("HRS"))
                .append(th("SEM"))
                .append(th("TOTAL"))
                .append(th("OBSERVACIONES"))
                .append("</tr>")
                .toString();
    }

    private String construirHeaderTablaTrabajosInvestigacion() {
        return new StringBuilder("<tr>")
                .append(th("ACTO ADMIN"))
                .append(th("ID. ESTUDIANTE"))
                .append(th("NOMBRE ESTUDIANTE"))
                .append(th("HRS"))
                .append(th("SEM"))
                .append(th("TOTAL"))
                .append(th("OBSERVACIONES"))
                .append("</tr>")
                .toString();
    }

    private String construirHeaderTablaAsesoria() {
        return new StringBuilder("<tr>")
                .append(th("ACTO ADMIN"))
                .append(th("ACTIVIDAD"))
                .append(th("HORAS"))
                .append(th("SEMANAS"))
                .append(th("TOTAL"))
                .append("</tr>")
                .toString();
    }

    private String construirHeaderTablaServicios() {
        return construirHeaderTablaAsesoria();
    }

    private String construirHeaderTablaSemilleros() {
        return new StringBuilder("<tr>")
                .append(th("ACTO ADMIN"))
                .append(th("ID"))
                .append(th("SEMILLERO"))
                .append(th("ROL"))
                .append(th("HORAS"))
                .append(th("SEMANAS"))
                .append(th("TOTAL"))
                .append("</tr>")
                .toString();
    }

    private String construirHeaderTablaProyectosInvestigacion() {
        return new StringBuilder("<tr>")
                .append(th("COD. VRI"))
                .append(th("NOMBRE PROYECTO"))
                .append(th("F. INICIAL"))
                .append(th("F. FINAL"))
                .append(th("H. APROB."))
                .append(th("H. LABOR"))
                .append(th("SEMANAS"))
                .append(th("TOTAL"))
                .append("</tr>")
                .toString();
    }

    private String th(String value) {
        return "<th>" + escapeText(value) + "</th>";
    }

    private String td(String value) {
        return "<td>" + escapeText(valorOrDash(value)) + "</td>";
    }

    private String formatearNumero(Float valor) {
        if (valor == null) {
            return "-";
        }
        return formatearNumero(valor.doubleValue());
    }

    private String formatearNumero(Double valor) {
        if (valor == null) {
            return "-";
        }
        if (Math.abs(valor - Math.round(valor)) < 0.001d) {
            return String.valueOf(Math.round(valor));
        }
        return String.format(Locale.US, "%.1f", valor);
    }

    private String valorOrDash(String value) {
        return StringUtils.hasText(value) ? value : "-";
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

    private String normalizar(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
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

    private String construirHeaderPorTipo(RldTipoActividad tipo) {
        switch (tipo) {
            case TRABAJOS_DOCENCIA:
                return construirHeaderTablaTrabajosDocencia();
            case CAPACITACION:
                return construirHeaderTablaCapacitacion();
            case ADMINISTRACION:
                return construirHeaderTablaAdministracion();
            case OTROS_SERVICIOS:
                return construirHeaderTablaOtrosServicios();
            case EXTENSION:
                return construirHeaderTablaExtension();
            case TRABAJOS_INVESTIGACION:
                return construirHeaderTablaTrabajosInvestigacion();
            case ASESORIA:
                return construirHeaderTablaAsesoria();
            case SERVICIOS:
                return construirHeaderTablaServicios();
            case SEMILLEROS_INVESTIGACION:
                return construirHeaderTablaSemilleros();
            case PROYECTO_INVESTIGACION:
                return construirHeaderTablaProyectosInvestigacion();
            default:
                return "";
        }
    }

    private enum RldTipoActividad {
        DOCENCIA,
        TRABAJOS_DOCENCIA,
        CAPACITACION,
        ADMINISTRACION,
        OTROS_SERVICIOS,
        EXTENSION,
        TRABAJOS_INVESTIGACION,
        ASESORIA,
        SERVICIOS,
        SEMILLEROS_INVESTIGACION,
        PROYECTO_INVESTIGACION,
        DESCONOCIDO
    }

    private String construirTablaSimple(String titulo, String resumenLabel,
            List<UsuarioActividadCalendario> relaciones, java.util.function.Function<UsuarioActividadCalendario, String> builder) {
        if (relaciones == null || relaciones.isEmpty()) {
            return "";
        }
        StringBuilder filas = new StringBuilder();
        float total = 0f;
        for (UsuarioActividadCalendario relacion : relaciones) {
            Actividad actividad = obtenerActividad(relacion);
            float horas = relacion.getHorasActividad() != null ? relacion.getHorasActividad() : 0f;
            float semanas = actividad != null && actividad.getSemanas() != null ? actividad.getSemanas() : 0f;
            total += horas * semanas;
            filas.append(builder.apply(relacion));
        }
        RldTipoActividad tipo = resolverTipoActividad(relaciones.get(0));
        String header = construirHeaderPorTipo(tipo);
        return new StringBuilder()
                .append("<div class=\"section\">")
                .append("<div class=\"section-header\">").append(escapeText(titulo)).append("</div>")
                .append("<div class=\"section-summary\">")
                .append(escapeText(resumenLabel))
                .append(": ")
                .append(formatearNumero(total))
                .append("</div>")
                .append("<table>")
                .append(header)
                .append(filas)
                .append("</table>")
                .append("</div>")
                .toString();
    }
}
