package co.edu.unicauca.sed.api.service;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.ActividadBoolean;
import co.edu.unicauca.sed.api.domain.ActividadDate;
import co.edu.unicauca.sed.api.domain.ActividadDecimal;
import co.edu.unicauca.sed.api.domain.ActividadInt;
import co.edu.unicauca.sed.api.domain.ActividadVarchar;
import co.edu.unicauca.sed.api.domain.EavAtributo;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.AtributoDTO;
import co.edu.unicauca.sed.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sed.api.repository.ActividadBooleanRepository;
import co.edu.unicauca.sed.api.repository.ActividadDateRepository;
import co.edu.unicauca.sed.api.repository.ActividadDecimalRepository;
import co.edu.unicauca.sed.api.repository.ActividadIntRepository;
import co.edu.unicauca.sed.api.repository.ActividadVarcharRepository;
import co.edu.unicauca.sed.api.repository.EavAtributoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para gestionar EAV Atributos con soporte para CRUD y paginación.
 */
@Service
@RequiredArgsConstructor
public class EavAtributoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EavAtributoService.class);

    @Autowired
    private ActividadIntRepository actividadIntRepository;

    @Autowired
    private ActividadDecimalRepository actividadDecimalRepository;

    @Autowired
    private ActividadVarcharRepository actividadVarcharRepository;

    @Autowired
    private ActividadDateRepository actividadDateRepository;

    @Autowired
    private ActividadBooleanRepository actividadBooleanRepository;

    @Autowired
    private EavAtributoRepository eavAtributoRepository;

    /**
     * Obtiene una lista paginada de atributos EAV.
     */
    public ResponseEntity<ApiResponse<Page<EavAtributo>>> obtenerEavAtributos(int page, int size) {
        try {
            Page<EavAtributo> eavAtributos = eavAtributoRepository.findAll(PageRequest.of(page, size));
            return ResponseEntity.ok(new ApiResponse<>(200, "Atributos obtenidos correctamente", eavAtributos));
        } catch (Exception e) {
            LOGGER.error("❌ Error al obtener atributos EAV", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al obtener atributos EAV", null));
        }
    }

    /**
     * Obtiene un atributo por su ID.
     */
    public ResponseEntity<ApiResponse<EavAtributo>> obtenerEavAtributoPorId(Integer id) {
        try {
            Optional<EavAtributo> atributo = eavAtributoRepository.findById(id);
            return atributo.map(value -> ResponseEntity.ok(new ApiResponse<>(200, "Atributo encontrado", value)))
                    .orElseGet(() -> ResponseEntity.status(404).body(new ApiResponse<>(404, "Atributo no encontrado", null)));
        } catch (Exception e) {
            LOGGER.error("❌ Error al obtener el atributo EAV", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al obtener el atributo EAV", null));
        }
    }

    /**
     * Crea un nuevo atributo EAV.
     */
    public ResponseEntity<ApiResponse<EavAtributo>> crearEavAtributo(EavAtributo atributo) {
        try {
            EavAtributo nuevoAtributo = eavAtributoRepository.save(atributo);
            LOGGER.info("✅ Atributo EAV creado correctamente: {}", nuevoAtributo);
            return ResponseEntity.ok(new ApiResponse<>(201, "Atributo creado exitosamente", nuevoAtributo));
        } catch (Exception e) {
            LOGGER.error("❌ Error al crear el atributo EAV", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al crear el atributo EAV", null));
        }
    }

    /**
     * Actualiza un atributo existente.
     */
    public ResponseEntity<ApiResponse<EavAtributo>> actualizarEavAtributo(Integer id, EavAtributo atributo) {
        try {
            if (!eavAtributoRepository.existsById(id)) {
                return ResponseEntity.status(404).body(new ApiResponse<>(404, "Atributo no encontrado", null));
            }
            atributo.setOideavAtributo(id);
            EavAtributo actualizado = eavAtributoRepository.save(atributo);
            LOGGER.info("✅ Atributo EAV actualizado correctamente: {}", actualizado);
            return ResponseEntity.ok(new ApiResponse<>(200, "Atributo actualizado correctamente", actualizado));
        } catch (Exception e) {
            LOGGER.error("❌ Error al actualizar el atributo EAV", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al actualizar el atributo EAV", null));
        }
    }

    /**
     * Elimina un atributo por su ID.
     */
    public ResponseEntity<ApiResponse<Void>> eliminarEavAtributo(Integer id) {
        try {
            if (!eavAtributoRepository.existsById(id)) {
                return ResponseEntity.status(404).body(new ApiResponse<>(404, "Atributo no encontrado", null));
            }
            eavAtributoRepository.deleteById(id);
            LOGGER.info("✅ Atributo EAV eliminado con ID: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Atributo eliminado correctamente", null));
        } catch (Exception e) {
            LOGGER.error("❌ Error al eliminar el atributo EAV", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error al eliminar el atributo EAV", null));
        }
    }

    public void actualizarAtributosDinamicos(ActividadBaseDTO actividadDTO, Actividad actividad, Map<String, EavAtributo> cacheAtributos) {
        // Eliminar los atributos actuales de la actividad
        actividadVarcharRepository.deleteByActividad(actividad);
        actividadVarcharRepository.flush();
        actividadDecimalRepository.deleteByActividad(actividad);
        actividadDecimalRepository.flush();
        actividadIntRepository.deleteByActividad(actividad);
        actividadIntRepository.flush();
        actividadBooleanRepository.deleteByActividad(actividad);
        actividadBooleanRepository.flush();
        actividadDateRepository.deleteByActividad(actividad);
        actividadDateRepository.flush();
    
        // Guardar los nuevos atributos
        guardarAtributosDinamicos(actividadDTO, actividad, cacheAtributos);
    }

    public void guardarAtributosDinamicos(ActividadBaseDTO actividadDTO,
                                      Actividad actividad,
                                      Map<String, EavAtributo> cacheAtributos) {
        List<ActividadVarchar> actividadVarcharList = new ArrayList<>();
        List<ActividadDecimal> actividadDecimalList = new ArrayList<>();
        List<ActividadInt> actividadIntList = new ArrayList<>();
        List<ActividadBoolean> actividadBooleanList = new ArrayList<>();
        List<ActividadDate> actividadDateList = new ArrayList<>();

        for (AtributoDTO atributoDTO : actividadDTO.getAtributos()) {
            if (atributoDTO.getValor() == null || atributoDTO.getValor().trim().isEmpty()) {
                continue;
            }
    
            EavAtributo atributo = cacheAtributos.get(atributoDTO.getCodigoAtributo());
    
            switch (atributo.getTipoDato()) {
                case VARCHAR:
                    actividadVarcharList.add(new ActividadVarchar(actividad, atributo, atributoDTO.getValor()));
                    break;
    
                case FLOAT:
                    try {
                        Float valorDecimal = Float.parseFloat(atributoDTO.getValor());
                        actividadDecimalList.add(new ActividadDecimal(actividad, atributo, valorDecimal));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Error al convertir el valor a FLOAT: " + atributoDTO.getValor(), e);
                    }
                    break;
    
                case INT:
                    try {
                        Integer valorEntero = Integer.parseInt(atributoDTO.getValor());
                        actividadIntList.add(new ActividadInt(actividad, atributo, valorEntero));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Error al convertir el valor a INT: " + atributoDTO.getValor());
                    }
                    break;
    
                case BOOLEAN:
                    Boolean valorBooleano = Boolean.parseBoolean(atributoDTO.getValor());
                    actividadBooleanList.add(new ActividadBoolean(actividad, atributo, valorBooleano));
                    break;
    
                case DATE:
                    try {
                        LocalDateTime valorFecha = LocalDateTime.parse(atributoDTO.getValor());
                        actividadDateList.add(new ActividadDate(actividad, atributo, valorFecha));
                    } catch (DateTimeParseException e) {
                        throw new IllegalArgumentException("Error al convertir el valor a DATE: " + atributoDTO.getValor());
                    }
                    break;
    
                default:
                    throw new IllegalArgumentException("Tipo de dato no soportado: " + atributo.getTipoDato());
            }
        }
    
        // Guardar en batch para mejorar rendimiento
        if (!actividadVarcharList.isEmpty()) {
            actividadVarcharRepository.saveAll(actividadVarcharList);
        }
        if (!actividadDecimalList.isEmpty()) {
            actividadDecimalRepository.saveAll(actividadDecimalList);
        }
        if (!actividadIntList.isEmpty()) {
            actividadIntRepository.saveAll(actividadIntList);
        }
        if (!actividadBooleanList.isEmpty()) {
            actividadBooleanRepository.saveAll(actividadBooleanList);
        }
        if (!actividadDateList.isEmpty()) {
            actividadDateRepository.saveAll(actividadDateList);
        }
    }

    public List<AtributoDTO> obtenerAtributosPorActividad(Actividad actividad) {
        List<AtributoDTO> atributos = new ArrayList<>();
        
        actividadVarcharRepository.findByActividad(actividad)
            .forEach(a -> atributos.add(new AtributoDTO(a.getEavAtributo().getNombre(), a.getValor())));
    
        actividadDecimalRepository.findByActividad(actividad)
            .forEach(a -> atributos.add(new AtributoDTO(a.getEavAtributo().getNombre(), a.getValor().toString())));
    
        actividadIntRepository.findByActividad(actividad)
            .forEach(a -> atributos.add(new AtributoDTO(a.getEavAtributo().getNombre(), a.getValor().toString())));
    
        actividadBooleanRepository.findByActividad(actividad)
            .forEach(a -> atributos.add(new AtributoDTO(a.getEavAtributo().getNombre(), a.getValor().toString())));
    
        actividadDateRepository.findByActividad(actividad)
            .forEach(a -> atributos.add(new AtributoDTO(a.getEavAtributo().getNombre(), a.getValor().toString())));
    
        return atributos;
    }    
}
