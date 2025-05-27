package co.edu.unicauca.sgd.api.service.actividad.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.Fuente;
import co.edu.unicauca.sgd.api.domain.LaborDocente;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.dto.AtributoDTO;
import co.edu.unicauca.sgd.api.dto.FuenteDTO;
import co.edu.unicauca.sgd.api.dto.RolDTO;
import co.edu.unicauca.sgd.api.dto.UsuarioDTO;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadDTOEvaluador;
import co.edu.unicauca.sgd.api.repository.LaborDocenteRepository;
import co.edu.unicauca.sgd.api.service.EavAtributoService;
import co.edu.unicauca.sgd.api.service.actividad.ActividadDTOService;
import co.edu.unicauca.sgd.api.service.fuente.FuenteDTOService;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de conversión entre entidades Actividad y sus
 * respectivos DTOs.
 */
@Service
public class ActividadDTOServiceImpl implements ActividadDTOService {

    private static final String DEFAULT_NAME = "N/A";

    private final FuenteDTOService fuenteDTOService;
    private final EavAtributoService eavAtributoService;

    @Autowired
    private LaborDocenteRepository laborDocenteRepository;

    public ActividadDTOServiceImpl(FuenteDTOService fuenteDTOService, EavAtributoService eavAtributoService) {
        this.fuenteDTOService = fuenteDTOService;
        this.eavAtributoService = eavAtributoService;
    }

    @Override
    public ActividadBaseDTO buildActividadBaseDTO(Actividad actividad) {
        UsuarioDTO evaluadorDTO = convertToUsuarioDTO(actividad.getProceso().getEvaluador());

        // Obtener los atributos dinámicos en formato AtributoDTO
        List<AtributoDTO> atributos = eavAtributoService.obtenerAtributosPorActividad(actividad);

        // Verifica si hay LaborDocente en BD
        Optional<LaborDocente> laborDocenteOpt = laborDocenteRepository.findByUsuarioAndPeriodoAcademico(
                actividad.getProceso().getEvaluado(),
                actividad.getProceso().getOidPeriodoAcademico());

        // Determinar si tiene relación con LaborDocente
        Integer idLabor = actividad.getIdLaborDocente();
        Boolean esLabor = (idLabor != null);
        Boolean archivoLaborDocente = laborDocenteOpt.isPresent();

        return new ActividadBaseDTO(
                actividad.getOidActividad(),
                actividad.getTipoActividad(),
                actividad.getProceso().getOidProceso(),
                actividad.getEstadoActividad().getOidEstadoActividad(),
                actividad.getNombreActividad(),
                actividad.getHoras(),
                actividad.getSemanas(),
                actividad.getInformeEjecutivo(),
                actividad.getFechaCreacion(),
                actividad.getFechaActualizacion(),
                fuenteDTOService.convertirListaFuenteADTO(actividad.getFuentes()),
                atributos,
                evaluadorDTO,
                actividad.getProceso().getEvaluado().getOidUsuario(),
                actividad.getProceso().getEvaluador().getOidUsuario(),
                actividad.getIdLaborDocente(),
                esLabor,
                archivoLaborDocente);
    }

    @Override
    public ActividadDTOEvaluador convertToDTOWithEvaluado(Actividad actividad) {
        UsuarioDTO evaluadoDTO = convertToUsuarioDTO(actividad.getProceso().getEvaluado());
        List<FuenteDTO> fuenteDTOs = actividad.getFuentes().stream()
        .sorted(Comparator.comparing(Fuente::getTipoFuente))
        .map(this::convertFuenteToDTO)
        .collect(Collectors.toList());

        return buildActividadDTOEvaluador(actividad, fuenteDTOs, evaluadoDTO);
    }

    @Override
    public ActividadDTOEvaluador convertToDTOWithEvaluado(Actividad actividad, String tipoFuente, String estadoFuente) {
        UsuarioDTO evaluadoDTO = convertToUsuarioDTO(actividad.getProceso().getEvaluado());
        
        List<FuenteDTO> fuenteDTOs = actividad.getFuentes().stream()
                .filter(fuente -> {
                    boolean tipoMatch = (tipoFuente == null || fuente.getTipoFuente().equalsIgnoreCase(tipoFuente));
                    boolean estadoMatch = (estadoFuente == null
                            || fuente.getEstadoFuente().getNombreEstado().equalsIgnoreCase(estadoFuente));
                    return tipoMatch && estadoMatch;
                })
                .sorted(Comparator.comparing(Fuente::getTipoFuente))
                .map(this::convertFuenteToDTO)
                .collect(Collectors.toList());
    
        return buildActividadDTOEvaluador(actividad, fuenteDTOs, evaluadoDTO);
    }    

    private ActividadDTOEvaluador buildActividadDTOEvaluador(Actividad actividad, List<FuenteDTO> fuenteDTOs,
            UsuarioDTO evaluadoDTO) {
        return new ActividadDTOEvaluador(
                actividad.getOidActividad(),
                actividad.getNombreActividad(),
                actividad.getHoras(),
                actividad.getSemanas(),
                actividad.getEstadoActividad(),
                actividad.getInformeEjecutivo(),
                actividad.getFechaCreacion(),
                actividad.getFechaActualizacion(),
                actividad.getTipoActividad(),
                fuenteDTOs,
                evaluadoDTO);
    }

    @Override
    public UsuarioDTO convertToUsuarioDTO(Usuario usuario) {
        List<RolDTO> rolDTOList = usuario.getRoles().stream()
                .map(rol -> new RolDTO(rol.getNombre()))
                .collect(Collectors.toList());

        String nombres = usuario.getNombres() != null ? usuario.getNombres() : DEFAULT_NAME;
        String apellidos = usuario.getApellidos() != null ? usuario.getApellidos() : DEFAULT_NAME;

        return new UsuarioDTO(
                usuario.getOidUsuario(),
                usuario.getIdentificacion(),
                nombres,
                apellidos,
                rolDTOList);
    }

    @Override
    public FuenteDTO convertFuenteToDTO(Fuente fuente) {
        return new FuenteDTO(
                fuente.getOidFuente(),
                fuente.getTipoFuente(),
                fuente.getCalificacion(),
                fuente.getTipoCalificacion(),
                fuente.getNombreDocumentoFuente(),
                fuente.getNombreDocumentoInforme(),
                fuente.getObservacion(),
                fuente.getFechaCreacion(),
                fuente.getFechaActualizacion(),
                fuente.getEstadoFuente().getNombreEstado());
    }
}
