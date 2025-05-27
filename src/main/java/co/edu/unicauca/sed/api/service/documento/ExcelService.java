package co.edu.unicauca.sed.api.service.documento;

import co.edu.unicauca.sed.api.dto.CalificacionPorPeriodoDTO;
import co.edu.unicauca.sed.api.dto.ConsolidadoDTO;
import co.edu.unicauca.sed.api.dto.DocenteEvaluacionDTO;
import co.edu.unicauca.sed.api.dto.FuenteDTO;
import co.edu.unicauca.sed.api.dto.HistoricoCalificacionesDTO;
import co.edu.unicauca.sed.api.dto.InformacionConsolidadoDTO;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class ExcelService {

    @Autowired
    private FileService fileService;

    public Path generarExcelConsolidado(ConsolidadoDTO consolidadoDTO, String nombreDocumento, String nota) throws IOException {
        nota = Optional.ofNullable(nota).orElse("");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Consolidado");

            int currentRow = llenarDatosPrincipales(sheet, consolidadoDTO);
            String[] headers = { "ACTIVIDAD", "HS", "%", "Fuente 1", "Fuente 2", "Promedio", "Acumula" };
            crearEncabezados(sheet, currentRow++, headers, workbook);

            double totalHS = 0;
            double totalPorcentaje = 0;
            double totalAcumulado = 0;

            for (var entry : consolidadoDTO.getActividades().entrySet()) {
                currentRow = agregarTituloTipoActividad(sheet, workbook, currentRow, entry.getKey());

                for (Object actividadObj : entry.getValue()) {
                    if (actividadObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> actividad = (Map<String, Object>) actividadObj;

                        TotalesActividad totales = procesarActividad(sheet, currentRow++, actividad);
                        totalHS += totales.horas;
                        totalPorcentaje += totales.porcentaje;
                        totalAcumulado += totales.acumulado;
                    }
                }
            }

            currentRow = agregarTotales(sheet, workbook, currentRow, totalHS, consolidadoDTO.getTotalPorcentaje(), consolidadoDTO.getTotalAcumulado());

            if (!nota.isEmpty()) {
                agregarNota(sheet, workbook, currentRow++, nota);
            }

            return guardarArchivoExcel(workbook, nombreDocumento, consolidadoDTO);
        }
    }

    private int agregarTituloTipoActividad(Sheet sheet, Workbook workbook, int rowIndex, String tipoActividad) {
        Row row = sheet.createRow(rowIndex++);
        Cell cell = row.createCell(0);
        cell.setCellValue(tipoActividad);
        cell.setCellStyle(crearEstiloTitulo(workbook));
        return rowIndex;
    }

    private TotalesActividad procesarActividad(Sheet sheet, int rowIndex, Map<String, Object> actividad) {
        Row row = sheet.createRow(rowIndex);

        String nombre = (String) actividad.get("nombre");
        float horas = Optional.ofNullable((Float) actividad.get("horas")).orElse(0.0f);
        double porcentaje = Optional.ofNullable((Double) actividad.get("porcentaje")).orElse(0.0);
        double promedio = Optional.ofNullable((Double) actividad.get("promedio")).orElse(0.0);
        double acumulado = Optional.ofNullable((Double) actividad.get("acumulado")).orElse(0.0);

        row.createCell(0).setCellValue(nombre);
        row.createCell(1).setCellValue(horas);
        row.createCell(2).setCellValue(porcentaje);
        row.createCell(5).setCellValue(promedio);
        row.createCell(6).setCellValue(acumulado);

        List<FuenteDTO> fuentes = (List<FuenteDTO>) actividad.get("fuentes");
        Cell fuente1Cell = row.createCell(3);
        Cell fuente2Cell = row.createCell(4);

        setCellValueSafe(fuente1Cell, getSafeCalificacion(fuentes, 0));
        setCellValueSafe(fuente2Cell, getSafeCalificacion(fuentes, 1));

        return new TotalesActividad(horas, porcentaje, acumulado);
    }

    private void setCellValueSafe(Cell cell, Float value) {
        if (value != null) {
            cell.setCellValue(value);
        }
    }

    private Float getSafeCalificacion(List<FuenteDTO> fuentes, int index) {
        if (fuentes != null && fuentes.size() > index && fuentes.get(index) != null) {
            return fuentes.get(index).getCalificacion(); // Retorna null si la calificación es null
        }
        return null;
    }

    private int agregarTotales(Sheet sheet, Workbook workbook, int rowIndex, double totalHS, double totalPorcentaje, double totalAcumulado) {
        Row totalRow = sheet.createRow(rowIndex);
        CellStyle totalStyle = crearEstiloTitulo(workbook);

        totalRow.createCell(0).setCellValue("TOTALES:");
        totalRow.createCell(1).setCellValue(totalHS);
        totalRow.createCell(2).setCellValue(totalPorcentaje);
        totalRow.createCell(6).setCellValue(totalAcumulado);

        for (int i = 0; i <= 6; i++) {
            if (totalRow.getCell(i) != null) {
                totalRow.getCell(i).setCellStyle(totalStyle);
            }
        }
        return rowIndex + 1;
    }

    private void agregarNota(Sheet sheet, Workbook workbook, int rowIndex, String nota) {
        Row notaRow = sheet.createRow(rowIndex);
        Cell notaCell = notaRow.createCell(0);
        notaCell.setCellValue("Nota: " + nota);

        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 6));

        CellStyle notaStyle = workbook.createCellStyle();
        Font notaFont = workbook.createFont();
        notaFont.setItalic(true);
        notaStyle.setFont(notaFont);
        notaStyle.setWrapText(true);
        notaCell.setCellStyle(notaStyle);
    }

    private Path guardarArchivoExcel(Workbook workbook, String nombreDocumento, ConsolidadoDTO consolidadoDTO) throws IOException {
        MultipartFile multipartFile = workbookToMultipartFile(workbook, nombreDocumento);
        return fileService.guardarArchivo(
                multipartFile,
                consolidadoDTO.getPeriodoAcademico(),
                consolidadoDTO.getTipoContratacion(),
                consolidadoDTO.getDepartamento(),
                "Consolidados");
    }

    private MultipartFile workbookToMultipartFile(Workbook workbook, String nombreDocumento) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.write(bos);
            return new MockMultipartFile(
                    "file",
                    nombreDocumento + ".xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    bos.toByteArray());
        }
    }

    private CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static class TotalesActividad {
        double horas, porcentaje, acumulado;

        TotalesActividad(double horas, double porcentaje, double acumulado) {
            this.horas = horas;
            this.porcentaje = porcentaje;
            this.acumulado = acumulado;
        }
    }

    private int llenarDatosPrincipales(Sheet sheet, ConsolidadoDTO consolidadoDTO) {
        int currentRow = 0;

        // Crear un estilo para los títulos
        CellStyle titleStyle = crearEstiloTitulo(sheet.getWorkbook());

        // Datos principales
        currentRow = agregarFila(sheet, currentRow, "Nombre del Docente:", consolidadoDTO.getNombreDocente(), titleStyle);
        currentRow = agregarFila(sheet, currentRow, "Número de Identificación:", consolidadoDTO.getNumeroIdentificacion(), titleStyle);
        currentRow = agregarFila(sheet, currentRow, "Periodo Académico:", consolidadoDTO.getPeriodoAcademico(),titleStyle);

        return currentRow;
    }

    /**
     * Agrega una fila con una etiqueta y su respectivo valor en el Excel.
     */
    private int agregarFila(Sheet sheet, int rowIndex, String etiqueta, String valor, CellStyle titleStyle) {
        Row row = sheet.createRow(rowIndex++);
        Cell etiquetaCell = row.createCell(0);
        Cell valorCell = row.createCell(1);

        etiquetaCell.setCellValue(etiqueta);
        etiquetaCell.setCellStyle(titleStyle);
        valorCell.setCellValue(Optional.ofNullable(valor).orElse(""));

        return rowIndex;
    }

    private void crearEncabezados(Sheet sheet, int rowNumber, String[] headers, Workbook workbook) {
        Row headerRow = sheet.createRow(rowNumber);
        CellStyle headerStyle = crearEstiloTitulo(workbook);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    public ByteArrayOutputStream generarExcelInformacionConsolidado(List<InformacionConsolidadoDTO> data, String[] headers) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Sheet sheet = workbook.createSheet("Resumen Consolidado");

            crearEncabezados(sheet, 0, headers, workbook);

            // Agregar datos
            int rowIdx = 1;
            for (InformacionConsolidadoDTO item : data) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(item.getNombreDocente());
                row.createCell(1).setCellValue(item.getNumeroIdentificacion());
                row.createCell(2).setCellValue(item.getFacultad());
                row.createCell(3).setCellValue(item.getDepartamento());
                row.createCell(4).setCellValue(item.getCategoria());
                row.createCell(5).setCellValue(item.getCalificacion() != null ? item.getCalificacion() : 0);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out;

        } finally {
            workbook.close();
        }
    }

    public ByteArrayOutputStream generarExcelEvaluacionDocente(List<DocenteEvaluacionDTO> data, String[] headers)
            throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Evaluación Docente");

            crearEncabezados(sheet, 0, headers, workbook);

            int rowIdx = 1;
            for (DocenteEvaluacionDTO item : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.getNombreDocente());
                row.createCell(1).setCellValue(item.getIdentificacion());
                row.createCell(2).setCellValue(item.getContratacion());
                row.createCell(3)
                        .setCellValue(item.getPorcentajeEvaluacionCompletado() != null
                                ? item.getPorcentajeEvaluacionCompletado()
                                : 0);
                row.createCell(4).setCellValue(item.getEstadoConsolidado());
                row.createCell(5).setCellValue(item.getTotalAcumulado() != null ? item.getTotalAcumulado() : 0);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out;
        }
    }

    public ByteArrayOutputStream generarExcelHistorico(List<HistoricoCalificacionesDTO> datos) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Histórico Calificaciones");
    
        // Obtener todos los periodos únicos ordenados
        Set<String> periodosUnicos = datos.stream()
                .flatMap(dto -> dto.getCalificacionesPorPeriodo().stream())
                .map(CalificacionPorPeriodoDTO::getIdPeriodo)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    
        // Crear encabezados
        Row header = sheet.createRow(0);
        int col = 0;
        header.createCell(col++).setCellValue("Nombre");
        header.createCell(col++).setCellValue("Identificación");
        header.createCell(col++).setCellValue("Facultad");
        header.createCell(col++).setCellValue("Departamento");
        header.createCell(col++).setCellValue("Categoria");
    
        Map<String, Integer> colPorPeriodo = new HashMap<>();
        for (String periodo : periodosUnicos) {
            header.createCell(col).setCellValue("Periodo " + periodo);
            colPorPeriodo.put(periodo, col++);
        }
    
        header.createCell(col).setCellValue("Total");
    
        // Crear filas por docente
        int rowNum = 1;
        for (HistoricoCalificacionesDTO dto : datos) {
            Row row = sheet.createRow(rowNum++);
            int c = 0;
            row.createCell(c++).setCellValue(dto.getNombreDocente());
            row.createCell(c++).setCellValue(dto.getNumeroIdentificacion());
            row.createCell(c++).setCellValue(dto.getFacultad());
            row.createCell(c++).setCellValue(dto.getDepartamento());
            row.createCell(c++).setCellValue(dto.getCategoria());
    
            for (CalificacionPorPeriodoDTO cal : dto.getCalificacionesPorPeriodo()) {
                if (cal.getCalificacion() != null && colPorPeriodo.containsKey(cal.getIdPeriodo())) {
                    int colPos = colPorPeriodo.get(cal.getIdPeriodo());
                    row.createCell(colPos).setCellValue(cal.getCalificacion());
                }
            }
    
            row.createCell(col).setCellValue(dto.getPromedioGeneral() != null ? dto.getPromedioGeneral() : 0);
        }
    
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out;
    }
}
