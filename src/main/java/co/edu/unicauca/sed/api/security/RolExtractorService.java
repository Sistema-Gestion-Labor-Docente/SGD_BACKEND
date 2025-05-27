package co.edu.unicauca.sed.api.security;

import co.edu.unicauca.sed.api.mapper.UsuarioMapper;
import org.springframework.stereotype.Service;

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
