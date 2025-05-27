package co.edu.unicauca.sed.api.service.notificacion;

import org.springframework.stereotype.Service;

import co.edu.unicauca.sed.api.domain.Usuario;

import java.util.Locale;

/**
 * Servicio que construye plantillas de notificación para el envío de correos electrónicos
 * relacionados con la gestión de documentos (consolidado, fuente, resolución, etc.).
 */
@Service
public class NotificacionTemplateService {

    /**
     * Construye el mensaje de correo para eventos relacionados con documentos.
     *
     * @param tipoDocumento Tipo de documento (consolidado, fuente, resolución, etc.).
     * @param evaluador     Usuario que genera el documento.
     * @param evaluado      Usuario al que pertenece el documento.
     * @param departamento  Departamento asociado al evaluado.
     * @return Mensaje formateado para el correo.
     */
    public String construirMensajeNotificacion(String tipoDocumento, Usuario evaluador, Usuario evaluado, String departamento) {
        String accion = determinarAccion(tipoDocumento);

        return String.format(
            "Se ha %s un(a) %s correspondiente al docente %s %s (Identificación: %s).\n\n" +
            "El documento fue generado por el evaluador %s %s.\n\n" +
            "Departamento: %s\n\n" +
            "Por favor, revise la documentación correspondiente.\n\n" +
            "Atentamente,\n" +
            "Sistema de Gestión de Evaluación Docente",
            accion,
            tipoDocumento.toLowerCase(Locale.ROOT),
            evaluado.getNombres(), evaluado.getApellidos(), evaluado.getIdentificacion(),
            evaluador.getNombres(), evaluador.getApellidos(),
            departamento
        );
    }

    /**
     * Construye el asunto del correo para eventos relacionados con documentos.
     *
     * @param tipoDocumento Tipo de documento (consolidado, fuente, resolución, etc.).
     * @param departamento  Departamento asociado al evaluado.
     * @return Asunto formateado para el correo.
     */
    public String construirAsuntoNotificacion(String tipoDocumento, String departamento) {
        String accion = determinarAccion(tipoDocumento);
        return String.format("Se ha %s un(a) %s para el programa de %s", accion, tipoDocumento.toLowerCase(Locale.ROOT), departamento);
    }

    /**
     * Determina el verbo adecuado dependiendo del tipo de documento.
     *
     * @param tipoDocumento Tipo de documento (consolidado, fuente, resolución, etc.).
     * @return Verbo adecuado para el mensaje (aprobado, cargado, generado).
     */
    private String determinarAccion(String tipoDocumento) {
        switch (tipoDocumento.toLowerCase(Locale.ROOT)) {
            case "consolidado":
                return "aprobado";
            case "Fuente 2":
                return "cargado";
            case "resolucion":
                return "generado";
            default:
                return "procesado";
        }
    }
}
