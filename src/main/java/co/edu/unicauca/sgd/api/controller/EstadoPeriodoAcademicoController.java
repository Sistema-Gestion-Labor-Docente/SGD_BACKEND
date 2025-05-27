package co.edu.unicauca.sgd.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.domain.EstadoPeriodoAcademico;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.service.periodo_academico.EstadoPeriodoAcademicoService;

@RestController
@RequestMapping("api/estado-periodo-academico")
public class EstadoPeriodoAcademicoController {

    private static final Logger logger = LoggerFactory.getLogger(EstadoPeriodoAcademicoController.class);
    private final EstadoPeriodoAcademicoService service;

    public EstadoPeriodoAcademicoController(EstadoPeriodoAcademicoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EstadoPeriodoAcademico>> create(@RequestBody EstadoPeriodoAcademico estadoPeriodoAcademico) {
        return ResponseEntity.ok(service.guardar(estadoPeriodoAcademico));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EstadoPeriodoAcademico>> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EstadoPeriodoAcademico>>> findAll(Pageable pageable) {
        return ResponseEntity.ok(service.buscarTodos(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EstadoPeriodoAcademico>> update(@PathVariable Integer id, @RequestBody EstadoPeriodoAcademico estadoPeriodoAcademico) {
        return ResponseEntity.ok(service.actualizar(id, estadoPeriodoAcademico));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        logger.info("Solicitud para eliminar EstadoPeriodoAcademico con id: {}", id);
        return ResponseEntity.ok(service.eliminar(id));
    }
}
