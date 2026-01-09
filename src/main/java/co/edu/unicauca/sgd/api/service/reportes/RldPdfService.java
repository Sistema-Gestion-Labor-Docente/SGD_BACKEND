package co.edu.unicauca.sgd.api.service.reportes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import co.edu.unicauca.sgd.api.dto.reportes.RldPdfRequest;

public interface RldPdfService {

    ByteArrayOutputStream generarRldPdf(RldPdfRequest request) throws IOException;
}
