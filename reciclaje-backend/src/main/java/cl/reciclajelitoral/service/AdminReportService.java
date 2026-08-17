package cl.reciclajelitoral.service;

import cl.reciclajelitoral.entity.DetalleInspeccion;
import cl.reciclajelitoral.entity.FotoInspeccion;
import cl.reciclajelitoral.repository.DetalleInspeccionRepository;
import cl.reciclajelitoral.repository.InspeccionSemanalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReportService {

    private static final ZoneId CHILE_ZONE = ZoneId.of("America/Santiago");

    private final DetalleInspeccionRepository detalleRepository;
    private final InspeccionSemanalRepository inspeccionSemanalRepository;
    private final S3StorageService s3StorageService;

    public int getEffectiveWeekNumber(DetalleInspeccion d) {
        LocalDateTime dt = getEffectiveLocalDateTime(d);
        if (dt == null) {
            return d.getInspeccionSemanal() != null ? d.getInspeccionSemanal().getSemanaNumero() : -1;
        }
        return cl.reciclajelitoral.util.WeekDateUtils.getWeekNumber(dt);
    }

    public int getEffectiveYear(DetalleInspeccion d) {
        LocalDateTime dt = getEffectiveLocalDateTime(d);
        if (dt == null) {
            return d.getInspeccionSemanal() != null ? d.getInspeccionSemanal().getAnio() : -1;
        }
        return cl.reciclajelitoral.util.WeekDateUtils.getYear(dt);
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
        int currentYear = java.time.LocalDate.now(CHILE_ZONE).getYear();
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

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte Consolidado");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Hyperlink Style
            CellStyle linkStyle = workbook.createCellStyle();
            Font linkFont = workbook.createFont();
            linkFont.setUnderline(Font.U_SINGLE);
            linkFont.setColor(IndexedColors.BLUE.getIndex());
            linkStyle.setFont(linkFont);

            Row header = sheet.createRow(0);
            String[] headers = {
                    "ID Detalle", "Semana / Año", "Comuna", "Contenedor / Punto",
                    "Categoría", "Porcentaje Llenado (%)", "Kilos Calculados", "Usuario / Inspector",
                    "Observaciones", "Foto Antes (Vista Previa)", "Enlaces S3 Foto Antes HD",
                    "Foto Después (Vista Previa)", "Enlaces S3 Foto Después HD"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
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
                        if (fotos.size() > 1) {
                            fotosDespues.add(fotos.get(1));
                        }
                    }
                }

                boolean hasPhoto = false;

                // 1. Foto(s) Antes
                if (!fotosAntes.isEmpty()) {
                    FotoInspeccion principalAntes = fotosAntes.get(0);
                    byte[] thumb1 = getResizedThumbnailBytes(principalAntes, nombrePunto, 120, 80);
                    if (thumb1.length > 0) {
                        int picIdx1 = workbook.addPicture(thumb1, Workbook.PICTURE_TYPE_JPEG);
                        ClientAnchor anchor1 = createHelper.createClientAnchor();
                        anchor1.setCol1(9);
                        anchor1.setRow1(rowIdx);
                        anchor1.setCol2(10);
                        anchor1.setRow2(rowIdx + 1);
                        drawing.createPicture(anchor1, picIdx1);
                        hasPhoto = true;
                    }

                    Cell link1Cell = row.createCell(10);
                    FotoInspeccion fPrimary = fotosAntes.get(0);
                    String freshUrl = s3StorageService.obtenerUrlFresca(fPrimary.getUrlFoto());
                    if (freshUrl != null && !freshUrl.isEmpty()) {
                        String label = fotosAntes.size() > 1 ? "Abrir Foto Antes 1 (" + fotosAntes.size() + " fotos) ↗" : "Abrir Foto Antes ↗";
                        link1Cell.setCellValue(label);
                        Hyperlink hLink = createHelper.createHyperlink(HyperlinkType.URL);
                        hLink.setAddress(freshUrl);
                        link1Cell.setHyperlink(hLink);
                        link1Cell.setCellStyle(linkStyle);
                    } else {
                        link1Cell.setCellValue("Sin URL");
                    }
                } else {
                    row.createCell(10).setCellValue("Sin foto");
                }

                // 2. Foto(s) Después
                if (!fotosDespues.isEmpty()) {
                    FotoInspeccion principalDespues = fotosDespues.get(0);
                    byte[] thumb2 = getResizedThumbnailBytes(principalDespues, nombrePunto, 120, 80);
                    if (thumb2.length > 0) {
                        int picIdx2 = workbook.addPicture(thumb2, Workbook.PICTURE_TYPE_JPEG);
                        ClientAnchor anchor2 = createHelper.createClientAnchor();
                        anchor2.setCol1(11);
                        anchor2.setRow1(rowIdx);
                        anchor2.setCol2(12);
                        anchor2.setRow2(rowIdx + 1);
                        drawing.createPicture(anchor2, picIdx2);
                        hasPhoto = true;
                    }

                    Cell link2Cell = row.createCell(12);
                    FotoInspeccion fPrimary = fotosDespues.get(0);
                    String freshUrl = s3StorageService.obtenerUrlFresca(fPrimary.getUrlFoto());
                    if (freshUrl != null && !freshUrl.isEmpty()) {
                        String label = fotosDespues.size() > 1 ? "Abrir Foto Después 1 (" + fotosDespues.size() + " fotos) ↗" : "Abrir Foto Después ↗";
                        link2Cell.setCellValue(label);
                        Hyperlink hLink = createHelper.createHyperlink(HyperlinkType.URL);
                        hLink.setAddress(freshUrl);
                        link2Cell.setHyperlink(hLink);
                        link2Cell.setCellStyle(linkStyle);
                    } else {
                        link2Cell.setCellValue("Sin URL");
                    }
                } else {
                    row.createCell(12).setCellValue("-");
                }

                if (hasPhoto) {
                    row.setHeightInPoints(65f);
                }

                rowIdx++;
            }

            for (int i = 0; i < headers.length; i++) {
                if (i == 9 || i == 11) {
                    sheet.setColumnWidth(i, 4800); // Columna de imagen
                } else if (i == 10 || i == 12) {
                    sheet.setColumnWidth(i, 7500); // Columna de enlaces S3
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
                String urlStr = foto.getUrlFoto().trim();
                if (urlStr.startsWith("data:image/")) {
                    try {
                        String base64Data = urlStr.substring(urlStr.indexOf(",") + 1);
                        byte[] rawBytes = Base64.getDecoder().decode(base64Data);
                        origImage = ImageIO.read(new ByteArrayInputStream(rawBytes));
                    } catch (Exception e) {
                        log.warn("Error leyendo Base64 de foto id {}: {}", foto.getId(), e.getMessage());
                    }
                } else if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
                    try {
                        java.net.URL url = new java.net.URI(urlStr).toURL();
                        java.net.URLConnection conn = url.openConnection();
                        conn.setConnectTimeout(5000);
                        conn.setReadTimeout(5000);
                        try (java.io.InputStream in = conn.getInputStream()) {
                            origImage = ImageIO.read(in);
                        }
                    } catch (Exception e) {
                        log.warn("Error descargando foto S3 id {} desde URL {}: {}", foto.getId(), urlStr, e.getMessage());
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
