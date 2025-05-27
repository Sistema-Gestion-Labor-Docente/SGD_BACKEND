package co.edu.unicauca.sed.api.service.fuente;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.EstadoFuente;
import co.edu.unicauca.sed.api.domain.Fuente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interfaz para el manejo de fuentes y sus archivos asociados.
 */
public interface FuenteService {

    Page<Fuente> obtenerTodos(Pageable pageable);

    Fuente buscarPorId(Integer oid);

    List<Fuente> buscarPorActividadId(Integer oidActividad);

    Fuente guardar(Fuente fuente);

    void eliminar(Integer oid);

    void guardarFuente(String fuentesJson, MultipartFile informeFuente, String observacion, Map<String, MultipartFile> archivos);

    ResponseEntity<?> obtenerArchivo(Integer id, boolean esInforme);

    void crearTipoFuente(Actividad actividad, EstadoFuente estadoFuente);
    
    String guardarDocumentoFuente(Fuente fuente, MultipartFile documentoFuente, String prefijo) throws IOException;

    Fuente obtenerFuente(Integer oidFuente);
}
