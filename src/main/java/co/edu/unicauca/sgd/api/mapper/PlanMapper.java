package co.edu.unicauca.sgd.api.mapper;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.Plan;
import co.edu.unicauca.sgd.api.domain.Programa;
import co.edu.unicauca.sgd.api.dto.materias.PlanDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.PlanDTOResponse;

@Component
public class PlanMapper {

    public Plan convertToEntity(PlanDTORequest dto) {
        Plan plan = new Plan();
        plan.setNumero(dto.getNumero());
        plan.setEstado(dto.getEstado());
        plan.setFechaAprobacion(dto.getFechaAprobacion());
        plan.setAcuerdo(dto.getAcuerdo());

        if (dto.getOidPrograma() != null) {
            Programa prog = new Programa();
            prog.setOidPrograma(dto.getOidPrograma());
            plan.setPrograma(prog);
        }
        return plan;
    }

    public void actualizarCamposBasicos(Plan existente, PlanDTORequest dto) {
        existente.setNumero(dto.getNumero());
        existente.setEstado(dto.getEstado());
        existente.setFechaAprobacion(dto.getFechaAprobacion());
        existente.setAcuerdo(dto.getAcuerdo());

        if (dto.getOidPrograma() != null) {
            Programa prog = new Programa();
            prog.setOidPrograma(dto.getOidPrograma());
            existente.setPrograma(prog);
        }
    }

    public PlanDTOResponse toResponse(Plan entidad) {
        PlanDTOResponse dto = new PlanDTOResponse();
        dto.setOidPlan(entidad.getOidPlan());
        dto.setNumero(entidad.getNumero());
        dto.setEstado(entidad.getEstado());
        dto.setFechaAprobacion(entidad.getFechaAprobacion());
        dto.setAcuerdo(entidad.getAcuerdo());

        if (entidad.getPrograma() != null) {
            dto.setOidPrograma(entidad.getPrograma().getOidPrograma());
            dto.setNombrePrograma(entidad.getPrograma().getNombre());
        }

        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setUsuarioCreacion(entidad.getUsuarioCreacion());
        dto.setFechaActualizacion(entidad.getFechaActualizacion());
        dto.setUsuarioActualizacion(entidad.getUsuarioActualizacion());
        return dto;
    }
}
