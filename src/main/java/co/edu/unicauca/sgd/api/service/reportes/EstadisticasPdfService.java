package co.edu.unicauca.sgd.api.service.reportes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import co.edu.unicauca.sgd.api.dto.reportes.ReporteEstadisticasPdfSeleccionRequest;

public interface EstadisticasPdfService {

    ByteArrayOutputStream generarReportePdf(ReporteEstadisticasPdfSeleccionRequest request) throws IOException;
}
