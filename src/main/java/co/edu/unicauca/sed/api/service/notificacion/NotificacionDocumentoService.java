package co.edu.unicauca.sed.api.service.notificacion;

import co.edu.unicauca.sed.api.client.ClienteNotificacion;
import co.edu.unicauca.sed.api.domain.Fuente;
import co.edu.unicauca.sed.api.domain.Usuario;
import co.edu.unicauca.sed.api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Optional;

/**
 * Servicio para enviar notificaciones relacionadas con documentos (consolidado,
 * fuente, resolución, etc.)
 */
@Service
public class NotificacionDocumentoService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionDocumentoService.class);

    @Autowired
    private ClienteNotificacion notificationClient;

    @Autowired
    private NotificacionTemplateService notificacionTemplateService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Notifica al jefe de departamento cuando se genera o aprueba un documento.
     *
     * @param tipoDocumento Tipo de documento
     * @param evaluador     Usuario que genera el documento.
     * @param evaluado      Usuario al que pertenece el documento.
     */
    @Async
    public void notificarJefeDepartamento(String tipoDocumento, Usuario evaluador, Usuario evaluado) {
        try {
            String departamento = evaluado.getUsuarioDetalle().getDepartamento();

            Optional<Usuario> jefeDepartamento = usuarioRepository.findFirstActiveByDepartamentoAndRolId(departamento, 7);

            if (jefeDepartamento.isPresent() && jefeDepartamento.get().getCorreo() != null) {
                String asunto = notificacionTemplateService.construirAsuntoNotificacion(tipoDocumento, departamento);
                String mensaje = notificacionTemplateService.construirMensajeNotificacion(tipoDocumento, evaluador,
                        evaluado, departamento);

                notificationClient.enviarNotificacion(Collections.singletonList(jefeDepartamento.get().getCorreo()), asunto, mensaje);

                logger.info("✅ Notificación enviada al CPD {} ({}) sobre el documento: {}",
                        departamento, jefeDepartamento.get().getCorreo(), tipoDocumento);
            } else {
                logger.warn("⚠️ No se encontró CPD para el departamento: {}", departamento);
            }
        } catch (Exception e) {
            logger.error("❌ Error notificando al CPD: {}", e.getMessage(), e);
        }
    }

    @Async
    private void notificarEvaluado(String tipoDocumento, Usuario evaluador, Usuario evaluado) {
        try {
            String departamento = evaluado.getUsuarioDetalle().getDepartamento();
            String asunto = notificacionTemplateService.construirAsuntoNotificacion(tipoDocumento, departamento);
            String mensaje = notificacionTemplateService.construirMensajeNotificacion(tipoDocumento, evaluador, evaluado, departamento);
            notificationClient.enviarNotificacion(Collections.singletonList(evaluado.getCorreo()), asunto, mensaje);
        } catch (Exception e) {
            logger.error("❌ Error notificando al jefe de departamento: {}", e.getMessage(), e);
        }
    }

    public void notificarEvaluadoPorFuente(String tipoDocumento, Fuente fuente) {
        try {
            Usuario evaluado = fuente.getActividad().getProceso().getEvaluado();
            Usuario evaluador = fuente.getActividad().getProceso().getEvaluador();

            notificarEvaluado(tipoDocumento, evaluador, evaluado);
        } catch (Exception e) {
            logger.error("❌ Error notificando al evaluado por fuente: {}", e.getMessage(), e);
        }
    }
}
