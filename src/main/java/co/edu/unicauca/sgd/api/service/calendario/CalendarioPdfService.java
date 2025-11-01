package co.edu.unicauca.sgd.api.service.calendario;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTOResponse;

public interface CalendarioPdfService {

    ByteArrayOutputStream generarCalendarioPdf(Calendario calendario, List<FechaDTOResponse> fechas) throws IOException;
}
