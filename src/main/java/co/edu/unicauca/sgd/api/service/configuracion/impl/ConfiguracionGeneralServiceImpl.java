package co.edu.unicauca.sgd.api.service.configuracion.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import co.edu.unicauca.sgd.api.domain.ConfiguracionGeneral;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.configuracion.ConfiguracionGeneralDTORequest;
import co.edu.unicauca.sgd.api.dto.configuracion.ConfiguracionGeneralDTOResponse;
import co.edu.unicauca.sgd.api.mapper.ConfiguracionGeneralMapper;
import co.edu.unicauca.sgd.api.repository.ConfiguracionGeneralRepository;
import co.edu.unicauca.sgd.api.service.configuracion.ConfiguracionGeneralService;
import jakarta.transaction.Transactional;

@Service
public class ConfiguracionGeneralServiceImpl implements ConfiguracionGeneralService {

    private static final String CLAVE_SED_URL = "sedUrlBase";

    private final ConfiguracionGeneralRepository configuracionGeneralRepository;
    private final ConfiguracionGeneralMapper configuracionGeneralMapper;

    public ConfiguracionGeneralServiceImpl(ConfiguracionGeneralRepository configuracionGeneralRepository,
            ConfiguracionGeneralMapper configuracionGeneralMapper) {
        this.configuracionGeneralRepository = configuracionGeneralRepository;
        this.configuracionGeneralMapper = configuracionGeneralMapper;
    }

    @Override
    public ApiResponse<Page<ConfiguracionGeneralDTOResponse>> obtenerTodos(Pageable pageable) {
        try {
            Page<ConfiguracionGeneral> page = configuracionGeneralRepository.findAll(pageable);
            Page<ConfiguracionGeneralDTOResponse> mapped = page.map(configuracionGeneralMapper::toResponse);
            String message = mapped.hasContent() ? "Configuraciones obtenidas correctamente." : "No se encontraron configuraciones.";
            return new ApiResponse<>(200, message, mapped);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al listar configuraciones: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<ConfiguracionGeneralDTOResponse> buscarPorId(Integer oid) {
        try {
            ConfiguracionGeneral entity = configuracionGeneralRepository.findById(oid)
                    .orElseThrow(() -> new IllegalStateException("Configuración no encontrada con ID: " + oid));
            return new ApiResponse<>(200, "Configuración encontrada.", configuracionGeneralMapper.toResponse(entity));
        } catch (IllegalStateException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al buscar configuración: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<ConfiguracionGeneralDTOResponse> guardar(ConfiguracionGeneralDTORequest dto) {
        try {
            validarClave(dto.getClave());
            if (configuracionGeneralRepository.findByClave(dto.getClave()).isPresent()) {
                return new ApiResponse<>(409, "Ya existe una configuración con la clave: " + dto.getClave(), null);
            }
            ConfiguracionGeneral entity = configuracionGeneralMapper.toEntity(dto);
            ConfiguracionGeneral saved = configuracionGeneralRepository.save(entity);
            return new ApiResponse<>(200, "Configuración guardada correctamente.", configuracionGeneralMapper.toResponse(saved));
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al guardar configuración: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<ConfiguracionGeneralDTOResponse> actualizar(Integer oid, ConfiguracionGeneralDTORequest dto) {
        try {
            validarClave(dto.getClave());
            ConfiguracionGeneral existing = configuracionGeneralRepository.findById(oid)
                    .orElseThrow(() -> new IllegalStateException("Configuración no encontrada con ID: " + oid));

            if (!existing.getClave().equals(dto.getClave())
                    && configuracionGeneralRepository.findByClave(dto.getClave()).isPresent()) {
                return new ApiResponse<>(409, "Ya existe una configuración con la clave: " + dto.getClave(), null);
            }

            configuracionGeneralMapper.update(existing, dto);
            ConfiguracionGeneral saved = configuracionGeneralRepository.save(existing);
            return new ApiResponse<>(200, "Configuración actualizada correctamente.", configuracionGeneralMapper.toResponse(saved));
        } catch (IllegalStateException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al actualizar configuración: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            if (!configuracionGeneralRepository.existsById(oid)) {
                throw new IllegalStateException("Configuración no encontrada con ID: " + oid);
            }
            configuracionGeneralRepository.deleteById(oid);
            return new ApiResponse<>(200, "Configuración eliminada correctamente.", null);
        } catch (IllegalStateException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar configuración: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<String> obtenerSedUrl() {
        try {
            ConfiguracionGeneral entity = configuracionGeneralRepository.findByClave(CLAVE_SED_URL)
                    .orElseThrow(() -> new IllegalStateException("Configuración sedUrl no encontrada."));

            if (!entity.isHabilitado()) {
                return new ApiResponse<>(400, "La configuración sedUrl está deshabilitada.", null);
            }

            String valor = entity.getValor();
            if (!StringUtils.hasText(valor)) {
                return new ApiResponse<>(400, "La configuración sedUrl no tiene valor.", null);
            }
            return new ApiResponse<>(200, "URL de SED obtenida correctamente.", valor);
        } catch (IllegalStateException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al obtener la URL de SED: " + e.getMessage(), null);
        }
    }

    private void validarClave(String clave) {
        if (!StringUtils.hasText(clave)) {
            throw new IllegalArgumentException("La clave es obligatoria.");
        }
    }
}
