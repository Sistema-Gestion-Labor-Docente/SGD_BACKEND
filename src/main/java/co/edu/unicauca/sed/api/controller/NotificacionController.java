package co.edu.unicauca.sed.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.sed.api.service.fuente.FuenteNotificacionService;

@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    private final FuenteNotificacionService fuenteNotificacionService;

    public NotificacionController(FuenteNotificacionService fuenteNotificacionService) {
        this.fuenteNotificacionService = fuenteNotificacionService;
    }

    @GetMapping("/test-envio")
    public String ejecutarNotificacionManual() {
        fuenteNotificacionService.notificarFuentesPendientes();
        return "Notificación ejecutada manualmente.";
    }
}
