package co.edu.unicauca.sgd.api.service.calendario.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.sgd.api.domain.Fecha;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;
import co.edu.unicauca.sgd.api.repository.FechaRepository;
import co.edu.unicauca.sgd.api.service.calendario.FechaService;
import co.edu.unicauca.sgd.api.utils.StringUtils;

@Service
public class FechaServiceImpl implements FechaService {

    @Autowired
    private FechaRepository fechaRepository;

    @Override
    public ApiResponse<Page<Fecha>> obtenerTodas(String nombre, TipoFechaEnum tipo, Pageable pageable) {
        try {
            Specification<Fecha> spec = Specification.where(null);

            if (StringUtils.hasText(nombre)) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.upper(root.get("nombre")), "%" + nombre.toUpperCase() + "%"));
            }

            if (tipo != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("tipo"), tipo));
            }

            Page<Fecha> fechas = fechaRepository.findAll(spec, pageable);
            return new ApiResponse<>(200, "Fechas obtenidas correctamente", fechas);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al obtener fechas: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Fecha> buscarPorId(Integer oid) {
        try {
            Fecha fecha = fechaRepository.findById(oid)
                    .orElseThrow(() -> new RuntimeException("Fecha no encontrada con ID: " + oid));
            return new ApiResponse<>(200, "Fecha encontrada correctamente", fecha);
        } catch (RuntimeException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Fecha> guardar(Fecha fecha) {
        try {
            Fecha guardada = fechaRepository.save(fecha);
            return new ApiResponse<>(200, "Fecha guardada correctamente", guardada);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al guardar la fecha: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Fecha> actualizar(Integer id, Fecha fechaActualizada) {
        try {
            Fecha existente = fechaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Fecha no encontrada con ID: " + id));

            existente.setNombre(fechaActualizada.getNombre());
            existente.setFechaInicial(fechaActualizada.getFechaInicial());
            existente.setFechaFin(fechaActualizada.getFechaFin());
            existente.setTipo(fechaActualizada.getTipo());
            existente.setCalendario(fechaActualizada.getCalendario());

            Fecha actualizada = fechaRepository.save(existente);
            return new ApiResponse<>(200, "Fecha actualizada correctamente", actualizada);
        } catch (RuntimeException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al actualizar la fecha: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            if (!fechaRepository.existsById(oid)) {
                return new ApiResponse<>(404, "Fecha no encontrada con ID: " + oid, null);
            }
            fechaRepository.deleteById(oid);
            return new ApiResponse<>(200, "Fecha eliminada correctamente", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar la fecha: " + e.getMessage(), null);
        }
    }
}

