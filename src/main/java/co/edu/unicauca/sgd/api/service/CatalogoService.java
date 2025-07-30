package co.edu.unicauca.sgd.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.CatalogoDTO;
import co.edu.unicauca.sgd.api.enums.CategoriaEnum;
import co.edu.unicauca.sgd.api.enums.ContratacionEnum;
import co.edu.unicauca.sgd.api.enums.DedicacionEnum;
import co.edu.unicauca.sgd.api.enums.DepartamentoEnum;
import co.edu.unicauca.sgd.api.enums.EstudiosEnum;
import co.edu.unicauca.sgd.api.enums.FacultadEnum;
import co.edu.unicauca.sgd.api.enums.ProgramaEnum;
import co.edu.unicauca.sgd.api.repository.RolRepository;
import co.edu.unicauca.sgd.api.repository.TipoActividadRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioDetalleRepository;
import co.edu.unicauca.sgd.api.utils.EnumUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CatalogoService {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private TipoActividadRepository tipoActividadRepository;

    @Autowired
    private UsuarioDetalleRepository usuarioDetalleRepository;

    /**
     * Método principal para obtener el catálogo completo.
     */
    public ApiResponse<CatalogoDTO> obtenerCatalogo() {
        try {
            CatalogoDTO catalogoDTO = new CatalogoDTO();

            // Obtener las opciones para las secciones del catálogo
            catalogoDTO.setFacultades(obtenerFacultades());
            catalogoDTO.setDepartamentos(obtenerDepartamentos());
            catalogoDTO.setCategorias(obtenerCategorias());
            catalogoDTO.setContrataciones(obtenerContrataciones());
            catalogoDTO.setDedicaciones(obtenerDedicaciones());
            catalogoDTO.setEstudios(obtenerEstudios());
            catalogoDTO.setProgramas(obtenerProgramas());

            // Obtener Roles
            catalogoDTO.setRoles(obtenerRoles());

            // Obtener Tipo Actividades
            catalogoDTO.setTipoActividades(obtenerTipoActividades());


            return new ApiResponse<>(200, "Catálogo obtenido correctamente.", catalogoDTO);

        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al obtener el catálogo: " + e.getMessage(), null);
        }
    }

    private List<Map<String, String>> obtenerFacultades() {
        return obtenerValor(usuarioDetalleRepository.findDistinctFacultad(), FacultadEnum.class);
    }
    
    private List<Map<String, String>> obtenerDepartamentos() {
        return obtenerValor(usuarioDetalleRepository.findDistinctDepartamento(), DepartamentoEnum.class);
    }
    
    private List<Map<String, String>> obtenerCategorias() {
        return obtenerValor(usuarioDetalleRepository.findDistinctCategoria(), CategoriaEnum.class);
    }
    
    private List<Map<String, String>> obtenerContrataciones() {
        return obtenerValor(usuarioDetalleRepository.findDistinctContratacion(), ContratacionEnum.class);
    }
    
    private List<Map<String, String>> obtenerDedicaciones() {
        return obtenerValor(usuarioDetalleRepository.findDistinctDedicacion(), DedicacionEnum.class);
    }
    
    private List<Map<String, String>> obtenerEstudios() {
        return obtenerValor(usuarioDetalleRepository.findDistinctEstudios(), EstudiosEnum.class);
    }

    private List<Map<String, String>> obtenerProgramas() {
        return obtenerValor(usuarioDetalleRepository.findDistinctProgramas(), ProgramaEnum.class);
    }
    
    private List<Map<String, String>> obtenerValor(List<String> listaBD,
            Class<? extends Enum<? extends EnumUtils.ValorEnum>> enumClass) {

        // Usamos LinkedHashMap para mantener el orden y evitar duplicados basados en el valor
        Map<String, String> mapaUnificado = new LinkedHashMap<>();

        // 1. Agregar valores desde la BD
        if (listaBD != null) {
            listaBD.forEach(valor -> {
                String clave = valor.trim().toLowerCase();
                mapaUnificado.putIfAbsent(clave, valor);
            });
        }

        // 2. Agregar valores desde el enum
        Arrays.stream(enumClass.getEnumConstants())
                .map(e -> (EnumUtils.ValorEnum) e)
                .forEach(e -> {
                    String clave = e.getValor().trim().toLowerCase();
                    mapaUnificado.putIfAbsent(clave, e.getValor());
                });

        // 3. Convertir a List<Map<String, String>>
        return mapaUnificado.values().stream()
                .map(valor -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("codigo", valor); // puedes ajustar si quieres usar name() aquí
                    map.put("nombre", valor);
                    return map;
                })
                .collect(Collectors.toList());
    }


    private List<Map<String, Object>> obtenerRoles() {
        return rolRepository.findAll().stream()
            .filter(Objects::nonNull)
            .map(rol -> Map.<String, Object>of("codigo", rol.getOid(), "nombre", rol.getNombre()))
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> obtenerTipoActividades() {
        return tipoActividadRepository.findAll().stream()
            .filter(Objects::nonNull)
            .map(tipoActividad -> Map.<String, Object>of("codigo", tipoActividad.getOidTipoActividad(), "nombre", tipoActividad.getNombre()))
            .collect(Collectors.toList());
    }
}
