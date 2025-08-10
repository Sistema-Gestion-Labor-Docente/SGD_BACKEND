package co.edu.unicauca.sgd.api.mapper;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.Materia;
import co.edu.unicauca.sgd.api.dto.materias.MateriaDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.MateriaDTOResponse;

@Component
public class MateriaMapper {

    /** SOLO campos simples. Las relaciones se setean en el Service. */
    public Materia convertToEntity(MateriaDTORequest dto) {
        Materia m = new Materia();
        m.setOidMateria(dto.getOidMateria());
        m.setCodigo(dto.getCodigo());
        m.setNombre(dto.getNombre());
        m.setSemestre(dto.getSemestre());
        m.setHorasSemana(dto.getHorasSemana());
        return m;
    }

    public void actualizarCamposBasicos(Materia existente, MateriaDTORequest dto) {
        existente.setOidMateria(dto.getOidMateria());
        existente.setCodigo(dto.getCodigo());
        existente.setNombre(dto.getNombre());
        existente.setSemestre(dto.getSemestre());
        existente.setHorasSemana(dto.getHorasSemana());
        // relaciones (Departamento/Plan) se manejan en el Service
    }

    public MateriaDTOResponse toResponse(Materia e) {
        MateriaDTOResponse dto = new MateriaDTOResponse();
        dto.setIdMateria(e.getIdMateria());
        dto.setOidMateria(e.getOidMateria());
        dto.setCodigo(e.getCodigo());
        dto.setNombre(e.getNombre());
        dto.setSemestre(e.getSemestre());
        dto.setHorasSemana(e.getHorasSemana());

        if (e.getDepartamento() != null) {
            dto.setOidDepartamento(e.getDepartamento().getOidDepartamento());
            dto.setNombreDepartamento(e.getDepartamento().getNombre());
        }
        if (e.getPlan() != null) {
            dto.setOidPlan(e.getPlan().getOidPlan());
            dto.setNumeroPlan(e.getPlan().getNumero());
        }

        dto.setFechaCreacion(e.getFechaCreacion());
        dto.setUsuarioCreacion(e.getUsuarioCreacion());
        dto.setFechaActualizacion(e.getFechaActualizacion());
        dto.setUsuarioActualizacion(e.getUsuarioActualizacion());
        return dto;
    }
}
