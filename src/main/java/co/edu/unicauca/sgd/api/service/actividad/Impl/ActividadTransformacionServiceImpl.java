package co.edu.unicauca.sgd.api.service.actividad.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadPaginadaDTO;
import co.edu.unicauca.sgd.api.service.actividad.ActividadCalculoService;
import co.edu.unicauca.sgd.api.service.actividad.ActividadTransformacionService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para la transformación de actividades y sus cálculos.
 */
@Service
public class ActividadTransformacionServiceImpl implements ActividadTransformacionService {

    @Autowired
    private ActividadCalculoService calculoService;

    @Override
    public Map<String, Object> transformarActividad(Actividad actividad, float horasTotales) {
        double porcentaje = calculoService.calcularPorcentaje(actividad.getHoras(), horasTotales);


        return Map.of(
                "oidActividad", actividad.getOidActividad(),
                "nombre", actividad.getNombreActividad(),
                "horas", actividad.getHoras(),
                "porcentaje", porcentaje);
    }

    @Override
    public ActividadPaginadaDTO construirActividadPaginadaDTO(Page<Actividad> actividadPage) {
        List<Actividad> actividades = actividadPage.getContent();
        float totalHoras = calculoService.calcularTotalHoras(actividades);

        Map<String, List<Map<String, Object>>> actividadesPorTipo = agruparActividadesPorTipo(actividades, totalHoras);

        ActividadPaginadaDTO actividadPaginadaDTO = new ActividadPaginadaDTO();
        actividadPaginadaDTO.setActividades(actividadesPorTipo);
        actividadPaginadaDTO.setCurrentPage(actividadPage.getNumber());
        actividadPaginadaDTO.setPageSize(actividadPage.getSize());
        actividadPaginadaDTO.setTotalItems((int) actividadPage.getTotalElements());
        actividadPaginadaDTO.setTotalPages(actividadPage.getTotalPages());

        return actividadPaginadaDTO;
    }

    @Override
    public Map<String, List<Map<String, Object>>> agruparActividadesPorTipo(List<Actividad> actividades,
            float totalHoras) {
        return actividades.stream()
                .sorted(Comparator.comparing(a -> a.getTipoActividad().getNombre()))
                .collect(Collectors.groupingBy(
                        actividad -> String.valueOf(actividad.getTipoActividad().getNombre()),
                        Collectors.mapping(
                                actividad -> (Map<String, Object>) transformarActividad(actividad, totalHoras),
                                Collectors.toList())));
    }
}
