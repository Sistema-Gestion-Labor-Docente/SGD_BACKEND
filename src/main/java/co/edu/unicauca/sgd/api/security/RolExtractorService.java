package co.edu.unicauca.sgd.api.security;

import org.springframework.stereotype.Service;

import co.edu.unicauca.sgd.api.mapper.UsuarioMapper;

@Service
public class RolExtractorService {

    private final UsuarioMapper usuarioMapper;

    public RolExtractorService(UsuarioMapper usuarioMapper) {
        this.usuarioMapper = usuarioMapper;
    }

    public String obtenerRolDesdeCorreoOClaims(String correo, String rolDesdeToken) {
        return usuarioMapper.obtenerUsuarioPorCorreo(correo)
                .flatMap(usuario -> usuario.getRoles().stream().findFirst())
                .map(rol -> rol.getNombre())
                .orElse(rolDesdeToken);
    }
}
