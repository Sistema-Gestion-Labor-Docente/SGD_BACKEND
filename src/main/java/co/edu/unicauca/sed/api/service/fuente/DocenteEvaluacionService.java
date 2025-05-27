package co.edu.unicauca.sed.api.service.fuente;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.Proceso;
import co.edu.unicauca.sed.api.domain.Usuario;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.DocenteEvaluacionDTO;
import co.edu.unicauca.sed.api.mapper.DocenteEvaluacionMapper;
import co.edu.unicauca.sed.api.repository.ProcesoRepository;
import co.edu.unicauca.sed.api.repository.UsuarioRepository;
import co.edu.unicauca.sed.api.service.documento.ExcelService;
import co.edu.unicauca.sed.api.service.periodo_academico.PeriodoAcademicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocenteEvaluacionService {

    @Autowired
    private ProcesoRepository procesoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PeriodoAcademicoService periodoAcademicoService;

    @Autowired
    private DocenteEvaluacionMapper docenteEvaluacionMapper;

    @Autowired
    private ExcelService excelService;

    /**
     * Obtener evaluaciones de docentes con filtros opcionales.
     *
     * @param idEvaluado         ID del docente (opcional).
     * @param idPeriodoAcademico ID del período académico (opcional).
     * @param departamento       Departamento del docente (opcional).
     * @return Lista de evaluaciones de docentes.
     */
    public ApiResponse<Page<DocenteEvaluacionDTO>> obtenerEvaluacionDocentes(Integer idEvaluado, Integer idPeriodoAcademico, String departamento, String nombre, 
        String tipoContrato, String identificacion, Pageable pageable) {

        try {
            Integer periodoFinal = (idPeriodoAcademico != null) ? idPeriodoAcademico : periodoAcademicoService.obtenerIdPeriodoAcademicoActivo();

            List<Usuario> evaluados = cargarUsuariosEvaluados(idEvaluado, periodoFinal);
            evaluados = aplicarFiltros(evaluados, departamento, nombre, tipoContrato, identificacion, idEvaluado);

            List<DocenteEvaluacionDTO> evaluacionDTOs = mapearADocenteEvaluacionDTO(evaluados, periodoFinal);

            evaluacionDTOs.sort(Comparator.comparing(DocenteEvaluacionDTO::getPorcentajeEvaluacionCompletado,Comparator.nullsLast(Comparator.reverseOrder())));

            Page<DocenteEvaluacionDTO> pageResult = paginarResultados(evaluacionDTOs, pageable);

            return new ApiResponse<>(200, "Evaluaciones obtenidas correctamente.", pageResult);

        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(400, "Error en los parámetros proporcionados: " + e.getMessage(), Page.empty());

        } catch (Exception e) {
            return new ApiResponse<>(500, "Error inesperado al obtener evaluaciones de docentes: " + e.getMessage(), Page.empty());
        }
    }

    private List<Usuario> cargarUsuariosEvaluados(Integer idEvaluado, Integer idPeriodoAcademico) {
        return obtenerUsuariosEvaluados(idEvaluado, idPeriodoAcademico);
    }

    private List<Usuario> aplicarFiltros(List<Usuario> evaluados, String departamento, String nombre,
            String tipoContrato, String identificacion, Integer idEvaluado) {
        if (departamento != null) {
            evaluados = filtrarPorDepartamento(evaluados, departamento);
        }

        if (nombre != null && !nombre.isBlank()) {
            evaluados = evaluados.stream()
                .filter(usuario -> {
                    String nombreCompleto = (usuario.getNombres() != null ? usuario.getNombres() : "") + " " +
                        (usuario.getApellidos() != null ? usuario.getApellidos() : "");
                    return nombreCompleto.trim().toLowerCase().contains(nombre.toLowerCase());
                }).collect(Collectors.toList());
        }

        if (tipoContrato != null && !tipoContrato.isBlank()) {
            evaluados = evaluados.stream()
                .filter(usuario -> (usuario.getUsuarioDetalle() != null)
                    && (usuario.getUsuarioDetalle().getContratacion() != null)
                    && usuario.getUsuarioDetalle().getContratacion().toLowerCase().contains(tipoContrato.toLowerCase())).collect(Collectors.toList());
        }

        if (identificacion != null && !identificacion.isBlank()) {
            evaluados = evaluados.stream()
                .filter(usuario -> (usuario.getIdentificacion() != null) && usuario.getIdentificacion().toLowerCase().contains(identificacion.toLowerCase())).collect(Collectors.toList());
        }

        if (evaluados.isEmpty() && idEvaluado == null) {
            throw new IllegalArgumentException("No se encontraron docentes con los filtros proporcionados.");
        }

        return evaluados;
    }

    private Page<DocenteEvaluacionDTO> paginarResultados(List<DocenteEvaluacionDTO> evaluacionDTOs, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), evaluacionDTOs.size());
        List<DocenteEvaluacionDTO> paginatedList = evaluacionDTOs.subList(start, end);

        return new PageImpl<>(paginatedList, pageable, evaluacionDTOs.size());
    }

    private List<DocenteEvaluacionDTO> mapearADocenteEvaluacionDTO(List<Usuario> evaluados, Integer periodoFinal) {
        return evaluados.stream()
            .map(evaluado -> {
                List<Actividad> actividades = procesoRepository
                    .findByEvaluado_OidUsuarioAndOidPeriodoAcademico_OidPeriodoAcademico( evaluado.getOidUsuario(), periodoFinal)
                    .stream().flatMap(proceso -> proceso.getActividades().stream()).collect(Collectors.toList());
                return docenteEvaluacionMapper.toDto(evaluado, actividades);
            }).collect(Collectors.toList());
    }


    /**
     * Filtra la lista de evaluados según el departamento proporcionado.
     *
     * @param evaluados    Lista de evaluados.
     * @param departamento Departamento para filtrar.
     * @return Lista de evaluados filtrados por departamento.
     */
    private List<Usuario> filtrarPorDepartamento(List<Usuario> evaluados, String departamento) {
        return evaluados.stream()
                .filter(evaluado -> evaluado.getUsuarioDetalle() != null &&
                        departamento.equalsIgnoreCase(evaluado.getUsuarioDetalle().getDepartamento()))
                .collect(Collectors.toList());
    }

    /**
     * Obtener la lista de usuarios evaluados según los filtros de ID y período académico.
     *
     * @param idEvaluado         ID del docente (opcional).
     * @param idPeriodoAcademico ID del período académico.
     * @return Lista de usuarios evaluados.
     */
    private List<Usuario> obtenerUsuariosEvaluados(Integer idEvaluado, Integer idPeriodoAcademico) {

        final int ROL_DOCENTE_ID = 1;

        if (idEvaluado != null) {
            Usuario usuario = usuarioRepository.findById(idEvaluado).orElseThrow(() -> new IllegalArgumentException("Evaluado no encontrado."));

            boolean esDocente = usuario.getRoles().stream().anyMatch(rol -> rol.getOid().equals(ROL_DOCENTE_ID));

            if (!esDocente) {
                throw new IllegalArgumentException("El usuario no tiene rol docente.");
            }

            return List.of(usuario);
        }

        return procesoRepository.findByOidPeriodoAcademico_OidPeriodoAcademico(idPeriodoAcademico)
                .stream().map(Proceso::getEvaluado)
                .filter(usuario -> usuario.getRoles().stream().anyMatch(rol -> rol.getOid().equals(ROL_DOCENTE_ID)))
                .distinct().collect(Collectors.toList());
    }

    public ByteArrayResource exportarEvaluacionDocenteExcel(
            Integer idEvaluado, Integer idPeriodoAcademico, String departamento, String nombre, String tipoContrato, String identificacion) throws IOException {

        // Determinar el periodo activo si no se proporciona
        Integer periodoFinal = (idPeriodoAcademico != null) ? idPeriodoAcademico : periodoAcademicoService.obtenerIdPeriodoAcademicoActivo();

        // Obtener y filtrar usuarios evaluados
        List<Usuario> evaluados = cargarUsuariosEvaluados(idEvaluado, periodoFinal);
        evaluados = aplicarFiltros(evaluados, departamento, nombre, tipoContrato, identificacion, idEvaluado);

        // Mapear a DTO y ordenar por % completado
        List<DocenteEvaluacionDTO> data = mapearADocenteEvaluacionDTO(evaluados, periodoFinal);
        data.sort(Comparator.comparing(DocenteEvaluacionDTO::getPorcentajeEvaluacionCompletado, Comparator.nullsLast(Comparator.reverseOrder())));

        // Definir los headers del Excel
        String[] headers = { "Nombre", "Identificación", "Tipo Contrato", "% Completado", "Estado", "Total Acumulado" };

        // Generar Excel
        ByteArrayOutputStream excelStream = excelService.generarExcelEvaluacionDocente(data, headers);

        return new ByteArrayResource(excelStream.toByteArray());
    }
}
