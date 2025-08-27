package co.edu.unicauca.sgd.api.service.materias;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface PlanDocumentosService {

    public ByteArrayOutputStream generarFormatoAdicion(Integer oidPlan) throws IOException;

}
