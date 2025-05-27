package co.edu.unicauca.sed.api.service.evaluacion_docente;

import co.edu.unicauca.sed.api.dto.OportunidadMejoraDTO;
import co.edu.unicauca.sed.api.domain.Autoevaluacion;
import co.edu.unicauca.sed.api.domain.OportunidadMejora;
import co.edu.unicauca.sed.api.repository.OportunidadMejoraRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OportunidadMejoraServiceImpl implements OportunidadMejoraService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OportunidadMejoraServiceImpl.class);
    private final OportunidadMejoraRepository oportunidadMejoraRepository;

    @Override
    public void guardar(List<OportunidadMejoraDTO> mejoras, Autoevaluacion autoevaluacion) {
        if (mejoras == null) {
            LOGGER.info("📌 Lista de oportunidades de mejora nula. No se realizará ninguna operación.");
            return;
        }

        eliminarMejorasRemovidas(mejoras, autoevaluacion);

        mejoras.forEach(mejora -> {
            if (mejora.getDescripcion() == null || mejora.getDescripcion().isBlank()) {
                return;
            }

            OportunidadMejora entidad;

            if (mejora.getOidOportunidadMejora() != null) {
                entidad = oportunidadMejoraRepository.findById(mejora.getOidOportunidadMejora()).orElseGet(OportunidadMejora::new);
            } else {
                entidad = new OportunidadMejora();
            }

            entidad.setAutoevaluacion(autoevaluacion);
            entidad.setDescripcion(mejora.getDescripcion());

            oportunidadMejoraRepository.save(entidad);
        });
    }

    private void eliminarMejorasRemovidas(List<OportunidadMejoraDTO> mejoras, Autoevaluacion autoevaluacion) {
        List<OportunidadMejora> actuales = oportunidadMejoraRepository.findByAutoevaluacion(autoevaluacion);

        Set<Integer> nuevosIds = mejoras.stream()
            .map(OportunidadMejoraDTO::getOidOportunidadMejora).filter(Objects::nonNull).collect(Collectors.toSet());

        for (OportunidadMejora existente : actuales) {
            Integer id = existente.getOidOportunidadMejora();
            if (id != null && !nuevosIds.contains(id)) {
                oportunidadMejoraRepository.deleteById(id);
                LOGGER.info("🗑️ Oportunidad de mejora con ID {} eliminada por no estar en la nueva lista", id);
            }
        }
    }

}