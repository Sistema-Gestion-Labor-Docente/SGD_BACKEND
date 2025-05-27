package co.edu.unicauca.sed.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unicauca.sed.api.domain.Rol;
import co.edu.unicauca.sed.api.repository.RolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Servicio para la gestión de roles.
 * Proporciona métodos para realizar operaciones CRUD sobre los roles.
 */
@Service
public class RolService {

    private static final Logger logger = LoggerFactory.getLogger(RolService.class);

    @Autowired
    private RolRepository rolRepository;

    /**
     * Recupera todos los roles disponibles.
     *
     * @return Lista de roles.
     */
    public Page<Rol> findAll(Pageable pageable) {
        return rolRepository.findAll(pageable);
    }

    /**
     * Busca un rol por su ID.
     *
     * @param oid El ID del rol.
     * @return El rol encontrado o null si no existe.
     */
    public Optional<Rol> findByOid(Integer id) {
        return rolRepository.findById(id);
    }

    /**
     * Guarda un nuevo rol.
     *
     * @param rol El objeto Rol a guardar.
     * @return El rol guardado.
     */
    public Rol save(Rol rol) {
        if (rol.getNombre() != null) {
            rol.setNombre(rol.getNombre().toUpperCase());
        }
        return rolRepository.save(rol);
    }

    /**
     * Actualiza un rol existente.
     *
     * @param oid El ID del rol a actualizar.
     * @param rol Datos actualizados del rol.
     * @return true si la actualización fue exitosa, false si el rol no existe.
     */
    public Rol update(Integer id, Rol updatedRol) {
        Rol existingRol = rolRepository.findById(id).orElseThrow(() -> new RuntimeException("Rol con ID " + id + " no encontrado."));
    
        // Actualizar los campos del rol existente
        existingRol.setNombre(updatedRol.getNombre().toUpperCase());
    
        // Guardar y devolver el rol actualizado
        return rolRepository.save(existingRol);
    }

    /**
     * Elimina un rol por su ID.
     *
     * @param oid El ID del rol a eliminar.
     */
    public void delete(Integer oid) {
        logger.info("Eliminando rol con ID: {}", oid);
        rolRepository.deleteById(oid);
    }

        /**
     * Procesa y persiste una lista de roles.
     */
    public List<Rol> processRoles(List<Rol> roles) {
        List<Rol> rolesPersistidos = new ArrayList<>();
        for (Rol rol : roles) {
            if (rol.getOid() != null) {
                Rol rolExistente = rolRepository.findById(rol.getOid())
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado con OID: " + rol.getOid()));
                rolesPersistidos.add(rolExistente);
            } else {
                rolesPersistidos.add(rolRepository.save(rol));
            }
        }
        return rolesPersistidos;
    }
}
