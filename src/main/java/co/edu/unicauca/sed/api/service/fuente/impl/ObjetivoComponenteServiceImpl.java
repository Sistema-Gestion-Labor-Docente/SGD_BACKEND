package co.edu.unicauca.sed.api.service.fuente.impl;

import co.edu.unicauca.sed.api.domain.Componente;
import co.edu.unicauca.sed.api.domain.ObjetivoComponente;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.ComponenteConObjetivosDTO;
import co.edu.unicauca.sed.api.dto.ObjetivoDTO;
import co.edu.unicauca.sed.api.repository.ObjetivoComponenteRepository;
import co.edu.unicauca.sed.api.service.fuente.ObjetivoComponenteService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ObjetivoComponenteServiceImpl implements ObjetivoComponenteService {

    @Autowired
    private ObjetivoComponenteRepository objetivoComponenteRepository;

    @Override
    public ObjetivoComponente guardar(ObjetivoComponente objetivoComponente) {
        return objetivoComponenteRepository.save(objetivoComponente);
    }

    @Override
    public ObjetivoComponente buscarPorId(Integer id) {
        return objetivoComponenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ObjetivoComponente no encontrado con ID: " + id));
    }

    @Override
    public ApiResponse<Page<ComponenteConObjetivosDTO>> listar(Pageable pageable) {
        List<ObjetivoComponente> objetivos = objetivoComponenteRepository.findAll();

        Map<Componente, List<ObjetivoComponente>> agrupado = objetivos.stream()
                .collect(Collectors.groupingBy(ObjetivoComponente::getComponente));

        List<ComponenteConObjetivosDTO> resultado = agrupado.entrySet().stream()
                .map(entry -> {
                    Componente componente = entry.getKey();
                    List<ObjetivoDTO> objetivosDTO = entry.getValue().stream()
                            .map(obj -> new ObjetivoDTO(obj.getOidObjetivoComponente(), obj.getDescripcion()))
                            .toList();

                    return new ComponenteConObjetivosDTO(
                            componente.getOidComponente(),
                            componente.getNombre(),
                            componente.getPorcentaje(),
                            objetivosDTO);
                })
                .sorted(Comparator.comparing(ComponenteConObjetivosDTO::getOidComponente))
                .toList();

        // Manualmente paginar
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), resultado.size());
        Page<ComponenteConObjetivosDTO> page = new PageImpl<>(resultado.subList(start, end), pageable, resultado.size());

        return new ApiResponse<>(200, "Listado de componentes y sus objetivos.", page);
    }
}
