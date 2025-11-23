package co.edu.unicauca.sgd.api.mapper;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.Necesidad;
import co.edu.unicauca.sgd.api.dto.materias.MateriaDTOResponse;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTORequest;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTOResponse;

@Component
public class NecesidadMapper {

    public Necesidad toEntity(NecesidadDTORequest dto) {
        Necesidad entidad = new Necesidad();
        entidad.setGrupo(dto.getGrupo());
        entidad.setCupo(dto.getCupo());
        return entidad;
    }

    public void actualizarCampos(Necesidad existente, NecesidadDTORequest dto) {
        if (dto.getGrupo() != null) {
            existente.setGrupo(dto.getGrupo());
        }
        if (dto.getCupo() != null) {
            existente.setCupo(dto.getCupo());
        }
    }

    public NecesidadDTOResponse toResponse(Necesidad entidad) {
        NecesidadDTOResponse dto = new NecesidadDTOResponse();
        dto.setOidNecesidad(entidad.getOidNecesidad());
        dto.setGrupo(entidad.getGrupo());
        dto.setCupo(entidad.getCupo());
        dto.setEstado(entidad.getEstado());
        dto.setEstadoDescripcion(entidad.getEstado() != null ? entidad.getEstado().getValor() : null);

        if (entidad.getCalendario() != null) {
            dto.setOidCalendario(entidad.getCalendario().getOidcalendario());
            dto.setAnioCalendario(entidad.getCalendario().getAnioCalendario());
            dto.setNumeroCalendario(entidad.getCalendario().getNumeroCalendario());
        }

        if (entidad.getMateria() != null) {
            dto.setIdMateria(entidad.getMateria().getIdMateria());
            dto.setOidMateria(entidad.getMateria().getOidMateria());
            dto.setCodigoMateria(entidad.getMateria().getCodigo());
            dto.setNombreMateria(entidad.getMateria().getNombre());
            dto.setSemestreMateria(entidad.getMateria().getSemestre());

            MateriaDTOResponse materiaDto = new MateriaDTOResponse();
            materiaDto.setIdMateria(entidad.getMateria().getIdMateria());
            materiaDto.setOidMateria(entidad.getMateria().getOidMateria());
            materiaDto.setCodigo(entidad.getMateria().getCodigo());
            materiaDto.setNombre(entidad.getMateria().getNombre());
            materiaDto.setSemestre(entidad.getMateria().getSemestre());
            materiaDto.setHorasSemana(entidad.getMateria().getHorasSemana());

            if (entidad.getMateria().getDepartamento() != null) {
                materiaDto.setOidDepartamento(entidad.getMateria().getDepartamento().getOidDepartamento());
                materiaDto.setNombreDepartamento(entidad.getMateria().getDepartamento().getNombre());
            }
            if (entidad.getMateria().getPlan() != null) {
                materiaDto.setOidPlan(entidad.getMateria().getPlan().getOidPlan());
                materiaDto.setNumeroPlan(entidad.getMateria().getPlan().getNumero());
            }
            if (entidad.getMateria().getCorrequisito() != null) {
                materiaDto.setIdCorrequisito(entidad.getMateria().getCorrequisito().getIdMateria());
                materiaDto.setOidCorrequisito(entidad.getMateria().getCorrequisito().getOidMateria());
                materiaDto.setNombreCorrequisito(entidad.getMateria().getCorrequisito().getNombre());
            }

            materiaDto.setFechaCreacion(entidad.getMateria().getFechaCreacion());
            materiaDto.setUsuarioCreacion(entidad.getMateria().getUsuarioCreacion());
            materiaDto.setFechaActualizacion(entidad.getMateria().getFechaActualizacion());
            materiaDto.setUsuarioActualizacion(entidad.getMateria().getUsuarioActualizacion());

            dto.setMateria(materiaDto);
        }

        if (entidad.getCorrequisitoNecesidad() != null) {
            dto.setCorrequisitoOidNecesidad(entidad.getCorrequisitoNecesidad().getOidNecesidad());
            if (entidad.getCorrequisitoNecesidad().getMateria() != null) {
                dto.setCorrequisitoNombreMateria(entidad.getCorrequisitoNecesidad().getMateria().getNombre());
            }
        }

        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setUsuarioCreacion(entidad.getUsuarioCreacion());
        dto.setFechaActualizacion(entidad.getFechaActualizacion());
        dto.setUsuarioActualizacion(entidad.getUsuarioActualizacion());
        return dto;
    }
}
