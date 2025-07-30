package co.edu.unicauca.sgd.api.service.actividad;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para consultas avanzadas sobre actividades.
 */
@Service
public class ActividadQueryServiceImpl implements ActividadQueryService {

    private static final Logger logger = LoggerFactory.getLogger(ActividadQueryServiceImpl.class);
    public static final boolean DEFAULT_ASCENDING_ORDER = true;

    @PersistenceContext
    private EntityManager entityManager;

    public ActividadQueryServiceImpl() {
    }

    public List<ActividadBaseDTO> ordenarActividadesPorTipo(List<ActividadBaseDTO> actividades, Boolean ordenAscendente) {
        // Crear un Comparator explícito para ActividadBaseDTO
        Comparator<ActividadBaseDTO> comparador = Comparator.comparing(actividad -> actividad.getTipoActividad().getNombre());

        // Invertir el orden si no es ascendente
        if (ordenAscendente != null && !ordenAscendente) {
            comparador = comparador.reversed();
        }

        // Ordenar y retornar la lista de actividades
        return actividades.stream().sorted(comparador).collect(Collectors.toList());
    }
}
