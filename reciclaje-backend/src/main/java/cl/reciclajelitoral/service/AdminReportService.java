package cl.reciclajelitoral.service;

import cl.reciclajelitoral.entity.DetalleInspeccion;
import cl.reciclajelitoral.entity.FotoInspeccion;
import cl.reciclajelitoral.entity.Rol;
import cl.reciclajelitoral.entity.TipoRuta;
import cl.reciclajelitoral.entity.Usuario;
import cl.reciclajelitoral.repository.DetalleInspeccionRepository;
import cl.reciclajelitoral.repository.InspeccionSemanalRepository;
import cl.reciclajelitoral.util.WeekDateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
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

    public boolean isDetalleChofer(DetalleInspeccion d) {
        if (d.getInspeccionSemanal() != null && d.getInspeccionSemanal().getTipoRuta() == TipoRuta.CHOFER) {
            return true;
        }
        if (d.getCreadoPorUsuario() != null && d.getCreadoPorUsuario().getRol() == Rol.CHOFER) {
            return true;
        }
        if (d.getActualizadoPorUsuario() != null && d.getActualizadoPorUsuario().getRol() == Rol.CHOFER) {
            return true;
        }
        return false;
    }

    public boolean isDetalleInspectorOrChofer(DetalleInspeccion d) {
        if (isDetalleChofer(d)) {
            return true;
        }
        if (d.getCreadoPorUsuario() != null && d.getCreadoPorUsuario().getRol() == Rol.INSPECTOR) {
            return true;
        }
        if (d.getActualizadoPorUsuario() != null && d.getActualizadoPorUsuario().getRol() == Rol.INSPECTOR) {
            return true;
        }
        if (d.getInspeccionSemanal() != null && d.getInspeccionSemanal().getInspector() != null && d.getInspeccionSemanal().getInspector().getRol() == Rol.INSPECTOR) {
            return true;
        }
        if (d.getCreadoPorUsuario() != null && d.getCreadoPorUsuario().getRol() != Rol.ADMIN && d.getCreadoPorUsuario().getRol() != Rol.REPORTERIA) {
            return true;
        }
        if (d.getInspeccionSemanal() != null && (d.getInspeccionSemanal().getTipoRuta() == null || d.getInspeccionSemanal().getTipoRuta() == TipoRuta.INSPECTOR)) {
            return true;
        }
        return false;
    }

    public boolean matchesUsuario(DetalleInspeccion d, Long usuarioId) {
        if (usuarioId == null) return true;
        if (d.getActualizadoPorUsuario() != null && usuarioId.equals(d.getActualizadoPorUsuario().getId())) return true;
        if (d.getCreadoPorUsuario() != null && usuarioId.equals(d.getCreadoPorUsuario().getId())) return true;
        if (d.getInspeccionSemanal() != null && d.getInspeccionSemanal().getInspector() != null && usuarioId.equals(d.getInspeccionSemanal().getInspector().getId())) return true;
        return false;
    }

    public String getNombreActor(DetalleInspeccion d) {
        if (d.getCreadoPorUsuario() != null && (d.getCreadoPorUsuario().getRol() == Rol.INSPECTOR || d.getCreadoPorUsuario().getRol() == Rol.CHOFER)) {
            return d.getCreadoPorUsuario().getNombre();
        }
        if (d.getActualizadoPorUsuario() != null && (d.getActualizadoPorUsuario().getRol() == Rol.INSPECTOR || d.getActualizadoPorUsuario().getRol() == Rol.CHOFER)) {
            return d.getActualizadoPorUsuario().getNombre();
        }
        if (d.getInspeccionSemanal() != null && d.getInspeccionSemanal().getInspector() != null) {
            return d.getInspeccionSemanal().getInspector().getNombre();
        }
        if (d.getCreadoPorUsuario() != null) {
            return d.getCreadoPorUsuario().getNombre();
        }
        return "Sin Asignar";
    }

    public String getRolActor(DetalleInspeccion d) {
        return isDetalleChofer(d) ? "CHOFER" : "INSPECTOR";
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
                .filter(d -> usuarioId != null ? matchesUsuario(d, usuarioId) : isDetalleInspectorOrChofer(d))
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

        List<DetalleInspeccion> detallesInspector = detalles.stream()
                .filter(d -> !isDetalleChofer(d))
                .toList();

        List<DetalleInspeccion> detallesChofer = detalles.stream()
                .filter(this::isDetalleChofer)
                .toList();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte Consolidado");

            // Styles
            XSSFCellStyle bannerStyle = (XSSFCellStyle) workbook.createCellStyle();
            Font bannerFont = workbook.createFont();
            bannerFont.setBold(true);
            bannerFont.setFontHeightInPoints((short) 11);
            bannerFont.setColor(IndexedColors.WHITE.getIndex());
            bannerStyle.setFont(bannerFont);
            bannerStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(30, 41, 59), null)); // #1e293b Dark Navy Slate
            bannerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            bannerStyle.setAlignment(HorizontalAlignment.LEFT);
            bannerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(51, 65, 85), null)); // #334155 Slate
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

            XSSFCellStyle totalLabelStyle = (XSSFCellStyle) workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalLabelStyle.setFont(totalFont);
            totalLabelStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(241, 245, 249), null));
            totalLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalLabelStyle.setAlignment(HorizontalAlignment.RIGHT);
            totalLabelStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            XSSFCellStyle totalNumStyle = (XSSFCellStyle) workbook.createCellStyle();
            totalNumStyle.setFont(totalFont);
            totalNumStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(241, 245, 249), null));
            totalNumStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalNumStyle.setAlignment(HorizontalAlignment.RIGHT);
            totalNumStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            XSSFCellStyle totalEmptyStyle = (XSSFCellStyle) workbook.createCellStyle();
            totalEmptyStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(241, 245, 249), null));
            totalEmptyStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Subtle background styles for Column F (% Llenado)
            Font pctFont = workbook.createFont();
            pctFont.setBold(true);

            XSSFCellStyle styleGreen = (XSSFCellStyle) workbook.createCellStyle();
            styleGreen.setFont(pctFont);
            styleGreen.setFillForegroundColor(new XSSFColor(new java.awt.Color(220, 252, 231), null));
            styleGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleGreen.setAlignment(HorizontalAlignment.RIGHT);
            styleGreen.setVerticalAlignment(VerticalAlignment.CENTER);

            XSSFCellStyle styleYellow = (XSSFCellStyle) workbook.createCellStyle();
            styleYellow.setFont(pctFont);
            styleYellow.setFillForegroundColor(new XSSFColor(new java.awt.Color(254, 249, 195), null));
            styleYellow.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleYellow.setAlignment(HorizontalAlignment.RIGHT);
            styleYellow.setVerticalAlignment(VerticalAlignment.CENTER);

            XSSFCellStyle styleRed = (XSSFCellStyle) workbook.createCellStyle();
            styleRed.setFont(pctFont);
            styleRed.setFillForegroundColor(new XSSFColor(new java.awt.Color(254, 226, 226), null));
            styleRed.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleRed.setAlignment(HorizontalAlignment.RIGHT);
            styleRed.setVerticalAlignment(VerticalAlignment.CENTER);

            CreationHelper createHelper = workbook.getCreationHelper();
            Drawing<?> drawing = sheet.createDrawingPatriarch();

            List<String> headersInspector = getExcelHeaders(true, maxFotosAntes, maxFotosDespues);
            List<String> headersChofer = getExcelHeaders(false, maxFotosAntes, maxFotosDespues);

            int rowIdx = 0;
            if (!detallesInspector.isEmpty()) {
                rowIdx = renderExcelSection(sheet, createHelper, drawing, workbook,
                        "🏛️ SECCIÓN 1: INSPECCIONES DE RUTA - INSPECTORES",
                        detallesInspector, true, maxFotosAntes, maxFotosDespues, headersInspector,
                        bannerStyle, headerStyle, totalLabelStyle, totalNumStyle, totalEmptyStyle,
                        linkStyle, styleGreen, styleYellow, styleRed, rowIdx);
            }

            if (!detallesChofer.isEmpty()) {
                rowIdx = renderExcelSection(sheet, createHelper, drawing, workbook,
                        "🚚 SECCIÓN 2: RETIROS Y RECOLECCIÓN - CHOFERES",
                        detallesChofer, false, maxFotosAntes, maxFotosDespues, headersChofer,
                        bannerStyle, headerStyle, totalLabelStyle, totalNumStyle, totalEmptyStyle,
                        linkStyle, styleGreen, styleYellow, styleRed, rowIdx);
            }

            if (detalles.isEmpty()) {
                Row emptyRow = sheet.createRow(0);
                emptyRow.createCell(0).setCellValue("No se encontraron registros para los filtros seleccionados.");
            }

            int totalCols = Math.max(headersInspector.size(), headersChofer.size());
            sheet.setColumnWidth(0, 3000); // ID Detalle
            sheet.setColumnWidth(1, 4500); // Semana / Año
            sheet.setColumnWidth(2, 5000); // Comuna
            sheet.setColumnWidth(3, 7500); // Contenedor / Punto
            sheet.setColumnWidth(4, 4000); // Categoría
            sheet.setColumnWidth(5, 4500); // Porcentaje Llenado (%)
            sheet.setColumnWidth(6, 4500); // Kilos Acumulados / Retirados
            sheet.setColumnWidth(7, 6500); // Inspector / Chofer
            sheet.setColumnWidth(8, 7500); // Observaciones
            for (int i = 9; i < totalCols; i++) {
                int offset = i - 9;
                if (offset % 2 == 0) {
                    sheet.setColumnWidth(i, 4500); // Columna miniatura
                } else {
                    sheet.setColumnWidth(i, 7500); // Columna enlace HD
                }
            }

            ByteArrayOutputStream excelBaos = new ByteArrayOutputStream();
            workbook.write(excelBaos);
            return excelBaos.toByteArray();
        }
    }

    private List<String> getExcelHeaders(boolean isInspector, int maxAntes, int maxDesp) {
        List<String> headersList = new ArrayList<>(List.of(
                "ID Detalle", "Semana / Año", "Comuna", "Contenedor / Punto",
                "Categoría", "Porcentaje Llenado (%)",
                isInspector ? "Kilos Acumulados" : "Kilos Retirados",
                isInspector ? "Inspector" : "Chofer",
                "Observaciones"
        ));

        for (int i = 1; i <= maxAntes; i++) {
            headersList.add(maxAntes == 1 ? "Foto Antes (Vista Previa)" : "Foto Antes " + i + " (Vista Previa)");
            headersList.add(maxAntes == 1 ? "Enlace S3 Foto Antes (HD ↗)" : "Enlace S3 Foto Antes " + i + " (HD ↗)");
        }

        for (int i = 1; i <= maxDesp; i++) {
            headersList.add(maxDesp == 1 ? "Foto Después (Vista Previa)" : "Foto Después " + i + " (Vista Previa)");
            headersList.add(maxDesp == 1 ? "Enlace S3 Foto Después " + i + " (HD ↗)" : "Enlace S3 Foto Después " + i + " (HD ↗)");
        }
        return headersList;
    }

    private int renderExcelSection(Sheet sheet,
                                   CreationHelper createHelper,
                                   Drawing<?> drawing,
                                   Workbook workbook,
                                   String sectionTitle,
                                   List<DetalleInspeccion> detallesList,
                                   boolean isInspector,
                                   int maxFotosAntes,
                                   int maxFotosDespues,
                                   List<String> headersList,
                                   CellStyle bannerStyle,
                                   CellStyle headerStyle,
                                   CellStyle totalLabelStyle,
                                   CellStyle totalNumStyle,
                                   CellStyle totalEmptyStyle,
                                   CellStyle linkStyle,
                                   CellStyle styleGreen,
                                   CellStyle styleYellow,
                                   CellStyle styleRed,
                                   int startRow) {
        if (detallesList.isEmpty()) {
            return startRow;
        }

        int rowIdx = startRow;

        // 1. Banner Row
        Row bannerRow = sheet.createRow(rowIdx++);
        bannerRow.setHeightInPoints(26f);
        Cell bannerCell = bannerRow.createCell(0);
        bannerCell.setCellValue(sectionTitle);
        bannerCell.setCellStyle(bannerStyle);
        for (int c = 1; c < headersList.size(); c++) {
            Cell emptyBanner = bannerRow.createCell(c);
            emptyBanner.setCellStyle(bannerStyle);
        }
        sheet.addMergedRegion(new CellRangeAddress(bannerRow.getRowNum(), bannerRow.getRowNum(), 0, headersList.size() - 1));

        // 2. Header Row
        Row headerRow = sheet.createRow(rowIdx++);
        headerRow.setHeightInPoints(24f);
        for (int i = 0; i < headersList.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headersList.get(i));
            cell.setCellStyle(headerStyle);
        }

        // 3. Data Rows
        double sumKg = 0;
        double sumPct = 0;

        for (DetalleInspeccion d : detallesList) {
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

            double pctVal = d.getPorcentajeEstimado() != null ? d.getPorcentajeEstimado().doubleValue() : 0.0;
            sumPct += pctVal;
            Cell pctCell = row.createCell(5);
            pctCell.setCellValue(pctVal);

            if (pctVal >= 90.0) {
                pctCell.setCellStyle(styleRed);
            } else if (pctVal >= 50.0) {
                pctCell.setCellStyle(styleYellow);
            } else {
                pctCell.setCellStyle(styleGreen);
            }

            // Kilos
            double kgVal;
            if (isInspector) {
                kgVal = d.getKilosCalculados() != null ? d.getKilosCalculados().doubleValue() : 0.0;
            } else {
                kgVal = d.getKilosRetirados() != null ? d.getKilosRetirados().doubleValue() :
                        (d.getKilosCalculados() != null ? d.getKilosCalculados().doubleValue() : 0.0);
            }
            sumKg += kgVal;
            row.createCell(6).setCellValue(kgVal);

            // Actor
            row.createCell(7).setCellValue(getNombreActor(d));

            // Observaciones
            row.createCell(8).setCellValue(d.getObservaciones() != null ? d.getObservaciones() : "");

            // Fotos
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

            // Fotos ANTES
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
                        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                        drawing.createPicture(anchor, picIdx);
                        hasPhoto = true;
                    } else {
                        thumbCell.setCellValue("[Sin imagen]");
                    }

                    if (freshUrl != null && !freshUrl.trim().isEmpty()) {
                        Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
                        link.setAddress(freshUrl);
                        linkCell.setCellValue("Ver Foto HD ↗");
                        linkCell.setHyperlink(link);
                        linkCell.setCellStyle(linkStyle);
                    } else {
                        linkCell.setCellValue("Sin URL");
                    }
                } else {
                    thumbCell.setCellValue("-");
                    linkCell.setCellValue("-");
                }
            }

            // Fotos DESPUÉS
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
                        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                        drawing.createPicture(anchor, picIdx);
                        hasPhoto = true;
                    } else {
                        thumbCell.setCellValue("[Sin imagen]");
                    }

                    if (freshUrl != null && !freshUrl.trim().isEmpty()) {
                        Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
                        link.setAddress(freshUrl);
                        linkCell.setCellValue("Ver Foto HD ↗");
                        linkCell.setHyperlink(link);
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

        // 4. Totals Row
        double avgPct = detallesList.isEmpty() ? 0 : (sumPct / detallesList.size());
        Row totRow = sheet.createRow(rowIdx++);
        totRow.setHeightInPoints(22f);
        Cell totLbl = totRow.createCell(0);
        totLbl.setCellValue("TOTAL " + (isInspector ? "INSPECTORES" : "CHOFERES"));
        totLbl.setCellStyle(totalLabelStyle);
        for (int c = 1; c <= 4; c++) {
            Cell emptyTot = totRow.createCell(c);
            emptyTot.setCellStyle(totalLabelStyle);
        }
        sheet.addMergedRegion(new CellRangeAddress(totRow.getRowNum(), totRow.getRowNum(), 0, 4));

        Cell pctTot = totRow.createCell(5);
        pctTot.setCellValue(String.format("%.1f%%", avgPct));
        pctTot.setCellStyle(totalNumStyle);

        Cell kgTot = totRow.createCell(6);
        kgTot.setCellValue(sumKg);
        kgTot.setCellStyle(totalNumStyle);

        for (int c = 7; c < headersList.size(); c++) {
            Cell emptyTot = totRow.createCell(c);
            emptyTot.setCellStyle(totalEmptyStyle);
        }

        // Empty row of spacing
        Row spaceRow = sheet.createRow(rowIdx++);
        spaceRow.setHeightInPoints(15f);

        return rowIdx;
    }

    public byte[] generateExcelZipReport(Long comunaId, Long usuarioId) throws IOException {
        return generateExcelReport(comunaId, usuarioId, null, null);
    }

    @Transactional(readOnly = true)
    public byte[] generatePdfReport(Long comunaId, Long usuarioId, Integer semanaNumero, Integer anio) throws Exception {
        List<DetalleInspeccion> detalles = detalleRepository.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getVisitado()))
                .filter(d -> comunaId == null || (d.getContenedor() != null && d.getContenedor().getComuna() != null && d.getContenedor().getComuna().getId().equals(comunaId)))
                .filter(d -> usuarioId != null ? matchesUsuario(d, usuarioId) : isDetalleInspectorOrChofer(d))
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
        if (usuarioId != null) {
            String actorFiltro = detalles.stream()
                    .filter(d -> matchesUsuario(d, usuarioId))
                    .findFirst()
                    .map(d -> getNombreActor(d) + " (" + getRolActor(d) + ")")
                    .orElse("Usuario #" + usuarioId);
            filtroTexto += " | Inspector / Chofer: " + actorFiltro;
        } else {
            filtroTexto += " | Todos los Inspectores y Choferes";
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

        // Separate into Inspector and Chofer details
        List<DetalleInspeccion> detallesInspector = detalles.stream()
                .filter(d -> !isDetalleChofer(d))
                .toList();

        List<DetalleInspeccion> detallesChofer = detalles.stream()
                .filter(this::isDetalleChofer)
                .toList();

        double totalKilosAcumulados = detallesInspector.stream()
                .mapToDouble(d -> d.getKilosCalculados() != null ? d.getKilosCalculados().doubleValue() : 0.0)
                .sum();

        double totalKilosRetirados = detallesChofer.stream()
                .mapToDouble(d -> {
                    if (d.getKilosRetirados() != null) return d.getKilosRetirados().doubleValue();
                    return d.getKilosCalculados() != null ? d.getKilosCalculados().doubleValue() : 0.0;
                })
                .sum();

        double sumPorcInspector = detallesInspector.stream()
                .mapToDouble(d -> d.getPorcentajeEstimado() != null ? d.getPorcentajeEstimado().doubleValue() : 0.0)
                .sum();
        double promedioAcumulados = !detallesInspector.isEmpty() ? (sumPorcInspector / detallesInspector.size()) : 0.0;

        double sumPorcChofer = detallesChofer.stream()
                .mapToDouble(d -> d.getPorcentajeEstimado() != null ? d.getPorcentajeEstimado().doubleValue() : 0.0)
                .sum();
        double promedioRetirados = !detallesChofer.isEmpty() ? (sumPorcChofer / detallesChofer.size()) : 0.0;

        // KPI Summary Box (5 columns)
        com.lowagie.text.pdf.PdfPTable kpiTable = new com.lowagie.text.pdf.PdfPTable(5);
        kpiTable.setWidthPercentage(100);
        kpiTable.setWidths(new float[]{1f, 1f, 1f, 1f, 1f});
        kpiTable.setSpacingAfter(10f);

        com.lowagie.text.Font kpiValFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 12, new java.awt.Color(5, 150, 105));
        com.lowagie.text.Font kpiLblFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA, 8, new java.awt.Color(100, 116, 139));

        kpiTable.addCell(createKpiCell("Total Puntos Inspeccionados", String.valueOf(detalles.size()), kpiLblFont, kpiValFont));
        kpiTable.addCell(createKpiCell("Total Kilos Acumulados", String.format("%.1f kg", totalKilosAcumulados), kpiLblFont, kpiValFont));
        kpiTable.addCell(createKpiCell("Total Kilos Retirados", String.format("%.1f kg", totalKilosRetirados), kpiLblFont, kpiValFont));
        kpiTable.addCell(createKpiCell("Promedio Acumulados", String.format("%.1f%%", promedioAcumulados), kpiLblFont, kpiValFont));
        kpiTable.addCell(createKpiCell("Promedio Retirados", String.format("%.1f%%", promedioRetirados), kpiLblFont, kpiValFont));
        document.add(kpiTable);

        com.lowagie.text.Font sectionTitleFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 10f, new java.awt.Color(30, 41, 59));
        com.lowagie.text.Font headFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 8.5f, java.awt.Color.WHITE);
        com.lowagie.text.Font bodyFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA, 8, new java.awt.Color(51, 65, 85));
        com.lowagie.text.Font bodyFontBold = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 8, new java.awt.Color(15, 23, 42));
        com.lowagie.text.Font catFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 7.5f, new java.awt.Color(2, 132, 199));
        com.lowagie.text.Font totalFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 8.5f, new java.awt.Color(15, 23, 42));

        java.awt.Color lightBg = new java.awt.Color(248, 250, 252);
        java.awt.Color whiteBg = java.awt.Color.WHITE;
        java.awt.Color borderColor = new java.awt.Color(226, 232, 240);

        if (!detallesInspector.isEmpty()) {
            addPdfSection(document, "🏛️ SECCIÓN 1: INSPECCIONES DE RUTA - INSPECTORES", detallesInspector, true,
                    sectionTitleFont, headFont, bodyFont, bodyFontBold, catFont, totalFont, lightBg, whiteBg, borderColor);
        }

        if (!detallesChofer.isEmpty()) {
            addPdfSection(document, "🚚 SECCIÓN 2: RETIROS Y RECOLECCIÓN - CHOFERES", detallesChofer, false,
                    sectionTitleFont, headFont, bodyFont, bodyFontBold, catFont, totalFont, lightBg, whiteBg, borderColor);
        }

        if (detalles.isEmpty()) {
            com.lowagie.text.Paragraph pEmpty = new com.lowagie.text.Paragraph("No se encontraron registros para los filtros seleccionados.", bodyFont);
            pEmpty.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            pEmpty.setSpacingBefore(20f);
            document.add(pEmpty);
        }

        document.close();
        return baos.toByteArray();
    }

    private void addPdfSection(com.lowagie.text.Document document,
                               String sectionTitle,
                               List<DetalleInspeccion> detallesList,
                               boolean isInspector,
                               com.lowagie.text.Font sectionTitleFont,
                               com.lowagie.text.Font headFont,
                               com.lowagie.text.Font bodyFont,
                               com.lowagie.text.Font bodyFontBold,
                               com.lowagie.text.Font catFont,
                               com.lowagie.text.Font totalFont,
                               java.awt.Color lightBg,
                               java.awt.Color whiteBg,
                               java.awt.Color borderColor) throws com.lowagie.text.DocumentException {
        if (detallesList.isEmpty()) return;

        com.lowagie.text.Paragraph pSec = new com.lowagie.text.Paragraph(sectionTitle, sectionTitleFont);
        pSec.setSpacingBefore(10f);
        pSec.setSpacingAfter(5f);
        document.add(pSec);

        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.0f, 2.2f, 3.8f, 1.8f, 1.6f, 2.2f, 3.0f, 3.4f});

        String[] headers = {
                "ID", "Comuna", "Punto Limpio", "Categoría", "% Llenado",
                isInspector ? "Kilos Acumulados" : "Kilos Retirados",
                isInspector ? "Inspector" : "Chofer",
                "Observaciones"
        };

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

        double sumKg = 0;
        double sumPct = 0;
        int rowIndex = 0;

        for (DetalleInspeccion d : detallesList) {
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
            double pctVal = d.getPorcentajeEstimado() != null ? d.getPorcentajeEstimado().doubleValue() : 0.0;
            sumPct += pctVal;
            String pctStr = String.format("%.0f%%", pctVal);
            com.lowagie.text.pdf.PdfPCell cPct = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(pctStr, bodyFontBold));
            cPct.setBackgroundColor(currentBg);
            cPct.setBorderColor(borderColor);
            cPct.setPadding(5f);
            cPct.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
            table.addCell(cPct);

            // Kilos
            double kgVal;
            if (isInspector) {
                kgVal = d.getKilosCalculados() != null ? d.getKilosCalculados().doubleValue() : 0.0;
            } else {
                kgVal = d.getKilosRetirados() != null ? d.getKilosRetirados().doubleValue() :
                        (d.getKilosCalculados() != null ? d.getKilosCalculados().doubleValue() : 0.0);
            }
            sumKg += kgVal;
            com.lowagie.text.pdf.PdfPCell cKg = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(String.format("%.1f", kgVal), bodyFontBold));
            cKg.setBackgroundColor(currentBg);
            cKg.setBorderColor(borderColor);
            cKg.setPadding(5f);
            cKg.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
            table.addCell(cKg);

            // Actor
            String actorStr = getNombreActor(d);
            com.lowagie.text.pdf.PdfPCell cUser = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(actorStr, bodyFont));
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

        // Summary Row
        double avgPct = detallesList.isEmpty() ? 0 : (sumPct / detallesList.size());
        com.lowagie.text.pdf.PdfPCell cTotalLbl = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase("TOTAL " + (isInspector ? "INSPECTORES" : "CHOFERES"), totalFont));
        cTotalLbl.setColspan(4);
        cTotalLbl.setBackgroundColor(new java.awt.Color(241, 245, 249));
        cTotalLbl.setBorderColor(borderColor);
        cTotalLbl.setPadding(6f);
        cTotalLbl.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
        table.addCell(cTotalLbl);

        com.lowagie.text.pdf.PdfPCell cTotalPct = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(String.format("%.1f%%", avgPct), totalFont));
        cTotalPct.setBackgroundColor(new java.awt.Color(241, 245, 249));
        cTotalPct.setBorderColor(borderColor);
        cTotalPct.setPadding(6f);
        cTotalPct.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
        table.addCell(cTotalPct);

        com.lowagie.text.pdf.PdfPCell cTotalKg = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(String.format("%.1f kg", sumKg), totalFont));
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
