package co.edu.unicauca.sgd.api.service.materias.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.Materia;
import co.edu.unicauca.sgd.api.domain.Plan;
import co.edu.unicauca.sgd.api.exception.materias.PlanDocumentoValidationException;
import co.edu.unicauca.sgd.api.exception.materias.PlanNotFoundException;
import co.edu.unicauca.sgd.api.repository.DepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.MateriaRepository;
import co.edu.unicauca.sgd.api.repository.PlanRepository;
import co.edu.unicauca.sgd.api.service.materias.PlanDocumentosService;

@Service
public class PlanDocumentosServiceImpl implements PlanDocumentosService {

    private static final Logger logger = LoggerFactory.getLogger(PlanDocumentosServiceImpl.class);

    private DepartamentoRepository departamentoRepository;

    private MateriaRepository materiaRepository;

    private PlanRepository planRepository;

    public PlanDocumentosServiceImpl(DepartamentoRepository departamentoRepository,
                                     MateriaRepository materiaRepository,
                                     PlanRepository planRepository) {
        this.departamentoRepository = departamentoRepository;
        this.materiaRepository = materiaRepository;
        this.planRepository = planRepository;
    }

    @Override
    public ByteArrayOutputStream generarFormatoAdicion(Integer oidPlan) throws IOException {
        logger.info("Generando formato de adición para oidPlan: {}", oidPlan);

        try (
            InputStream templateStream = getClass().getClassLoader().getResourceAsStream("formatos/Formato_Adicion.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(templateStream)
        ) {
            // 1. Insertar hoja oculta con departamentos
            agregarHojaDepartamentos(workbook);

            // 2. Insertar el Named Range para la validación de datos
            crearRangoNombreDepartamentos(workbook);

            // 3. Insertar validación y valores en hoja principal
            configurarValidacionYDefault(workbook);

            // 4. Precargar datos de las materias del plan en el formato
            precargarMateriasEnFormato(workbook, oidPlan);

            // 5. Ocultar valor oidPlan en hoja oculta
            ocultarOidPlan(workbook, oidPlan);

            // 6. Escribir y retornar archivo
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            logger.info("Formato de adición generado exitosamente para oidPlan: {}", oidPlan);
            return outputStream;

        } catch (Exception e) {
            logger.error("Error generando formato de adición para oidPlan: {}", oidPlan, e);
            throw new IOException("Error generando formato de adición", e);
        }
    }

    @Override
    public void cargarMateriasDesdeExcel(InputStream excelStream, Integer oidPlan) throws IOException {
        Plan plan = planRepository.findById(oidPlan)
                .orElseThrow(() -> new PlanNotFoundException("No existe plan con OID " + oidPlan));

        try (XSSFWorkbook workbook = new XSSFWorkbook(excelStream)) {
            validarOidPlanOculto(workbook, oidPlan);

            XSSFSheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getLastRowNum();

            // Primer paso: cargar/actualizar materias sin correquisito
            Map<String, Materia> materiasPorOid = procesarMateriasBasicas(sheet, rowCount, plan);

            // Segundo paso: procesar correquisitos
            asignarCorrequisitos(sheet, rowCount, materiasPorOid);
        }
    }

    // --------------- Métodos privados auxiliares ---------------- //

    /**
     * Agrega una hoja oculta con la lista de departamentos.
     */
    private void agregarHojaDepartamentos(XSSFWorkbook workbook) {
        List<Departamento> departamentos = departamentoRepository.findAll();

        XSSFSheet hojaDeptos = workbook.createSheet("DEPARTAMENTOS_LISTA");
        hojaDeptos.createRow(0).createCell(0).setCellValue("NINGUNO");
        for (int i = 0; i < departamentos.size(); i++) {
            hojaDeptos.createRow(i + 1).createCell(0).setCellValue(departamentos.get(i).getNombre());
            hojaDeptos.createRow(i + 1).createCell(1).setCellValue(departamentos.get(i).getOidDepartamento());
        }
        workbook.setSheetHidden(workbook.getSheetIndex(hojaDeptos), true);
    }

    /**
     * Crea un rango con nombre (Named Range) para la lista de departamentos.
     */
    private void crearRangoNombreDepartamentos(XSSFWorkbook workbook) {
        int totalDepartamentos = departamentoRepository.findAll().size();
        XSSFName namedRange = workbook.createName();
        namedRange.setNameName("DEPTOS_LISTA");
        String reference = "DEPARTAMENTOS_LISTA!$A$1:$A$" + (totalDepartamentos + 1);
        namedRange.setRefersToFormula(reference);
    }

    /**
     * Configura la validación de datos en la hoja principal, y valores por defecto.
     */
    private void configurarValidacionYDefault(XSSFWorkbook workbook) {
        XSSFSheet hoja = workbook.getSheetAt(0); // principal
        DataValidationHelper helper = hoja.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createFormulaListConstraint("=DEPTOS_LISTA");
        CellRangeAddressList addressList = new CellRangeAddressList(1, 99, 6, 6); // columna G (índice 6), filas 2-100
        DataValidation validation = helper.createValidation(constraint, addressList);

        // Configuración de la validación
        validation.setShowErrorBox(true);
        validation.setShowPromptBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("Error", "Por favor seleccione un departamento válido de la lista.");
        validation.createPromptBox("Departamento", "Seleccione un departamento de la lista desplegable.");

        hoja.addValidationData(validation);

        // Establece valor por defecto "NINGUNO"
        for (int fila = 1; fila <= 99; fila++) {
            Row row = hoja.getRow(fila);
            if (row == null) row = hoja.createRow(fila);
            Cell cell = row.getCell(6);
            if (cell == null) cell = row.createCell(6);
            cell.setCellValue("NINGUNO");
        }
    }

    /**
     * Inserta una hoja oculta para guardar el oidPlan.
     */
    private void ocultarOidPlan(XSSFWorkbook workbook, Integer oidPlan) {
        XSSFSheet hiddenSheet = workbook.createSheet("OIDPLAN_OCULTO");
        hiddenSheet.createRow(0).createCell(0).setCellValue(oidPlan);
        workbook.setSheetHidden(workbook.getSheetIndex(hiddenSheet), true);
    }

    /**
     * Precarga las materias del plan en el formato Excel.
     */
    private void precargarMateriasEnFormato(XSSFWorkbook workbook, Integer oidPlan) {
        // Consigue la lista de materias del plan
        List<Materia> materias = materiaRepository.findAllByPlanOidPlan(oidPlan)
            .stream()
            .sorted(Comparator.comparing(Materia::getSemestre, Comparator.nullsLast(Integer::compareTo)))
            .toList();
        XSSFSheet hoja = workbook.getSheetAt(0);

        int rowIdx = 1; // Fila 1 es la primera después del encabezado
        for (Materia materia : materias) {
            Row row = hoja.getRow(rowIdx);
            if (row == null) row = hoja.createRow(rowIdx);

            // OIDMATERIA (A)
            Cell cellA = row.getCell(0) == null ? row.createCell(0) : row.getCell(0);
            cellA.setCellValue(materia.getOidMateria());

            // CODIGO (B)
            Cell cellB = row.getCell(1) == null ? row.createCell(1) : row.getCell(1);
            cellB.setCellValue(materia.getCodigo());

            // NOMBRE (C)
            Cell cellC = row.getCell(2) == null ? row.createCell(2) : row.getCell(2);
            cellC.setCellValue(materia.getNombre());

            // SEMESTRE (D)
            Cell cellD = row.getCell(3) == null ? row.createCell(3) : row.getCell(3);
            if (materia.getSemestre() != null)
                cellD.setCellValue(materia.getSemestre());

            // HORAS SEMANA (E)
            Cell cellE = row.getCell(4) == null ? row.createCell(4) : row.getCell(4);
            if (materia.getHorasSemana() != null)
                cellE.setCellValue(materia.getHorasSemana());

            // OID CORREQUISITO (F)
            Cell cellF = row.getCell(5) == null ? row.createCell(5) : row.getCell(5);
            if (materia.getCorrequisito() != null)
                cellF.setCellValue(materia.getCorrequisito().getOidMateria());

            // DEPARTAMENTO OFERTANTE (G)
            Cell cellG = row.getCell(6) == null ? row.createCell(6) : row.getCell(6);
            if (materia.getDepartamento() != null)
                cellG.setCellValue(materia.getDepartamento().getNombre());
            else
                cellG.setCellValue("NINGUNO"); // o "" si prefieres vacío

            rowIdx++;
        }
    }



    /**
     * Obtiene el valor de una celda como String, manejando tipos numéricos y nulos.
     */
    private String getCellString(Row row, int idx) {
        Cell cell = row.getCell(idx);
        return cell == null ? null : cell.getCellType() == CellType.NUMERIC
                ? String.valueOf((int) cell.getNumericCellValue())
                : cell.getStringCellValue().trim();
    }

    /**
     * Obtiene el valor de una celda como Integer, manejando tipos numéricos, cadenas y nulos.
     */
    private Integer getCellInteger(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) return (int) cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue().trim();
            return value.isEmpty() ? null : Integer.valueOf(value);
        }
        return null;
    }

    /**
     * Verifica si una fila está vacía (todas las celdas son nulas o están en blanco).
     */
    private boolean isRowEmpty(Row row) {
        for (int c = 0; c < 7; c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellString(row, c).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Valida que el OID del plan en la hoja oculta coincida con el proporcionado.
     */
    private void validarOidPlanOculto(XSSFWorkbook workbook, Integer oidPlan) {
        XSSFSheet oidSheet = workbook.getSheet("OIDPLAN_OCULTO");
        if (oidSheet == null || oidSheet.getRow(0) == null || oidSheet.getRow(0).getCell(0) == null) {
            throw new PlanDocumentoValidationException("No se encontró el OID del plan oculto en el archivo.");
        }
        int oidPlanOculto = (int) oidSheet.getRow(0).getCell(0).getNumericCellValue();
        if (!oidPlan.equals(oidPlanOculto)) {
            throw new PlanDocumentoValidationException("El OID del plan proporcionado (" + oidPlan + ") no coincide con el del archivo (" + oidPlanOculto + ").");
        }
    }

    /**
     * Procesa las materias básicas (sin correquisito) y las guarda/actualiza en la base de datos.
     * Retorna un mapa de OID de materia a la entidad Materia creada/actualizada.
     */
    private Map<String, Materia> procesarMateriasBasicas(XSSFSheet sheet, int rowCount, Plan plan) {
        Map<String, Materia> materiasPorOid = new HashMap<>();

        for (int i = 1; i <= rowCount; i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) continue;
            if (filaIncompleta(row)) continue;

            String oidMateria = getCellString(row, 0);
            String codigo = getCellString(row, 1);
            String nombre = getCellString(row, 2);

            Optional<Materia> materiaExistente = materiaRepository.findFirstByOidMateriaIgnoreCaseOrCodigoIgnoreCaseOrNombreIgnoreCase(oidMateria, codigo, nombre);

            Materia materia = materiaExistente.orElseGet(Materia::new);
            if (!materiaExistente.isPresent()) {
                materia.setPlan(plan);
            }

            materia.setOidMateria(oidMateria);
            materia.setCodigo(codigo);
            materia.setNombre(nombre);
            materia.setSemestre(getCellInteger(row, 3));
            materia.setHorasSemana(getCellInteger(row, 4));

            String departamentoNombre = getCellString(row, 6);
            Optional<Departamento> departamento = departamentoRepository.findByNombre(departamentoNombre);
            materia.setDepartamento(departamento.orElse(null));

            // No se asigna correquisito aquí
            Materia guardada = materiaRepository.save(materia);
            materiasPorOid.put(oidMateria, guardada);
        }
        return materiasPorOid;
    }

    /**
     * Asigna los correquisitos a las materias ya creadas/actualizadas.
     */
    private void asignarCorrequisitos(XSSFSheet sheet, int rowCount, Map<String, Materia> materiasPorOid) {
        for (int i = 1; i <= rowCount; i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) continue;

            String oidMateria = getCellString(row, 0);
            Materia materia = materiasPorOid.get(oidMateria);
            if (materia == null) continue;

            String oidCorrequisito = getCellString(row, 5); // columna F
            if (oidCorrequisito != null && !oidCorrequisito.isEmpty()) {
                Materia correquisito = materiasPorOid.get(oidCorrequisito); // primero busca en el archivo
                if (correquisito == null) {
                    correquisito = materiaRepository.findByOidMateria(oidCorrequisito).orElse(null); // luego en la BD
                }
                materia.setCorrequisito(correquisito); // Puede quedar nulo si no existe
                materiaRepository.save(materia);
            }
        }
    }

    /**
     * Verifica si una fila tiene campos obligatorios incompletos.
     */
    private boolean filaIncompleta(Row row) {
        for (int col = 0; col <= 6; col++) {
            if (col == 5) continue; // saltar correquisito
            String val = getCellString(row, col);
            if (val == null || val.isEmpty()) {
                return true;
            }
        }
        return false;
    }



}
