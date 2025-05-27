package co.edu.unicauca.sed.api.client;

import co.edu.unicauca.sed.api.dto.PeriodoExternoDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Cliente que consulta los periodos académicos desde el sistema externo.
 */
@Service
@RequiredArgsConstructor
public class ClientePeriodoAcademico {

    private static final Logger logger = LoggerFactory.getLogger(ClientePeriodoAcademico.class);

    private final RestTemplate restTemplate;

    @Value("${periodo-academico.api.url}")
    private String urlBase;

    private final String RUTA_CONSULTA = "labordocente/periodos";

    /**
     * Obtiene los periodos académicos externos.
     *
     * @return Lista de periodos externos no vacía o vacía si hay error.
     */
    public List<PeriodoExternoDTO> obtenerPeriodosExternos() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<PeriodoExternoDTO[]> response = restTemplate.exchange(
                    urlBase + RUTA_CONSULTA,
                    HttpMethod.GET,
                    request,
                    PeriodoExternoDTO[].class
            );

            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            logger.error("❌ Error al consultar periodos académicos externos: {}", e.getMessage(), e);
            return List.of();
        }
    }
}
