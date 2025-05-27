package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.InformeAdministracion;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.InformeAdministracionFuenteDTO;
import co.edu.unicauca.sed.api.service.fuente.InformeAdministracionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/informes-administracion")
@RequiredArgsConstructor
public class InformeAdministracionController {

    private final InformeAdministracionService informeAdministracionService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> guardar(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "documentoAdministracion", required = false) MultipartFile documentoAdministracion) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            InformeAdministracionFuenteDTO informeDTO = mapper.readValue(dataJson,InformeAdministracionFuenteDTO.class);

            informeAdministracionService.guardar(informeDTO, documentoAdministracion);

            return new ApiResponse<>(200, "Informe de administración guardado correctamente.", null);

        } catch (Exception e) {
            return new ApiResponse<>(400, "Error procesando la solicitud: " + e.getMessage(), null);
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<InformeAdministracion> buscarPorId(@PathVariable Integer id) {
        return new ApiResponse<>(200, "Informe de administración encontrado.", informeAdministracionService.buscarPorId(id));
    }

    @GetMapping
    public ApiResponse<?> listar(Pageable pageable) {
        return new ApiResponse<>(200, "Listado de informes de administración.", informeAdministracionService.listar(pageable));
    }

    @GetMapping("/fuente/{oidFuente}")
    public ResponseEntity<ApiResponse<Object>> obtenerDetalleInformeAdministracion(@PathVariable Integer oidFuente) {
        return ResponseEntity.ok(informeAdministracionService.obtenerDetalleInformeAdministracion(oidFuente));
    }
}
