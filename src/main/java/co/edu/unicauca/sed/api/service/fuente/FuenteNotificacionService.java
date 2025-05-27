package co.edu.unicauca.sed.api.service.fuente;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import co.edu.unicauca.sed.api.client.ClienteNotificacion;
import co.edu.unicauca.sed.api.domain.Fuente;
import co.edu.unicauca.sed.api.domain.PeriodoAcademico;
import co.edu.unicauca.sed.api.domain.Proceso;
import co.edu.unicauca.sed.api.domain.Usuario;
import co.edu.unicauca.sed.api.repository.FuenteRepository;
import co.edu.unicauca.sed.api.repository.PeriodoAcademicoRepository;
import org.springframework.beans.factory.annotation.Value;

@Service
public class FuenteNotificacionService {

    private final FuenteRepository fuenteRepository;
    private final PeriodoAcademicoRepository periodoAcademicoRepository;
    private final ClienteNotificacion mensajeriaClient;

    @Value("${notificacion.dias.min}")
    private int diasMin;

    @Value("${notificacion.dias.max}")
    private int diasMax;

    @Value("${notificacion.habilitada:false}")
    private boolean notificacionHabilitada;

    public FuenteNotificacionService(FuenteRepository fuenteRepository,
                                     PeriodoAcademicoRepository periodoAcademicoRepository,
                                     ClienteNotificacion mensajeriaClient) {
        this.fuenteRepository = fuenteRepository;
        this.periodoAcademicoRepository = periodoAcademicoRepository;
        this.mensajeriaClient = mensajeriaClient;
    }

    @Scheduled(cron = "${notificacion.cron}", zone = "America/Bogota")
    public void notificarFuentesPendientes() {
        PeriodoAcademico periodoActivo = periodoAcademicoRepository
                .findByEstadoPeriodoAcademicoNombre("ACTIVO").orElse(null);

        if (periodoActivo == null || periodoActivo.getFechaFin() == null) {
            return;
        }

        LocalDate fechaHoy = LocalDate.now();
        LocalDate fechaFin = periodoActivo.getFechaFin();
        long diasFaltantes = ChronoUnit.DAYS.between(fechaHoy, fechaFin);

        if (diasFaltantes > diasMax || diasFaltantes < diasMin) {
            return;
        }

        List<Fuente> fuentesPendientes = fuenteRepository
            .findFuentesPendientesByTipoAndPeriodo(1, "2", periodoActivo.getOidPeriodoAcademico());

        for (Fuente fuente : fuentesPendientes) {
            Proceso proceso = fuente.getActividad().getProceso();
            if (!proceso.getOidPeriodoAcademico().getOidPeriodoAcademico()
                    .equals(periodoActivo.getOidPeriodoAcademico())) {
                continue;
            }
            Usuario evaluador = proceso.getEvaluador();

            if (evaluador != null && evaluador.getCorreo() != null) {
                String nombreActividad = fuente.getActividad().getNombreActividad();

                if (notificacionHabilitada) {
                    mensajeriaClient.enviarNotificacion(
                        List.of(evaluador.getCorreo()),
                        String.format("Actividad '%s' sin diligenciar", nombreActividad),
                        String.format(
                            "Estimado(a) %s %s, recuerde que tiene pendiente diligenciar la evaluación para la actividad '%s'.",
                            evaluador.getNombres(),
                            evaluador.getApellidos(),
                            nombreActividad
                        )
                    );
                } else {
                    System.out.println("📭 Notificación no enviada (deshabilitada en configuración).");
                }
            }
        }
    }
}
