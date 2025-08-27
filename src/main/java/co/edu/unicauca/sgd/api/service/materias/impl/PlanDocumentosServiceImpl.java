package co.edu.unicauca.sgd.api.service.materias.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.usermodel.Cell;
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
import co.edu.unicauca.sgd.api.repository.DepartamentoRepository;
import co.edu.unicauca.sgd.api.service.materias.PlanDocumentosService;

@Service
public class PlanDocumentosServiceImpl implements PlanDocumentosService {

    private static final Logger logger = LoggerFactory.getLogger(PlanDocumentosServiceImpl.class);

    private DepartamentoRepository departamentoRepository;

    public PlanDocumentosServiceImpl(DepartamentoRepository departamentoRepository) {
        this.departamentoRepository = departamentoRepository;
    }

    @Override
    public ByteArrayOutputStream generarFormatoAdicion(Integer oidPlan) throws IOException {

        logger.info("Generando formato de adición para oidPlan: {}", oidPlan);

        try (InputStream templateStream = getClass().getClassLoader().getResourceAsStream("formatos/Formato_Adicion.xlsx");
                XSSFWorkbook workbook = new XSSFWorkbook(templateStream)) {

            // Obtén los nombres de los departamentos
            List<String> nombresDepartamentos = departamentoRepository.findAll()
                .stream()
                .map(Departamento::getNombre)
                .toList();

            // 1. Crea la hoja oculta con los departamentos
            XSSFSheet hojaDeptos = workbook.createSheet("DEPARTAMENTOS_LISTA");
            hojaDeptos.createRow(0).createCell(0).setCellValue("NINGUNO");
            for (int i = 0; i < nombresDepartamentos.size(); i++) {
                hojaDeptos.createRow(i+1).createCell(0).setCellValue(nombresDepartamentos.get(i));
            }

            // 2. Crea el Named Range para la lista
            XSSFName namedRange = workbook.createName();
            namedRange.setNameName("DEPTOS_LISTA");
            String reference = "DEPARTAMENTOS_LISTA!$A$1:$A$" + String.valueOf(nombresDepartamentos.size() + 1);
            namedRange.setRefersToFormula(reference);

            // 3. Obtener la hoja principal ANTES de crear la validación
            XSSFSheet hoja = workbook.getSheetAt(0);
            
            // 4. Crear la validación de datos con configuración más específica
            DataValidationHelper helper = hoja.getDataValidationHelper();
            DataValidationConstraint constraint = helper.createFormulaListConstraint("=DEPTOS_LISTA");
            
            // Definir el rango de celdas donde se aplicará la validación (columna G, filas 2-100)
            CellRangeAddressList addressList = new CellRangeAddressList(1, 99, 6, 6);
            DataValidation validation = helper.createValidation(constraint, addressList);
            
            // Configurar la validación
            validation.setShowErrorBox(true);
            validation.setShowPromptBox(true);
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.createErrorBox("Error", "Por favor seleccione un departamento válido de la lista.");
            validation.createPromptBox("Departamento", "Seleccione un departamento de la lista desplegable.");
            
            // Aplicar la validación a la hoja
            hoja.addValidationData(validation);

            // 6. Establecer el valor por defecto "NINGUNO" en las celdas con validación
            for (int fila = 1; fila <= 99; fila++) {
                Row row = hoja.getRow(fila);
                if (row == null) {
                    row = hoja.createRow(fila);
                }
                Cell cell = row.getCell(6);
                if (cell == null) {
                    cell = row.createCell(6);
                }
                cell.setCellValue("NINGUNO");
            }

            // 7. Oculta la hoja de departamentos
            workbook.setSheetHidden(workbook.getSheetIndex(hojaDeptos), true);

            // Obtener la hoja principal
            XSSFSheet hiddenSheet = workbook.createSheet("OIDPLAN_OCULTO");
            hiddenSheet.createRow(0).createCell(0).setCellValue(oidPlan);
            workbook.setSheetHidden(workbook.getSheetIndex(hiddenSheet), true);

            // Obtener la hoja principal
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            logger.info("Formato de adición generado exitosamente para oidPlan: {}", oidPlan);
            return outputStream;

        } catch (Exception e) {
            logger.error("Error generando formato de adición para oidPlan: {}", oidPlan, e);
            throw new IOException("Error generando formato de adición", e);
        }
    }

}
