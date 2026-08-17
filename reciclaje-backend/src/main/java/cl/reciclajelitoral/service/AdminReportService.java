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
                .filter(d -> usuarioId == null || (d.getActualizadoPorUsuario() != null && d.getActualizadoPorUsuario().getId().equals(usuarioId)) || (d.getCreadoPorUsuario() != null && d.getCreadoPorUsuario().getId().equals(usuarioId)))
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
                .filter(d -> usuarioId == null || (d.getActualizadoPorUsuario() != null && d.getActualizadoPorUsuario().getId().equals(usuarioId)) || (d.getCreadoPorUsuario() != null && d.getCreadoPorUsuario().getId().equals(usuarioId)))
                .filter(d -> semanaNumero == null || semanaNumero.equals(getEffectiveWeekNumber(d)))
                .filter(d -> anio == null || anio.equals(getEffectiveYear(d)))
                .toList();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        com.lowagie.text.Document document = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4.rotate());
        com.lowagie.text.pdf.PdfWriter.getInstance(document, baos);

        document.open();

        // Título del PDF
        com.lowagie.text.Font titleFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 18, java.awt.Color.BLUE);
        com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph("♻️ Reciclaje Litoral - Reporte Consolidado", titleFont);
        title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        document.add(title);

        String filtroSemana = (semanaNumero != null && anio != null) ? " | Semana " + semanaNumero + " (" + anio + ")" : "";
        com.lowagie.text.Font subtitleFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA, 10, java.awt.Color.GRAY);
        com.lowagie.text.Paragraph subtitle = new com.lowagie.text.Paragraph("Generado el: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + filtroSemana + " | Sistema de Monitoreo", subtitleFont);
        subtitle.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(15f);
        document.add(subtitle);

        // Tabla de Detalle
        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.2f, 2.5f, 3.5f, 2.0f, 2.0f, 2.2f, 2.5f, 4.0f});

        com.lowagie.text.Font headFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 9, java.awt.Color.WHITE);
        String[] headers = {"ID", "Comuna", "Punto Limpio", "Categoría", "% Llenado", "Kilos (kg)", "Inspector", "Observaciones"};

        for (String h : headers) {
            com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(h, headFont));
            cell.setBackgroundColor(new java.awt.Color(30, 144, 255));
            cell.setPadding(6f);
            cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        com.lowagie.text.Font bodyFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA, 8, java.awt.Color.BLACK);
        double totalKilos = 0;

        for (DetalleInspeccion d : detalles) {
            table.addCell(new com.lowagie.text.Phrase(String.valueOf(d.getId()), bodyFont));
            table.addCell(new com.lowagie.text.Phrase(d.getContenedor() != null && d.getContenedor().getComuna() != null ? d.getContenedor().getComuna().getNombre() : "N/A", bodyFont));
            table.addCell(new com.lowagie.text.Phrase(d.getContenedor() != null ? d.getContenedor().getNombrePunto() : "-", bodyFont));
            table.addCell(new com.lowagie.text.Phrase(d.getContenedor() != null && d.getContenedor().getCategoria() != null ? d.getContenedor().getCategoria().name() : "-", bodyFont));
            table.addCell(new com.lowagie.text.Phrase((d.getPorcentajeEstimado() != null ? d.getPorcentajeEstimado() : "0") + "%", bodyFont));

            double kg = d.getKilosCalculados() != null ? d.getKilosCalculados().doubleValue() : 0.0;
            totalKilos += kg;
            table.addCell(new com.lowagie.text.Phrase(String.format("%.1f", kg), bodyFont));

            String userNombre = d.getActualizadoPorUsuario() != null ? d.getActualizadoPorUsuario().getNombre() :
                    (d.getCreadoPorUsuario() != null ? d.getCreadoPorUsuario().getNombre() : "Sistema");
            table.addCell(new com.lowagie.text.Phrase(userNombre, bodyFont));
            table.addCell(new com.lowagie.text.Phrase(d.getObservaciones() != null ? d.getObservaciones() : "", bodyFont));
        }

        document.add(table);

        // Resumen Final
        com.lowagie.text.Paragraph summary = new com.lowagie.text.Paragraph(
                "\nTotal Registros: " + detalles.size() + " | Total Kilos Recolectados: " + String.format("%.1f kg", totalKilos),
                com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 10, java.awt.Color.DARK_GRAY)
        );
        summary.setAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
        document.add(summary);

        document.close();
        return baos.toByteArray();
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
