package co.edu.unicauca.sgd.api.service.fuente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unicauca.sgd.api.domain.InformeAdministracion;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.InformeAdministracionFuenteDTO;

public interface InformeAdministracionService {

    void guardar(InformeAdministracionFuenteDTO informeDTO, MultipartFile documentoAdministracion);

    InformeAdministracion buscarPorId(Integer id);

    Page<InformeAdministracion> listar(Pageable pageable);

    ApiResponse<Object> obtenerDetalleInformeAdministracion(Integer oidFuente);
}