package co.edu.unicauca.sed.api.service.fuente.impl;

import co.edu.unicauca.sed.api.domain.EstadoFuente;
import co.edu.unicauca.sed.api.domain.Fuente;
import co.edu.unicauca.sed.api.domain.InformeAdministracion;
import co.edu.unicauca.sed.api.domain.ObjetivoComponente;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.InformeAdministracionFuenteDTO;
import co.edu.unicauca.sed.api.mapper.EvaluacionMapperUtil;
import co.edu.unicauca.sed.api.repository.FuenteRepository;
import co.edu.unicauca.sed.api.repository.InformeAdministracionRepository;
import co.edu.unicauca.sed.api.repository.ObjetivoComponenteRepository;
import co.edu.unicauca.sed.api.service.fuente.FuenteBusinessServiceImpl;
import co.edu.unicauca.sed.api.service.fuente.FuenteService;
import co.edu.unicauca.sed.api.service.fuente.InformeAdministracionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class InformeAdministracionServiceImpl implements InformeAdministracionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InformeAdministracionServiceImpl.class);
    private static final String PREFIJO_INFORME_ADMINISTRACION = "INFORMEADMINISTRACION";

    private final FuenteRepository fuenteRepository;
    private final InformeAdministracionRepository informeAdministracionRepository;
    private final ObjetivoComponenteRepository objetivoComponenteRepository;
    private final FuenteService fuenteService;
    private final FuenteBusinessServiceImpl fuenteBusinessService;

    @Override
    public void guardar(InformeAdministracionFuenteDTO informeDTO, MultipartFile documentoAdministracion) {
        Fuente fuente = obtenerFuente(informeDTO.getOidFuente());

        actualizarDatosFuente(fuente, informeDTO, documentoAdministracion);

        List<InformeAdministracion> informesProcesados = construirInformesAdministracion(fuente, informeDTO);

        informeAdministracionRepository.saveAll(informesProcesados);
    }

    private Fuente obtenerFuente(Integer oidFuente) {
        return fuenteRepository.findById(oidFuente)
                .orElseThrow(() -> new RuntimeException("Fuente no encontrada con ID: " + oidFuente));
    }

    private void actualizarDatosFuente(Fuente fuente, InformeAdministracionFuenteDTO informeDTO, MultipartFile documentoAdministracion) {
        fuente.setTipoCalificacion(informeDTO.getTipoCalificacion());
        fuente.setCalificacion(informeDTO.getCalificacion());
        fuente.setObservacion(informeDTO.getObservacion());
        EstadoFuente estadoFuente = fuenteBusinessService.determinarEstadoFuente(fuente);
        fuente.setEstadoFuente(estadoFuente);

        guardarArchivoAdministracion(fuente, documentoAdministracion);

        fuenteRepository.save(fuente);
    }

    private List<InformeAdministracion> construirInformesAdministracion(Fuente fuente,
            InformeAdministracionFuenteDTO informeDTO) {
        List<InformeAdministracion> informesExistentes = informeAdministracionRepository.findAllByFuente(fuente);

        Map<Integer, InformeAdministracion> mapaInformesExistentes = informesExistentes.stream()
                .collect(Collectors.toMap(
                        informe -> informe.getObjetivoComponente().getOidObjetivoComponente(),
                        informe -> informe));

        return informeDTO.getInformesAdministracion().stream()
                .map(item -> {
                    InformeAdministracion informeExistente = mapaInformesExistentes
                            .get(item.getOidObjetivoComponente());

                    if (informeExistente != null) {
                        informeExistente.setCalificacion(item.getCalificacion());
                        return informeExistente;
                    } else {
                        ObjetivoComponente objetivo = objetivoComponenteRepository
                                .findById(item.getOidObjetivoComponente())
                                .orElseThrow(() -> new RuntimeException(
                                        "ObjetivoComponente no encontrado con ID: " + item.getOidObjetivoComponente()));

                        InformeAdministracion nuevoInforme = new InformeAdministracion();
                        nuevoInforme.setFuente(fuente);
                        nuevoInforme.setObjetivoComponente(objetivo);
                        nuevoInforme.setCalificacion(item.getCalificacion());
                        return nuevoInforme;
                    }
                }).toList();
    }

    private void guardarArchivoAdministracion(Fuente fuente, MultipartFile documentoAdministracion) {
        try {
            if (documentoAdministracion != null && !documentoAdministracion.isEmpty()) {
                String timestamp = String.valueOf(System.currentTimeMillis());
                String prefijo = PREFIJO_INFORME_ADMINISTRACION + "-" + timestamp;
                String ruta = fuenteService.guardarDocumentoFuente(fuente, documentoAdministracion, prefijo);
                String nombreArchivo = Paths.get(ruta).getFileName().toString();

                fuente.setNombreDocumentoFuente(nombreArchivo);
                fuente.setRutaDocumentoFuente(ruta);
            }
        } catch (IOException e) {
            LOGGER.error("❌ Error al guardar documento de administración: {}", e.getMessage());
            throw new RuntimeException("Error al guardar el documento de administración", e);
        }
    }

    @Override
    public InformeAdministracion buscarPorId(Integer id) {
        return informeAdministracionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("InformeAdministracion no encontrado con ID: " + id));
    }

    @Override
    public Page<InformeAdministracion> listar(Pageable pageable) {
        return informeAdministracionRepository.findAll(pageable);
    }

    @Override
    public ApiResponse<Object> obtenerDetalleInformeAdministracion(Integer oidFuente) {
        try {
            LOGGER.info("🔍 Buscando Informe de Administración por Fuente con ID: {}", oidFuente);

            Fuente fuente = fuenteService.obtenerFuente(oidFuente);
            List<InformeAdministracion> informes = informeAdministracionRepository.findAllByFuente(fuente);

            Map<String, Object> resultado = construirResultadoInforme(fuente, informes);

            return new ApiResponse<>(200, "Informe de administración encontrado correctamente.", resultado);
        } catch (EntityNotFoundException e) {
            LOGGER.warn("⚠️ {}", e.getMessage());
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            LOGGER.error("❌ Error al buscar Informe de Administración por Fuente", e);
            return new ApiResponse<>(500, "Error inesperado al buscar el informe.", null);
        }
    }

    private Map<String, Object> construirResultadoInforme(Fuente fuente, List<InformeAdministracion> informes) {
        Map<String, Object> resultado = new LinkedHashMap<>();

        Map<String, Object> resumen = EvaluacionMapperUtil.construirInformacionFuente(
                fuente,
                fuente.getTipoCalificacion(),
                fuente.getObservacion(),
                fuente.getNombreDocumentoFuente());

        resultado.put("Fuente", resumen);
        resultado.put("informesAdministracion", construirInformes(informes));

        return resultado;
    }

    private List<Map<String, Object>> construirInformes(List<InformeAdministracion> informes) {
        return informes.stream().map(informe -> {
            Map<String, Object> informeMap = new HashMap<>();
            informeMap.put("oidObjetivoComponente", informe.getObjetivoComponente().getOidObjetivoComponente());
            informeMap.put("calificacion", informe.getCalificacion());
            return informeMap;
        }).collect(Collectors.toList());
    }
}
