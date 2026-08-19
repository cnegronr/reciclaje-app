package cl.reciclajelitoral.service;

import cl.reciclajelitoral.entity.DetalleInspeccion;
import cl.reciclajelitoral.entity.FotoInspeccion;
import cl.reciclajelitoral.repository.DetalleInspeccionRepository;
import cl.reciclajelitoral.repository.InspeccionSemanalRepository;
import cl.reciclajelitoral.util.WeekDateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final DetalleInspeccionRepository detalleRepository;
    private final InspeccionSemanalRepository inspeccionSemanalRepository;
    private final S3StorageService s3StorageService;

    public int getEffectiveWeekNumber(DetalleInspeccion d) {
        LocalDateTime dt = getEffectiveLocalDateTime(d);
        if (dt == null) {
            return d.getInspeccionSemanal() != null ? d.getInspeccionSemanal().getSemanaNumero() : -1;
        }
        return WeekDateUtils.getWeekNumber(dt);
    }

    public int getEffectiveYear(DetalleInspeccion d) {
        LocalDateTime dt = getEffectiveLocalDateTime(d);
        if (dt == null) {
            return d.getInspeccionSemanal() != null ? d.getInspeccionSemanal().getAnio() : -1;
        }
        return WeekDateUtils.getYear(dt);
    }

    private LocalDateTime getEffectiveLocalDateTime(DetalleInspeccion d) {
        if (d.getFechaHoraInicial() != null) return d.getFechaHoraInicial();
        if (d.getFechaHoraActualizacion() != null) return d.getFechaHoraActualizacion();
        if (d.getInspeccionSemanal() != null && d.getInspeccionSemanal().getCreadoEn() != null) {
            return d.getInspeccionSemanal().getCreadoEn();
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<Integer> getAvailableReportYears() {
        int currentYear = WeekDateUtils.getCurrentYear();
        Set<Integer> yearsSet = new TreeSet<>(Comparator.reverseOrder());
        yearsSet.add(currentYear);

        List<DetalleInspeccion> detalles = detalleRepository.findAll();
        for (DetalleInspeccion d : detalles) {
            if (Boolean.TRUE.equals(d.getVisitado())) {
                int yr = getEffectiveYear(d);
                if (yr > 2000) {
                    yearsSet.add(yr);
                }
            }
        }
        yearsSet.addAll(inspeccionSemanalRepository.findDistinctAnios());
        return new ArrayList<>(yearsSet);
    }

    @Transactional(readOnly = true)
    public byte[] generateExcelReport(Long comunaId, Long usuarioId, Integer semanaNumero, Integer anio) throws IOException {
        List<DetalleInspeccion> detalles = detalleRepository.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getVisitado()))
                .filter(d -> comunaId == null || (d.getContenedor() != null && d.getContenedor().getComuna() != null && d.getContenedor().getComuna().getId().equals(comunaId)))
                .filter(d -> usuarioId == null ||
                        (d.getActualizadoPorUsuario() != null && d.getActualizadoPorUsuario().getId().equals(usuarioId)) ||
                        (d.getCreadoPorUsuario() != null && d.getCreadoPorUsuario().getId().equals(usuarioId)) ||
                        (d.getInspeccionSemanal() != null && d.getInspeccionSemanal().getInspector() != null && d.getInspeccionSemanal().getInspector().getId().equals(usuarioId)))
                .filter(d -> semanaNumero == null || semanaNumero.equals(getEffectiveWeekNumber(d)))
                .filter(d -> anio == null || anio.equals(getEffectiveYear(d)))
                .toList();

        int maxFotosAntes = 1;
        int maxFotosDespues = 1;

        for (DetalleInspeccion d : detalles) {
            List<FotoInspeccion> fotos = d.getFotos();
            if (fotos != null && !fotos.isEmpty()) {
                int countAntes = 0;
                int countDespues = 0;
                for (FotoInspeccion f : fotos) {
                    if (f.getMomento() != null && f.getMomento().name().contains("ANTES")) {
                        countAntes++;
                    } else if (f.getMomento() != null && f.getMomento().name().contains("DESPUES")) {
                        countDespues++;
                    }
                }
                if (countAntes == 0 && countDespues == 0) {
                    countAntes = 1;
                    if (fotos.size() > 1) countDespues = fotos.size() - 1;
                }
                maxFotosAntes = Math.max(maxFotosAntes, countAntes);
                maxFotosDespues = Math.max(maxFotosDespues, countDespues);
            }
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte Consolidado");

            // Styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle linkStyle = workbook.createCellStyle();
            Font linkFont = workbook.createFont();
            linkFont.setUnderline(Font.U_SINGLE);
            linkFont.setColor(IndexedColors.BLUE.getIndex());
            linkStyle.setFont(linkFont);
            linkStyle.setAlignment(HorizontalAlignment.CENTER);
            linkStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Encabezados con columnas dedicadas por foto
            List<String> headersList = new ArrayList<>(List.of(
                    "ID Detalle", "Semana / Año", "Comuna", "Contenedor / Punto",
                    "Categoría", "Porcentaje Llenado (%)", "Kilos Calculados", "Usuario / Inspector",
                    "Observaciones"
            ));

            for (int i = 1; i <= maxFotosAntes; i++) {
                headersList.add(maxFotosAntes == 1 ? "Foto Antes (Vista Previa)" : "Foto Antes " + i + " (Vista Previa)");
                headersList.add(maxFotosAntes == 1 ? "Enlace S3 Foto Antes (HD ↗)" : "Enlace S3 Foto Antes " + i + " (HD ↗)");
            }

            for (int i = 1; i <= maxFotosDespues; i++) {
                headersList.add(maxFotosDespues == 1 ? "Foto Después (Vista Previa)" : "Foto Después " + i + " (Vista Previa)");
                headersList.add(maxFotosDespues == 1 ? "Enlace S3 Foto Después " + i + " (HD ↗)" : "Enlace S3 Foto Después " + i + " (HD ↗)");
            }

            Row header = sheet.createRow(0);
            header.setHeightInPoints(25f);
            for (int i = 0; i < headersList.size(); i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headersList.get(i));
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            CreationHelper createHelper = workbook.getCreationHelper();
            Drawing<?> drawing = sheet.createDrawingPatriarch();

            for (DetalleInspeccion d : detalles) {
                String nombrePunto = d.getContenedor() != null ? d.getContenedor().getNombrePunto() : "Contenedor " + d.getId();
                int effWeek = getEffectiveWeekNumber(d);
                int effYear = getEffectiveYear(d);
                String semanaAnio = "Semana " + effWeek + " (" + effYear + ")";

                Row row = sheet.createRow(rowIdx);

                row.createCell(0).setCellValue(d.getId());
                row.createCell(1).setCellValue(semanaAnio);
                row.createCell(2).setCellValue(d.getContenedor() != null && d.getContenedor().getComuna() != null ? d.getContenedor().getComuna().getNombre() : "N/A");
                row.createCell(3).setCellValue(nombrePunto);
                row.createCell(4).setCellValue(d.getContenedor() != null && d.getContenedor().getCategoria() != null ? d.getContenedor().getCategoria().name() : "N/A");
                row.createCell(5).setCellValue(d.getPorcentajeEstimado() != null ? d.getPorcentajeEstimado().doubleValue() : 0.0);
                row.createCell(6).setCellValue(d.getKilosCalculados() != null ? d.getKilosCalculados().doubleValue() : 0.0);

                String userNombre = d.getActualizadoPorUsuario() != null ? d.getActualizadoPorUsuario().getNombre() :
                        (d.getCreadoPorUsuario() != null ? d.getCreadoPorUsuario().getNombre() : "Sistema");
                row.createCell(7).setCellValue(userNombre);
                row.createCell(8).setCellValue(d.getObservaciones() != null ? d.getObservaciones() : "");

                List<FotoInspeccion> fotos = d.getFotos();
                List<FotoInspeccion> fotosAntes = new ArrayList<>();
                List<FotoInspeccion> fotosDespues = new ArrayList<>();

                if (fotos != null && !fotos.isEmpty()) {
                    for (FotoInspeccion f : fotos) {
                        if (f.getMomento() != null && f.getMomento().name().contains("ANTES")) {
                            fotosAntes.add(f);
                        } else if (f.getMomento() != null && f.getMomento().name().contains("DESPUES")) {
                            fotosDespues.add(f);
                        }
                    }
                    if (fotosAntes.isEmpty() && fotosDespues.isEmpty()) {
                        fotosAntes.add(fotos.get(0));
                        for (int i = 1; i < fotos.size(); i++) {
                            fotosDespues.add(fotos.get(i));
                        }
                    }
                }

                boolean hasPhoto = false;
                int currentColIdx = 9;

                // 1. Columnas para Fotos ANTES
                for (int i = 0; i < maxFotosAntes; i++) {
                    int colThumb = currentColIdx++;
                    int colLink = currentColIdx++;

                    Cell thumbCell = row.createCell(colThumb);
                    Cell linkCell = row.createCell(colLink);

                    if (i < fotosAntes.size()) {
                        FotoInspeccion f = fotosAntes.get(i);
                        String freshUrl = s3StorageService.obtenerUrlFresca(f.getUrlFoto());

                        byte[] thumb = getResizedThumbnailBytes(f, nombrePunto, 110, 75);
                        if (thumb.length > 0) {
                            int picIdx = workbook.addPicture(thumb, Workbook.PICTURE_TYPE_JPEG);
                            ClientAnchor anchor = createHelper.createClientAnchor();
                            anchor.setCol1(colThumb);
                            anchor.setRow1(rowIdx);
                            anchor.setCol2(colThumb + 1);
                            anchor.setRow2(rowIdx + 1);

                            drawing.createPicture(anchor, picIdx);
                            hasPhoto = true;
                        }

                        if (freshUrl != null && !freshUrl.isEmpty()) {
                            // Asignar hipervínculo estándar de POI a la celda de miniatura
                            XSSFHyperlink hLinkThumb = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL);
                            hLinkThumb.setAddress(freshUrl);
                            thumbCell.setHyperlink(hLinkThumb);

                            // Asignar hipervínculo estándar de POI a la celda de enlace HD
                            String label = maxFotosAntes == 1 ? "🔗 Abrir Foto en S3 HD ↗" : "🔗 Abrir Foto " + (i + 1) + " en S3 HD ↗";
                            linkCell.setCellValue(label);
                            XSSFHyperlink hLinkText = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL);
                            hLinkText.setAddress(freshUrl);
                            linkCell.setHyperlink(hLinkText);
                            linkCell.setCellStyle(linkStyle);
                        } else {
                            linkCell.setCellValue("Sin URL");
                        }
                    } else {
                        thumbCell.setCellValue("-");
                        linkCell.setCellValue("-");
                    }
                }

                // 2. Columnas para Fotos DESPUÉS
                for (int i = 0; i < maxFotosDespues; i++) {
                    int colThumb = currentColIdx++;
                    int colLink = currentColIdx++;

                    Cell thumbCell = row.createCell(colThumb);
                    Cell linkCell = row.createCell(colLink);

                    if (i < fotosDespues.size()) {
                        FotoInspeccion f = fotosDespues.get(i);
                        String freshUrl = s3StorageService.obtenerUrlFresca(f.getUrlFoto());

                        byte[] thumb = getResizedThumbnailBytes(f, nombrePunto, 110, 75);
                        if (thumb.length > 0) {
                            int picIdx = workbook.addPicture(thumb, Workbook.PICTURE_TYPE_JPEG);
                            ClientAnchor anchor = createHelper.createClientAnchor();
                            anchor.setCol1(colThumb);
                            anchor.setRow1(rowIdx);
                            anchor.setCol2(colThumb + 1);
                            anchor.setRow2(rowIdx + 1);

                            drawing.createPicture(anchor, picIdx);
                            hasPhoto = true;
                        }

                        if (freshUrl != null && !freshUrl.isEmpty()) {
                            // Asignar hipervínculo estándar de POI a la celda de miniatura
                            XSSFHyperlink hLinkThumb = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL);
                            hLinkThumb.setAddress(freshUrl);
                            thumbCell.setHyperlink(hLinkThumb);

                            // Asignar hipervínculo estándar de POI a la celda de enlace HD
                            String label = maxFotosDespues == 1 ? "🔗 Abrir Foto en S3 HD ↗" : "🔗 Abrir Foto " + (i + 1) + " en S3 HD ↗";
                            linkCell.setCellValue(label);
                            XSSFHyperlink hLinkText = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL);
                            hLinkText.setAddress(freshUrl);
                            linkCell.setHyperlink(hLinkText);
                            linkCell.setCellStyle(linkStyle);
                        } else {
                            linkCell.setCellValue("Sin URL");
                        }
                    } else {
                        thumbCell.setCellValue("-");
                        linkCell.setCellValue("-");
                    }
                }

                if (hasPhoto) {
                    row.setHeightInPoints(65f);
                }

                rowIdx++;
            }

            for (int i = 0; i < headersList.size(); i++) {
                if (i >= 9) {
                    if (i % 2 != 0) {
                        sheet.setColumnWidth(i, 4500); // Columna miniatura
                    } else {
                        sheet.setColumnWidth(i, 7500); // Columna enlace HD
                    }
                } else {
                    sheet.autoSizeColumn(i);
                }
            }

            ByteArrayOutputStream excelBaos = new ByteArrayOutputStream();
            workbook.write(excelBaos);
            return excelBaos.toByteArray();
        }
    }

    public byte[] generateExcelZipReport(Long comunaId, Long usuarioId) throws IOException {
        return generateExcelReport(comunaId, usuarioId, null, null);
    }

    @Transactional(readOnly = true)
    public byte[] generatePdfReport(Long comunaId, Long usuarioId, Integer semanaNumero, Integer anio) throws Exception {
        List<DetalleInspeccion> detalles = detalleRepository.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getVisitado()))
                .filter(d -> comunaId == null || (d.getContenedor() != null && d.getContenedor().getComuna() != null && d.getContenedor().getComuna().getId().equals(comunaId)))
                .filter(d -> usuarioId == null ||
                        (d.getActualizadoPorUsuario() != null && d.getActualizadoPorUsuario().getId().equals(usuarioId)) ||
                        (d.getCreadoPorUsuario() != null && d.getCreadoPorUsuario().getId().equals(usuarioId)) ||
                        (d.getInspeccionSemanal() != null && d.getInspeccionSemanal().getInspector() != null && d.getInspeccionSemanal().getInspector().getId().equals(usuarioId)))
                .filter(d -> semanaNumero == null || semanaNumero.equals(getEffectiveWeekNumber(d)))
                .filter(d -> anio == null || anio.equals(getEffectiveYear(d)))
                .toList();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        com.lowagie.text.Document document = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4.rotate(), 36, 36, 36, 45);
        com.lowagie.text.pdf.PdfWriter writer = com.lowagie.text.pdf.PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new HeaderFooterPageEvent());

        document.open();

        // Timestamp in Chile Timezone (America/Santiago)
        java.time.ZonedDateTime ahoraChile = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Santiago"));
        String fechaChileStr = ahoraChile.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        // Header Table (Width 100%, 2 columns: 60% / 40%)
        com.lowagie.text.pdf.PdfPTable headerTable = new com.lowagie.text.pdf.PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{6.0f, 4.0f});

        // Left Column: Brand & Title
        com.lowagie.text.pdf.PdfPCell leftHeaderCell = new com.lowagie.text.pdf.PdfPCell();
        leftHeaderCell.setBorder(com.lowagie.text.pdf.PdfPCell.NO_BORDER);
        leftHeaderCell.setPadding(0);

        com.lowagie.text.Font titleFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 18, new java.awt.Color(15, 23, 42));
        com.lowagie.text.Font subtitleFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 10, new java.awt.Color(16, 185, 129));

        leftHeaderCell.addElement(new com.lowagie.text.Paragraph("♻️ RECICLAJE LITORAL", titleFont));
        leftHeaderCell.addElement(new com.lowagie.text.Paragraph("Reporte Consolidado de Inspección de Vidrio", subtitleFont));

        // Right Column: Document Info (Chile Time)
        com.lowagie.text.pdf.PdfPCell rightHeaderCell = new com.lowagie.text.pdf.PdfPCell();
        rightHeaderCell.setBorder(com.lowagie.text.pdf.PdfPCell.NO_BORDER);
        rightHeaderCell.setPadding(0);
        rightHeaderCell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);

        com.lowagie.text.Font metaLabelFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 8, new java.awt.Color(100, 116, 139));
        com.lowagie.text.Font metaValFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA, 8, new java.awt.Color(15, 23, 42));

        com.lowagie.text.Paragraph metaP1 = new com.lowagie.text.Paragraph("DOCUMENTO OFICIAL DE MONITOREO", metaLabelFont);
        metaP1.setAlignment(com.lowagie.text.Element.ALIGN_RIGHT);

        com.lowagie.text.Paragraph metaP2 = new com.lowagie.text.Paragraph("Generado el: " + fechaChileStr + " (Hora Chile)", metaValFont);
        metaP2.setAlignment(com.lowagie.text.Element.ALIGN_RIGHT);

        String filtroTexto = "Filtro: ";
        if (semanaNumero != null && anio != null) {
            filtroTexto += "Semana " + semanaNumero + " (" + anio + ")";
        } else {
            filtroTexto += "Todas las Semanas";
        }
        if (comunaId != null && !detalles.isEmpty() && detalles.get(0).getContenedor() != null && detalles.get(0).getContenedor().getComuna() != null) {
            filtroTexto += " | Comuna: " + detalles.get(0).getContenedor().getComuna().getNombre();
        }
        com.lowagie.text.Paragraph metaP3 = new com.lowagie.text.Paragraph(filtroTexto, metaValFont);
        metaP3.setAlignment(com.lowagie.text.Element.ALIGN_RIGHT);

        rightHeaderCell.addElement(metaP1);
        rightHeaderCell.addElement(metaP2);
        rightHeaderCell.addElement(metaP3);

        headerTable.addCell(leftHeaderCell);
        headerTable.addCell(rightHeaderCell);
        document.add(headerTable);

        // Divider spacing
        com.lowagie.text.Paragraph gap = new com.lowagie.text.Paragraph(" ");
        gap.setSpacingBefore(6f);
        document.add(gap);

        // KPI Calculations
        double totalKilos = 0;
        double totalPorcentaje = 0;
        int totalPuntos = detalles.size();

        for (DetalleInspeccion d : detalles) {
            double kg = d.getKilosCalculados() != null ? d.getKilosCalculados().doubleValue() : 0.0;
            totalKilos += kg;
            if (d.getPorcentajeEstimado() != null) {
                totalPorcentaje += d.getPorcentajeEstimado().doubleValue();
            }
        }
        double promedioLlenado = totalPuntos > 0 ? (totalPorcentaje / totalPuntos) : 0;

        // KPI Summary Box (3 columns)
        com.lowagie.text.pdf.PdfPTable kpiTable = new com.lowagie.text.pdf.PdfPTable(3);
        kpiTable.setWidthPercentage(100);
        kpiTable.setWidths(new float[]{1f, 1f, 1f});
        kpiTable.setSpacingAfter(10f);

        com.lowagie.text.Font kpiValFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 12, new java.awt.Color(5, 150, 105));
        com.lowagie.text.Font kpiLblFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA, 8, new java.awt.Color(100, 116, 139));

        kpiTable.addCell(createKpiCell("Total Puntos Inspeccionados", String.valueOf(totalPuntos), kpiLblFont, kpiValFont));
        kpiTable.addCell(createKpiCell("Total Kilos Recolectados", String.format("%.1f kg", totalKilos), kpiLblFont, kpiValFont));
        kpiTable.addCell(createKpiCell("Promedio % Llenado", String.format("%.1f%%", promedioLlenado), kpiLblFont, kpiValFont));
        document.add(kpiTable);

        // Main Table
        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.0f, 2.2f, 3.8f, 1.8f, 1.8f, 2.0f, 2.4f, 4.0f});

        com.lowagie.text.Font headFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 8.5f, java.awt.Color.WHITE);
        String[] headers = {"ID", "Comuna", "Punto Limpio", "Categoría", "% Llenado", "Kilos (kg)", "Inspector", "Observaciones"};

        for (int i = 0; i < headers.length; i++) {
            com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(headers[i], headFont));
            cell.setBackgroundColor(new java.awt.Color(30, 41, 59));
            cell.setBorderColor(new java.awt.Color(51, 65, 85));
            cell.setPadding(6f);
            if (i == 4 || i == 5) {
                cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
            } else if (i == 0 || i == 3) {
                cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            } else {
                cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_LEFT);
            }
            table.addCell(cell);
        }

        com.lowagie.text.Font bodyFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA, 8, new java.awt.Color(51, 65, 85));
        com.lowagie.text.Font bodyFontBold = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 8, new java.awt.Color(15, 23, 42));
        com.lowagie.text.Font catFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 7.5f, new java.awt.Color(2, 132, 199));

        java.awt.Color lightBg = new java.awt.Color(248, 250, 252);
        java.awt.Color whiteBg = java.awt.Color.WHITE;
        java.awt.Color borderColor = new java.awt.Color(226, 232, 240);

        int rowIndex = 0;
        for (DetalleInspeccion d : detalles) {
            java.awt.Color currentBg = (rowIndex % 2 == 1) ? lightBg : whiteBg;
            rowIndex++;

            // ID
            com.lowagie.text.pdf.PdfPCell cId = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(String.valueOf(d.getId()), bodyFont));
            cId.setBackgroundColor(currentBg);
            cId.setBorderColor(borderColor);
            cId.setPadding(5f);
            cId.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            table.addCell(cId);

            // Comuna
            String comunaNombre = d.getContenedor() != null && d.getContenedor().getComuna() != null ? d.getContenedor().getComuna().getNombre() : "N/A";
            com.lowagie.text.pdf.PdfPCell cComuna = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(comunaNombre, bodyFontBold));
            cComuna.setBackgroundColor(currentBg);
            cComuna.setBorderColor(borderColor);
            cComuna.setPadding(5f);
            table.addCell(cComuna);

            // Punto Limpio
            String puntoNombre = d.getContenedor() != null ? d.getContenedor().getNombrePunto() : "-";
            com.lowagie.text.pdf.PdfPCell cPunto = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(puntoNombre, bodyFont));
            cPunto.setBackgroundColor(currentBg);
            cPunto.setBorderColor(borderColor);
            cPunto.setPadding(5f);
            table.addCell(cPunto);

            // Categoria
            String cat = d.getContenedor() != null && d.getContenedor().getCategoria() != null ? d.getContenedor().getCategoria().name() : "-";
            com.lowagie.text.pdf.PdfPCell cCat = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(cat, catFont));
            cCat.setBackgroundColor(currentBg);
            cCat.setBorderColor(borderColor);
            cCat.setPadding(5f);
            cCat.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            table.addCell(cCat);

            // % Llenado
            String pctStr = String.format("%.0f%%", d.getPorcentajeEstimado() != null ? d.getPorcentajeEstimado().doubleValue() : 0.0);
            com.lowagie.text.pdf.PdfPCell cPct = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(pctStr, bodyFontBold));
            cPct.setBackgroundColor(currentBg);
            cPct.setBorderColor(borderColor);
            cPct.setPadding(5f);
            cPct.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
            table.addCell(cPct);

            // Kilos
            double kg = d.getKilosCalculados() != null ? d.getKilosCalculados().doubleValue() : 0.0;
            com.lowagie.text.pdf.PdfPCell cKg = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(String.format("%.1f", kg), bodyFontBold));
            cKg.setBackgroundColor(currentBg);
            cKg.setBorderColor(borderColor);
            cKg.setPadding(5f);
            cKg.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
            table.addCell(cKg);

            // Inspector
            String userNombre = d.getActualizadoPorUsuario() != null ? d.getActualizadoPorUsuario().getNombre() :
                    (d.getCreadoPorUsuario() != null ? d.getCreadoPorUsuario().getNombre() : "Sistema");
            com.lowagie.text.pdf.PdfPCell cUser = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(userNombre, bodyFont));
            cUser.setBackgroundColor(currentBg);
            cUser.setBorderColor(borderColor);
            cUser.setPadding(5f);
            table.addCell(cUser);

            // Observaciones
            String obs = d.getObservaciones() != null ? d.getObservaciones() : "";
            com.lowagie.text.pdf.PdfPCell cObs = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(obs, bodyFont));
            cObs.setBackgroundColor(currentBg);
            cObs.setBorderColor(borderColor);
            cObs.setPadding(5f);
            table.addCell(cObs);
        }

        // Summary Row at bottom of table
        com.lowagie.text.Font totalFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 8.5f, new java.awt.Color(15, 23, 42));

        com.lowagie.text.pdf.PdfPCell cTotalLbl = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase("TOTALES Y PROMEDIOS", totalFont));
        cTotalLbl.setColspan(4);
        cTotalLbl.setBackgroundColor(new java.awt.Color(241, 245, 249));
        cTotalLbl.setBorderColor(borderColor);
        cTotalLbl.setPadding(6f);
        cTotalLbl.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
        table.addCell(cTotalLbl);

        com.lowagie.text.pdf.PdfPCell cTotalPct = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(String.format("%.1f%%", promedioLlenado), totalFont));
        cTotalPct.setBackgroundColor(new java.awt.Color(241, 245, 249));
        cTotalPct.setBorderColor(borderColor);
        cTotalPct.setPadding(6f);
        cTotalPct.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
        table.addCell(cTotalPct);

        com.lowagie.text.pdf.PdfPCell cTotalKg = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(String.format("%.1f kg", totalKilos), totalFont));
        cTotalKg.setBackgroundColor(new java.awt.Color(241, 245, 249));
        cTotalKg.setBorderColor(borderColor);
        cTotalKg.setPadding(6f);
        cTotalKg.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
        table.addCell(cTotalKg);

        com.lowagie.text.pdf.PdfPCell cTotalEmpty = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase("", totalFont));
        cTotalEmpty.setColspan(2);
        cTotalEmpty.setBackgroundColor(new java.awt.Color(241, 245, 249));
        cTotalEmpty.setBorderColor(borderColor);
        cTotalEmpty.setPadding(6f);
        table.addCell(cTotalEmpty);

        document.add(table);

        document.close();
        return baos.toByteArray();
    }

    private com.lowagie.text.pdf.PdfPCell createKpiCell(String label, String value, com.lowagie.text.Font lblFont, com.lowagie.text.Font valFont) {
        com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell();
        cell.setBackgroundColor(new java.awt.Color(248, 250, 252));
        cell.setBorderColor(new java.awt.Color(226, 232, 240));
        cell.setPadding(8f);
        cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);

        com.lowagie.text.Paragraph pVal = new com.lowagie.text.Paragraph(value, valFont);
        pVal.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        com.lowagie.text.Paragraph pLbl = new com.lowagie.text.Paragraph(label, lblFont);
        pLbl.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);

        cell.addElement(pVal);
        cell.addElement(pLbl);
        return cell;
    }

    private static class HeaderFooterPageEvent extends com.lowagie.text.pdf.PdfPageEventHelper {
        private final com.lowagie.text.Font footerFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA, 8, new java.awt.Color(100, 116, 139));

        @Override
        public void onEndPage(com.lowagie.text.pdf.PdfWriter writer, com.lowagie.text.Document document) {
            com.lowagie.text.pdf.PdfContentByte cb = writer.getDirectContent();
            cb.saveState();

            float leftMargin = document.left();
            float rightMargin = document.right();
            float bottomMargin = document.bottom() - 10;

            cb.setLineWidth(0.5f);
            cb.setColorStroke(new java.awt.Color(226, 232, 240));
            cb.moveTo(leftMargin, bottomMargin + 12);
            cb.lineTo(rightMargin, bottomMargin + 12);
            cb.stroke();

            com.lowagie.text.pdf.ColumnText.showTextAligned(
                    cb, com.lowagie.text.Element.ALIGN_LEFT,
                    new com.lowagie.text.Phrase("Reciclaje Litoral • Sistema de Monitoreo de Vidrio Comunal", footerFont),
                    leftMargin, bottomMargin, 0
            );

            com.lowagie.text.pdf.ColumnText.showTextAligned(
                    cb, com.lowagie.text.Element.ALIGN_RIGHT,
                    new com.lowagie.text.Phrase("Página " + writer.getPageNumber(), footerFont),
                    rightMargin, bottomMargin, 0
            );

            cb.restoreState();
        }
    }

    public byte[] generatePdfReport(Long comunaId, Long usuarioId) throws Exception {
        return generatePdfReport(comunaId, usuarioId, null, null);
    }

    private byte[] getResizedThumbnailBytes(FotoInspeccion foto, String nombrePunto, int targetWidth, int targetHeight) {
        try {
            BufferedImage origImage = null;
            if (foto != null && foto.getUrlFoto() != null && !foto.getUrlFoto().trim().isEmpty()) {
                byte[] imageBytes = s3StorageService.obtenerBytesImagen(foto.getUrlFoto());
                if (imageBytes != null && imageBytes.length > 0) {
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
                        origImage = ImageIO.read(bais);
                    } catch (Exception e) {
                        log.warn("Error leyendo bytes de imagen de foto id {}: {}", foto.getId(), e.getMessage());
                    }
                }
            }

            if (origImage == null) {
                return new byte[0];
            }

            BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resized.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(origImage, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resized, "jpg", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.warn("No se pudo generar la miniatura de imagen: {}", e.getMessage());
            return new byte[0];
        }
    }
}
