package co.edu.unicauca.sgd.api.service.materias.impl;

import co.edu.unicauca.sgd.api.domain.*;
import co.edu.unicauca.sgd.api.domain.materias.MateriaCorrequisito;
import co.edu.unicauca.sgd.api.domain.materias.MateriaCorrequisitoId;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.CorrequisitoListaResponse;
import co.edu.unicauca.sgd.api.dto.materias.CorrequisitoPairDTO;
import co.edu.unicauca.sgd.api.mapper.CorrequisitoMapper;
import co.edu.unicauca.sgd.api.repository.MateriaCorrequisitoRepository;
import co.edu.unicauca.sgd.api.repository.MateriaRepository;
import co.edu.unicauca.sgd.api.service.materias.CorrequisitoService;
import jakarta.transaction.Transactional;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CorrequisitoServiceImpl implements CorrequisitoService {

    private static final Logger log = LoggerFactory.getLogger(CorrequisitoServiceImpl.class);

    private MateriaRepository materiaRepository;
    
    private MateriaCorrequisitoRepository correpo;
    
    private CorrequisitoMapper mapper;

    public CorrequisitoServiceImpl(
            @Autowired MateriaRepository materiaRepository,
            @Autowired MateriaCorrequisitoRepository correpo,
            @Autowired CorrequisitoMapper mapper) {
        this.materiaRepository = materiaRepository;
        this.correpo = correpo;
        this.mapper = mapper;
    }

    @Override
    public ApiResponse<CorrequisitoListaResponse> listar(Integer idMateria) {
        try {
            validarExisteMateria(idMateria);
            List<Integer> ids = correpo.findCorrequisitosIds(idMateria);
            return new ApiResponse<>(200, "Co-requisitos obtenidos correctamente.",
                    mapper.toListResponse(idMateria, ids));
        } catch (RuntimeException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al listar co-requisitos: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> agregar(CorrequisitoPairDTO pair) {
        try {
            Pair p = normalizarYValidar(pair.getIdMateria1(), pair.getIdMateria2());

            Materia a = materiaRepository.findById(p.a).orElseThrow();
            Materia b = materiaRepository.findById(p.b).orElseThrow();

            // (Opcional) si deseas forzar que sean del mismo plan, descomenta:
            if (!Objects.equals(a.getPlan().getOidPlan(), b.getPlan().getOidPlan())) {
                throw new RuntimeException("Las materias no pertenecen al mismo plan.");
            }

            MateriaCorrequisitoId id = new MateriaCorrequisitoId(p.a, p.b);
            if (correpo.existsById(id)) {
                return new ApiResponse<>(200, "El co-requisito ya existía. No se realizó ningún cambio.", null);
            }

            correpo.save(new MateriaCorrequisito(id, a, b));
            log.info("Creado co-requisito {} <-> {}", p.a, p.b);
            return new ApiResponse<>(200, "Co-requisito creado correctamente.", null);
        } catch (RuntimeException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al crear co-requisito: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminar(CorrequisitoPairDTO pair) {
        try {
            Pair p = normalizarYValidar(pair.getIdMateria1(), pair.getIdMateria2());

            MateriaCorrequisitoId id = new MateriaCorrequisitoId(p.a, p.b);
            if (!correpo.existsById(id)) {
                return new ApiResponse<>(404, "El co-requisito no existe.", null);
            }
            correpo.deleteById(id);
            log.info("Eliminado co-requisito {} <-> {}", p.a, p.b);
            return new ApiResponse<>(200, "Co-requisito eliminado correctamente.", null);
        } catch (RuntimeException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar co-requisito: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<CorrequisitoListaResponse> reemplazar(Integer idMateria, List<Integer> nuevos) {
        try {
            validarExisteMateria(idMateria);

            // Normalizar: quitar self, nulls y duplicados
            Set<Integer> limpios = new LinkedHashSet<>();
            for (Integer m : nuevos == null ? List.<Integer>of() : nuevos) {
                if (m == null) continue;
                if (Objects.equals(m, idMateria)) continue;
                limpios.add(m);
            }

            // Borrar actuales y crear los nuevos
            correpo.deleteAllForMateria(idMateria);

            for (Integer otro : limpios) {
                Pair p = normalizarYValidar(idMateria, otro);
                Materia a = materiaRepository.findById(p.a).orElseThrow();
                Materia b = materiaRepository.findById(p.b).orElseThrow();

                // (Opcional) misma validación de plan:
                if (!Objects.equals(a.getPlan().getOidPlan(), b.getPlan().getOidPlan())) {
                    throw new RuntimeException("Las materias no pertenecen al mismo plan.");
                }

                correpo.save(new MateriaCorrequisito(new MateriaCorrequisitoId(p.a, p.b), a, b));
            }

            List<Integer> result = correpo.findCorrequisitosIds(idMateria);
            return new ApiResponse<>(200, "Co-requisitos reemplazados correctamente.",
                    mapper.toListResponse(idMateria, result));
        } catch (RuntimeException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al reemplazar co-requisitos: " + e.getMessage(), null);
        }
    }

    /* --------- helpers --------- */

    private void validarExisteMateria(Integer oid) {
        materiaRepository.findById(oid)
                .orElseThrow(() -> new RuntimeException("Materia no encontrada con ID: " + oid));
    }

    private Pair normalizarYValidar(Integer x, Integer y) {
        if (x == null || y == null) throw new RuntimeException("Debe enviar dos IDs de materia.");
        if (Objects.equals(x, y)) throw new RuntimeException("Una materia no puede ser co-requisito de sí misma.");
        int a = Math.min(x, y);
        int b = Math.max(x, y);
        // Validar existencia temprana (mensajes más claros)
        validarExisteMateria(a);
        validarExisteMateria(b);
        return new Pair(a, b);
    }

    private record Pair(int a, int b) {}
}
