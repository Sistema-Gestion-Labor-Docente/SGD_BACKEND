package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.LaborDocente;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.LaborDocenteRequestDTO;
import co.edu.unicauca.sed.api.service.LaborDocenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/labor-docente")
@RequiredArgsConstructor
public class LaborDocenteController {

    private final LaborDocenteService laborDocenteService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<LaborDocente>>> listar(Pageable pageable) {
        return ResponseEntity.ok(laborDocenteService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LaborDocente>> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(laborDocenteService.buscarPorId(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> guardar(
            @RequestPart("data") String dataJson,
            @RequestPart("documento") MultipartFile documento) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            LaborDocenteRequestDTO dto = mapper.readValue(dataJson, LaborDocenteRequestDTO.class);
            dto.setDocumento(documento);
            laborDocenteService.guardar(dto);
            return new ApiResponse<>(200, "Labor docente guardada correctamente.", null);
        } catch (Exception e) {
            return new ApiResponse<>(400, "Error procesando la solicitud: " + e.getMessage(), null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(laborDocenteService.eliminar(id));
    }

    @GetMapping("/descargar")
    public ResponseEntity<Resource> descargarDocumento(@RequestParam("oidUsuario") Integer oidUsuario) {
        return laborDocenteService.descargarDocumento(oidUsuario);
    }
}