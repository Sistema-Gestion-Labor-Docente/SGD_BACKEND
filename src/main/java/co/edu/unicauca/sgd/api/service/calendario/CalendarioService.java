package co.edu.unicauca.sgd.api.service.calendario;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTOResponse;

public interface CalendarioService {

    ApiResponse<Page<CalendarioDTOResponse>> obtenerTodos(String anioCalendario,
                                                          Integer numeroCalendario,
                                                          String estado,
                                                          Pageable pageable);

    ApiResponse<CalendarioDTOResponse> buscarPorId(Integer oid);

    ApiResponse<CalendarioDTOResponse> guardar(CalendarioDTORequest dto);

    ApiResponse<CalendarioDTOResponse> actualizar(Integer id, CalendarioDTORequest dto);

    ApiResponse<Void> eliminar(Integer oid);

    ByteArrayOutputStream generarCalendarioPdf(Integer oidCalendario) throws IOException;
}

