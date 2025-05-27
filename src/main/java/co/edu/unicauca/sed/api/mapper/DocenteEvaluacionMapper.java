package co.edu.unicauca.sed.api.mapper;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.Usuario;
import co.edu.unicauca.sed.api.dto.DocenteEvaluacionDTO;
import co.edu.unicauca.sed.api.service.actividad.ActividadCalculoService;
import co.edu.unicauca.sed.api.utils.MathUtils;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DocenteEvaluacionMapper {

    private final ActividadCalculoService calculoService;

    public DocenteEvaluacionMapper(ActividadCalculoService calculoService) {
        this.calculoService = calculoService;
    }

    /**
     * Transforma un objeto Usuario y sus actividades asociadas en un DocenteEvaluacionDTO.
     *
     * @param usuario     Usuario a transformar.
     * @param actividades Lista de actividades asociadas al usuario.
     * @return DocenteEvaluacionDTO.
     */
    public DocenteEvaluacionDTO toDto(Usuario usuario, List<Actividad> actividades) {
        float totalHoras = calculoService.calcularTotalHoras(actividades);

        double totalAcumulado = actividades.stream()
            .mapToDouble(actividad -> {
                double porcentaje = calculoService.calcularPorcentaje(actividad.getHoras(), totalHoras);
                double promedio = calculoService.calcularPromedio(actividad.getFuentes());
                return calculoService.calcularAcumulado(promedio, porcentaje);
            }).sum();

        int totalFuentes = actividades.stream().mapToInt(a -> a.getFuentes().size()).sum();
        int fuentesCompletadas = actividades.stream()
            .flatMap(a -> a.getFuentes().stream())
            .filter(f -> "Diligenciado".equalsIgnoreCase(f.getEstadoFuente().getNombreEstado()))
            .mapToInt(f -> 1)
            .sum();

        float porcentajeCompletado = MathUtils.calcularPorcentajeCompletado(totalFuentes, fuentesCompletadas);
        String estadoConsolidado = porcentajeCompletado == 100 ? "Completo" : "En progreso";

        return new DocenteEvaluacionDTO(
            usuario.getOidUsuario(),
            usuario.getNombres() + " " + usuario.getApellidos(),
            usuario.getIdentificacion(),
            usuario.getUsuarioDetalle().getContratacion(),
            porcentajeCompletado,
            estadoConsolidado,
            totalAcumulado
        );
    }
}
