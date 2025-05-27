package co.edu.unicauca.sed.api.service.usuario;

import co.edu.unicauca.sed.api.domain.Usuario;
import co.edu.unicauca.sed.api.domain.UsuarioDetalle;
import co.edu.unicauca.sed.api.repository.UsuarioDetalleRepository;
import co.edu.unicauca.sed.api.utils.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio para gestionar los detalles de usuario.
 */
@Service
public class UsuarioDetalleServiceImpl implements UsuarioDetalleService {

    @Autowired
    private UsuarioDetalleRepository usuarioDetalleRepository;

    @Autowired
    private StringUtils stringUtils;

    @Override
    public List<UsuarioDetalle> obtenerTodos() {
        return usuarioDetalleRepository.findAll();
    }

    @Override
    public UsuarioDetalle buscarPorOid(Integer oid) {
        return usuarioDetalleRepository.findById(oid).orElse(null);
    }

    @Override
    @Transactional
    public UsuarioDetalle guardar(UsuarioDetalle usuarioDetalle) {
        normalizarUsuarioDetalle(usuarioDetalle);
        return usuarioDetalleRepository.save(usuarioDetalle);
    }

    @Override
    public void eliminar(Integer oid) {
        usuarioDetalleRepository.deleteById(oid);
    }

    @Override
    @Transactional
    public void procesarUsuarioDetalle(Usuario usuario) {
        if (usuario.getUsuarioDetalle() != null) {
            UsuarioDetalle usuarioDetalle = usuario.getUsuarioDetalle();
            normalizarUsuarioDetalle(usuarioDetalle);

            try {
                UsuarioDetalle usuarioDetalleProcesado;

                if (usuarioDetalle.getOidUsuarioDetalle() != null) {
                    usuarioDetalleProcesado = usuarioDetalleRepository.findById(usuarioDetalle.getOidUsuarioDetalle())
                        .orElseThrow(() -> new RuntimeException(
                                "UsuarioDetalle no encontrado con OID: " + usuarioDetalle.getOidUsuarioDetalle()));

                    fusionarUsuarioDetalle(usuarioDetalleProcesado, usuarioDetalle);

                } else {
                    usuarioDetalleProcesado = usuarioDetalleRepository.save(usuarioDetalle);
                }

                usuario.setUsuarioDetalle(usuarioDetalleProcesado);

            } catch (Exception e) {
                throw new RuntimeException("Error al procesar el detalle del usuario: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Normaliza los datos del detalle del usuario convirtiéndolos a mayúsculas.
     * 
     * @param detalle Detalle del usuario a normalizar.
     */
    private void normalizarUsuarioDetalle(UsuarioDetalle detalle) {
        detalle.setFacultad(stringUtils.safeToUpperCase(detalle.getFacultad()));
        detalle.setDepartamento(stringUtils.safeToUpperCase(detalle.getDepartamento()));
        detalle.setCategoria(stringUtils.safeToUpperCase(detalle.getCategoria()));
        detalle.setContratacion(stringUtils.safeToUpperCase(detalle.getContratacion()));
        detalle.setDedicacion(stringUtils.safeToUpperCase(detalle.getDedicacion()));
        detalle.setEstudios(stringUtils.safeToUpperCase(detalle.getEstudios()));
        detalle.setPrograma(stringUtils.safeToUpperCase(detalle.getPrograma()));
    }

    /**
     * Fusiona los datos de un UsuarioDetalle existente con los datos recibidos.
     * 
     * @param existente UsuarioDetalle existente en la base de datos.
     * @param recibido  UsuarioDetalle con los nuevos datos.
     */
    private void fusionarUsuarioDetalle(UsuarioDetalle existente, UsuarioDetalle recibido) {
        existente.setFacultad(recibido.getFacultad());
        existente.setDepartamento(recibido.getDepartamento());
        existente.setCategoria(recibido.getCategoria());
        existente.setContratacion(recibido.getContratacion());
        existente.setDedicacion(recibido.getDedicacion());
        existente.setEstudios(recibido.getEstudios());
        existente.setPrograma(recibido.getPrograma());
    }
}
