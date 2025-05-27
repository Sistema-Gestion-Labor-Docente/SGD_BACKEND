package co.edu.unicauca.sed.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import co.edu.unicauca.sed.api.service.documento.DocumentoService;

@RestController
@RequestMapping("api/documento")
public class DocumentoController {
    @Autowired
    private DocumentoService documentoService;

    @GetMapping("/evidencia-ods")
    public ResponseEntity<?> descargarDocumento(@RequestParam Integer idArchivo, @RequestParam String tipoArchivo) {
        return documentoService.obtenerArchivoPorTipo(idArchivo, tipoArchivo);
    }
}