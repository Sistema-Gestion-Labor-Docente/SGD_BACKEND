package co.edu.unicauca.sed.api.service.usuario;

import co.edu.unicauca.sed.api.domain.EstadoUsuario;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.repository.EstadoUsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class EstadoUsuarioServiceImpl implements EstadoUsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(EstadoUsuarioServiceImpl.class);

    @Autowired
    private EstadoUsuarioRepository repository;

    @Override
    public ApiResponse<EstadoUsuario> crear(EstadoUsuario estadoUsuario) {
        try {
            EstadoUsuario nuevoEstado = repository.save(estadoUsuario);
            return new ApiResponse<>(201, "Estado de usuario creado exitosamente.", nuevoEstado);
        } catch (Exception e) {
            logger.error("❌ Error al crear EstadoUsuario", e);
            return new ApiResponse<>(500, "Error al crear estado de usuario: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<EstadoUsuario> buscarPorId(Integer id) {
        try {
            Optional<EstadoUsuario> estadoUsuario = repository.findById(id);
            return estadoUsuario.map(value ->
                    new ApiResponse<>(200, "Estado de usuario encontrado.", value))
                    .orElseGet(() -> new ApiResponse<>(404, "Estado de usuario no encontrado.", null));
        } catch (Exception e) {
            logger.error("❌ Error al buscar EstadoUsuario con ID: {}", id, e);
            return new ApiResponse<>(500, "Error al buscar estado de usuario: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Page<EstadoUsuario>> buscarTodos(Pageable pageable) {
        try {
            Page<EstadoUsuario> estados = repository.findAll(pageable);
            return new ApiResponse<>(200, "Lista de estados de usuario obtenida correctamente.", estados);
        } catch (Exception e) {
            logger.error("❌ Error al listar EstadosUsuario", e);
            return new ApiResponse<>(500, "Error al listar estados de usuario: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<EstadoUsuario> actualizar(Integer id, EstadoUsuario estadoUsuario) {
        try {
            return repository.findById(id).map(existing -> {
                existing.setNombre(estadoUsuario.getNombre());
                EstadoUsuario actualizado = repository.save(existing);
                return new ApiResponse<>(200, "Estado de usuario actualizado correctamente.", actualizado);
            }).orElseGet(() -> new ApiResponse<>(404, "Estado de usuario no encontrado.", null));
        } catch (Exception e) {
            logger.error("❌ Error al actualizar EstadoUsuario con ID: {}", id, e);
            return new ApiResponse<>(500, "Error al actualizar estado de usuario: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Void> eliminar(Integer id) {
        try {
            logger.info("🗑️ Eliminando EstadoUsuario con ID: {}", id);
            if (!repository.existsById(id)) {
                return new ApiResponse<>(404, "Estado de usuario no encontrado.", null);
            }
            repository.deleteById(id);
            return new ApiResponse<>(200, "Estado de usuario eliminado correctamente.", null);
        } catch (Exception e) {
            logger.error("❌ Error al eliminar EstadoUsuario con ID: {}", id, e);
            return new ApiResponse<>(500, "Error al eliminar estado de usuario: " + e.getMessage(), null);
        }
    }
}
