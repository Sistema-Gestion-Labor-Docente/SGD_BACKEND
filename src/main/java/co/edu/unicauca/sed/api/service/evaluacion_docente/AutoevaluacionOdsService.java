package co.edu.unicauca.sed.api.service.evaluacion_docente;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import co.edu.unicauca.sed.api.domain.Autoevaluacion;
import co.edu.unicauca.sed.api.domain.Fuente;
import co.edu.unicauca.sed.api.dto.ArchivoDTO;
import co.edu.unicauca.sed.api.dto.OdsDTO;

public interface AutoevaluacionOdsService {
    public void guardarOds(List<OdsDTO> odsList, Autoevaluacion autoevaluacion, List<MultipartFile> archivosOds, Fuente fuente);

    public ArchivoDTO obtenerArchivoPorId(Integer idOds);

    public List<Map<String, Object>> obtenerOds(Autoevaluacion autoevaluacion);

    //public List<MultipartFile> mapearArchivoODS(List<OdsDTO> odsSeleccionados, List<MultipartFile> archivos);

    public Integer obtenerMaxOidOds();
}