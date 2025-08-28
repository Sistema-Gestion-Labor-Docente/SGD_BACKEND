package co.edu.unicauca.sgd.api.service.materias;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public interface PlanDocumentosService {

    public ByteArrayOutputStream generarFormatoAdicion(Integer oidPlan) throws IOException;

    public void cargarMateriasDesdeExcel(InputStream excelStream, Integer oidPlan) throws IOException;

}
