package co.edu.unicauca.sed.api.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import co.edu.unicauca.sed.api.dto.EmailRequest;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Cliente que se encarga de comunicarse con el microservicio de notificaciones,
 * permitiendo enviar correos electrónicos con contenido en UTF-8.
 */
@Service
public class ClienteNotificacion {

    private static final Logger logger = LoggerFactory.getLogger(ClienteNotificacion.class);

    /**
     * Cliente HTTP utilizado para la comunicación con el microservicio.
     */
    private final RestTemplate restTemplate;

    /**
     * URL base del microservicio de notificaciones, obtenida desde el application.yml.
     */
    @Value("${notification.service.url}")
    private String urlServicioNotificaciones;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param restTemplate Cliente HTTP proporcionado por Spring.
     */
    public ClienteNotificacion(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Envía una notificación al microservicio de notificaciones.
     *
     * @param correos Lista de correos electrónicos de los destinatarios.
     * @param asunto  Asunto del correo.
     * @param mensaje Cuerpo del correo, con caracteres especiales y tildes.
     */
    public void enviarNotificacion(List<String> correos, String asunto, String mensaje) {
        try {
            EmailRequest emailRequest = new EmailRequest();
            emailRequest.setCorreos(correos);
            emailRequest.setAsunto(asunto);
            emailRequest.setMensaje(mensaje);
            emailRequest.setDocumentos(Collections.emptyMap());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));

            HttpEntity<EmailRequest> requestEntity = new HttpEntity<>(emailRequest, headers);

            restTemplate.postForEntity(urlServicioNotificaciones, requestEntity, Void.class);

            logger.info("✅ Notificación enviada correctamente a: {}", correos);
        } catch (Exception e) {
            logger.error("❌ Error al enviar la notificación al microservicio de notificaciones: {}", e.getMessage(), e);
        }
    }
}
