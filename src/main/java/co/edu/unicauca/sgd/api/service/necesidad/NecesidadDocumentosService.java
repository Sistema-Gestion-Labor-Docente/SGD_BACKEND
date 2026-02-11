package co.edu.unicauca.sgd.api.service.necesidad;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface NecesidadDocumentosService {

    ByteArrayOutputStream generarFormatoNecesidades(Integer oidCalendario, Integer oidDepartamento) throws IOException;
}
