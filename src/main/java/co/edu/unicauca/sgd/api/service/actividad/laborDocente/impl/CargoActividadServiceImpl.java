package co.edu.unicauca.sgd.api.service.actividad.laborDocente.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import co.edu.unicauca.sgd.api.domain.CargoActividad;
import co.edu.unicauca.sgd.api.domain.TipoActividad;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.CargoActividadDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.CargoActividadDTOResponse;
import co.edu.unicauca.sgd.api.mapper.CargoActividadMapper;
import co.edu.unicauca.sgd.api.repository.CargoActividadRepository;
import co.edu.unicauca.sgd.api.repository.TipoActividadRepository;
import co.edu.unicauca.sgd.api.service.actividad.laborDocente.CargoActividadService;
import jakarta.transaction.Transactional;

@Service
public class CargoActividadServiceImpl implements CargoActividadService {

    private static final Logger logger = LoggerFactory.getLogger(CargoActividadServiceImpl.class);

    private final CargoActividadRepository cargoActividadRepository;

    private final TipoActividadRepository tipoActividadRepository;
    
    private final CargoActividadMapper cargoActividadMapper;

    public CargoActividadServiceImpl(CargoActividadRepository cargoActividadRepository, TipoActividadRepository tipoActividadRepository, CargoActividadMapper cargoActividadMapper) {
        this.cargoActividadRepository = cargoActividadRepository;
        this.tipoActividadRepository = tipoActividadRepository;
        this.cargoActividadMapper = cargoActividadMapper;
    }

    @Override
    public ApiResponse<Page<CargoActividadDTOResponse>> obtenerTodos(String nombre, String tipo, Integer oidTipoActividad, Pageable pageable) {
        try {
            Specification<CargoActividad> spec = Specification.where(null);
            if (nombre != null) {
                spec = spec.and((root, query, cb) -> cb.like(cb.upper(root.get("nombre")), "%" + nombre.toUpperCase() + "%"));
            }
            if (tipo != null) {
                spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("tipo")), tipo.toUpperCase()));
            }
            if (oidTipoActividad != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("tipoActividad").get("oidTipoActividad"), oidTipoActividad));
            }
            Page<CargoActividad> result = cargoActividadRepository.findAll(spec, pageable);
            Page<CargoActividadDTOResponse> page = result.map(cargoActividadMapper::toResponse);
            boolean hasContent = page.hasContent();
            String message = hasContent ? "Cargos de actividad encontrados" : "No se encontraron cargos de actividad.";
            return new ApiResponse<>(200, message, page);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al consultar cargos de actividad: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<CargoActividadDTOResponse> buscarPorId(Integer oid) {
        try {
            CargoActividad entity = cargoActividadRepository.findById(oid)
                .orElseThrow(() -> new IllegalStateException("CargoActividad no encontrado con ID: " + oid));
            return new ApiResponse<>(200, "Cargo de actividad encontrado", cargoActividadMapper.toResponse(entity));
        } catch (IllegalStateException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al buscar cargo de actividad: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<CargoActividadDTOResponse> guardar(CargoActividadDTORequest request) {
        try {
            if (request.getOidTipoActividad() == null) {
                throw new IllegalArgumentException("El tipo de actividad es obligatorio.");
            }
            TipoActividad tipoActividad = tipoActividadRepository.findById(request.getOidTipoActividad())
                    .orElseThrow(() -> new IllegalStateException("Tipo de actividad no encontrado con ID: " + request.getOidTipoActividad()));

            CargoActividad entity = cargoActividadMapper.convertToEntity(request, tipoActividad);
            entity.setUsuarioCreacion("admin"); // Cambia esto por el usuario logueado si aplica

            CargoActividad guardado = cargoActividadRepository.save(entity);
            return new ApiResponse<>(201, "Cargo de actividad guardado", cargoActividadMapper.toResponse(guardado));
        } catch (IllegalStateException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al guardar cargo de actividad: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<CargoActividadDTOResponse> actualizar(Integer oid, CargoActividadDTORequest request) {
        try {
            if (request.getOidTipoActividad() == null) {
                throw new IllegalArgumentException("El tipo de actividad es obligatorio.");
            }
            CargoActividad entity = cargoActividadRepository.findById(oid)
                    .orElseThrow(() -> new IllegalStateException("CargoActividad no encontrado con ID: " + oid));
            TipoActividad tipoActividad = tipoActividadRepository.findById(request.getOidTipoActividad())
                    .orElseThrow(() -> new IllegalStateException("Tipo de actividad no encontrado con ID: " + request.getOidTipoActividad()));

            cargoActividadMapper.actualizarCamposBasicos(entity, request, tipoActividad);
            entity.setUsuarioActualizacion("admin"); // Cambia esto por el usuario logueado si aplica

            CargoActividad actualizado = cargoActividadRepository.save(entity);
            return new ApiResponse<>(200, "Cargo de actividad actualizado", cargoActividadMapper.toResponse(actualizado));
        } catch (IllegalStateException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al actualizar cargo de actividad: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            if (!cargoActividadRepository.existsById(oid)) {
                throw new IllegalStateException("CargoActividad no encontrado con ID: " + oid);
            }
            cargoActividadRepository.deleteById(oid);
            return new ApiResponse<>(204, "Cargo de actividad eliminado correctamente", null);
        } catch (IllegalStateException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar cargo de actividad: " + e.getMessage(), null);
        }
    }
}
