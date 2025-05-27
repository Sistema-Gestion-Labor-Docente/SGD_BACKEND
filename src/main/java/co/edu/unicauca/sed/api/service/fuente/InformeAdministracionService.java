package co.edu.unicauca.sed.api.service.fuente;

import co.edu.unicauca.sed.api.domain.InformeAdministracion;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.InformeAdministracionFuenteDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface InformeAdministracionService {

    void guardar(InformeAdministracionFuenteDTO informeDTO, MultipartFile documentoAdministracion);

    InformeAdministracion buscarPorId(Integer id);

    Page<InformeAdministracion> listar(Pageable pageable);

    ApiResponse<Object> obtenerDetalleInformeAdministracion(Integer oidFuente);
}