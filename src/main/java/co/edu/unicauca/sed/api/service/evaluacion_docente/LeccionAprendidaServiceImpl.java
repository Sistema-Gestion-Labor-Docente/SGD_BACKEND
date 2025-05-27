package co.edu.unicauca.sed.api.service.evaluacion_docente;

import co.edu.unicauca.sed.api.dto.LeccionDTO;
import co.edu.unicauca.sed.api.domain.Autoevaluacion;
import co.edu.unicauca.sed.api.domain.LeccionAprendida;
import co.edu.unicauca.sed.api.repository.LeccionAprendidaRepository;
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
public class LeccionAprendidaServiceImpl implements LeccionAprendidaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeccionAprendidaServiceImpl.class);
    private final LeccionAprendidaRepository leccionAprendidaRepository;

    @Override
    public void guardar(List<LeccionDTO> lecciones, Autoevaluacion autoevaluacion) {
        if (lecciones == null) {
            LOGGER.info("📌 Lista de lecciones nula. No se realizará ninguna operación.");
            return;
        }

        eliminarLeccionesRemovidas(lecciones, autoevaluacion);

        lecciones.forEach(leccion -> {
            if (leccion.getDescripcion() == null || leccion.getDescripcion().isBlank()) {
                return;
            }

            LeccionAprendida entidad;

            if (leccion.getOidLeccionAprendida() != null) {
                entidad = leccionAprendidaRepository.findById(leccion.getOidLeccionAprendida())
                    .orElseGet(() -> {
                        LOGGER.warn("❗ Lección con ID {} no encontrada. Se creará nueva.", leccion.getOidLeccionAprendida());
                        return new LeccionAprendida();
                    });
            } else {
                entidad = new LeccionAprendida();
            }

            entidad.setAutoevaluacion(autoevaluacion);
            entidad.setDescripcion(leccion.getDescripcion());

            leccionAprendidaRepository.save(entidad);
        });
    }

    @Override
    public List<LeccionDTO> obtenerDescripcionesLecciones(Autoevaluacion autoevaluacion) {
        return leccionAprendidaRepository.findByAutoevaluacion(autoevaluacion)
            .stream().map(leccion -> new LeccionDTO(leccion.getOidLeccionAprendida(), leccion.getDescripcion())).collect(Collectors.toList());
    }

    private void eliminarLeccionesRemovidas(List<LeccionDTO> lecciones, Autoevaluacion autoevaluacion) {
        List<LeccionAprendida> actuales = leccionAprendidaRepository.findByAutoevaluacion(autoevaluacion);

        Set<Integer> nuevosIds = lecciones.stream().map(LeccionDTO::getOidLeccionAprendida).filter(Objects::nonNull).collect(Collectors.toSet());

        for (LeccionAprendida existente : actuales) {
            Integer id = existente.getOidLeccionAprendida();
            if (id != null && !nuevosIds.contains(id)) {
                leccionAprendidaRepository.deleteById(id);
                LOGGER.info("🗑️ Lección con ID {} eliminada por no estar en la nueva lista", id);
            }
        }
    }
}
