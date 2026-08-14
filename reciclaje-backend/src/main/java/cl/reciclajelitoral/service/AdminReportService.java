package cl.reciclajelitoral.service;

import cl.reciclajelitoral.entity.DetalleInspeccion;
import cl.reciclajelitoral.entity.FotoInspeccion;
import cl.reciclajelitoral.repository.DetalleInspeccionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final DetalleInspeccionRepository detalleRepository;

    @Transactional(readOnly = true)
    public byte[] generateExcelZipReport(Long comunaId, Long usuarioId) throws IOException {
        List<DetalleInspeccion> detalles = detalleRepository.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getVisitado()))
                .filter(d -> comunaId == null || (d.getContenedor() != null && d.getContenedor().getComuna() != null && d.getContenedor().getComuna().getId().equals(comunaId)))
                .filter(d -> usuarioId == null || (d.getActualizadoPorUsuario() != null && d.getActualizadoPorUsuario().getId().equals(usuarioId)) || (d.getCreadoPorUsuario() != null && d.getCreadoPorUsuario().getId().equals(usuarioId)))
                .toList();

        ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
        Set<String> addedEntries = new HashSet<>();

        try (ZipOutputStream zos = new ZipOutputStream(zipBaos);
             Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Reporte Consolidado");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Hyperlink Style
            CellStyle linkStyle = workbook.createCellStyle();
            Font linkFont = workbook.createFont();
            linkFont.setUnderline(Font.U_SINGLE);
            linkFont.setColor(IndexedColors.BLUE.getIndex());
            linkStyle.setFont(linkFont);

            Row header = sheet.createRow(0);
            String[] headers = {"ID Detalle", "Comuna", "Contenedor / Punto", "Categoría", "Porcentaje Llenado (%)", "Kilos Calculados", "Usuario", "Observaciones", "Carpeta Fotos"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            CreationHelper createHelper = workbook.getCreationHelper();

            for (DetalleInspeccion d : detalles) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(d.getId());
                row.createCell(1).setCellValue(d.getContenedor() != null && d.getContenedor().getComuna() != null ? d.getContenedor().getComuna().getNombre() : "N/A");
                
                String nombrePunto = d.getContenedor() != null ? d.getContenedor().getNombrePunto() : "Contenedor " + d.getId();
                row.createCell(2).setCellValue(nombrePunto);
                row.createCell(3).setCellValue(d.getContenedor() != null && d.getContenedor().getCategoria() != null ? d.getContenedor().getCategoria().name() : "N/A");
                row.createCell(4).setCellValue(d.getPorcentajeEstimado() != null ? d.getPorcentajeEstimado().doubleValue() : 0.0);
                row.createCell(5).setCellValue(d.getKilosCalculados() != null ? d.getKilosCalculados().doubleValue() : 0.0);
                
                String userNombre = d.getActualizadoPorUsuario() != null ? d.getActualizadoPorUsuario().getNombre() :
                        (d.getCreadoPorUsuario() != null ? d.getCreadoPorUsuario().getNombre() : "Sistema");
                row.createCell(6).setCellValue(userNombre);
                row.createCell(7).setCellValue(d.getObservaciones() != null ? d.getObservaciones() : "");

                // Subcarpeta asociadas al contenedor
                Long contId = d.getContenedor() != null ? d.getContenedor().getId() : d.getId();
                String folderName = "fotos/contenedor_" + contId + "_" + nombrePunto.replaceAll("[^a-zA-Z0-9_-]", "_");
                
                Cell linkCell = row.createCell(8);
                linkCell.setCellValue("Abrir Fotos (" + folderName + ")");

                // POI Hyperlink FILE type to relative directory
                Hyperlink link = createHelper.createHyperlink(HyperlinkType.FILE);
                link.setAddress(folderName);
                linkCell.setHyperlink(link);
                linkCell.setCellStyle(linkStyle);

                // Agregar fotos a la subcarpeta del ZIP
                if (d.getFotos() != null && !d.getFotos().isEmpty()) {
                    int fotoCount = 1;
                    for (FotoInspeccion foto : d.getFotos()) {
                        String zipPath = folderName + "/foto_" + fotoCount++ + "_" + (foto.getMomento() != null ? foto.getMomento().name() : "INSPECCION") + ".jpg";
                        if (addedEntries.add(zipPath)) {
                            zos.putNextEntry(new ZipEntry(zipPath));
                            byte[] imgBytes = getImageBytes(foto, nombrePunto);
                            zos.write(imgBytes);
                            zos.closeEntry();
                        }
                    }
                } else {
                    String zipPath = folderName + "/info.txt";
                    if (addedEntries.add(zipPath)) {
                        zos.putNextEntry(new ZipEntry(zipPath));
                        zos.write(("Punto: " + nombrePunto + "\nSin fotos registradas en esta inspeccion.").getBytes(StandardCharsets.UTF_8));
                        zos.closeEntry();
                    }
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Escribir archivo Excel dentro del ZIP
            ByteArrayOutputStream excelBaos = new ByteArrayOutputStream();
            workbook.write(excelBaos);
            byte[] excelBytes = excelBaos.toByteArray();

            zos.putNextEntry(new ZipEntry("Reporte_Consolidado_Reciclaje.xlsx"));
            zos.write(excelBytes);
            zos.closeEntry();
            zos.finish();
        }

        return zipBaos.toByteArray();
    }

    private byte[] getImageBytes(FotoInspeccion foto, String nombrePunto) throws IOException {
        if (foto != null && foto.getUrlFoto() != null && foto.getUrlFoto().startsWith("data:image/")) {
            try {
                String base64Data = foto.getUrlFoto().substring(foto.getUrlFoto().indexOf(",") + 1);
                return Base64.getDecoder().decode(base64Data);
            } catch (Exception ignored) {}
        }

        // Crear una imagen JPEG valida con metadatos EXIF/headers graficos para visualizadores del SO
        BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(240, 248, 255));
        g.fillRect(0, 0, 800, 600);
        g.setColor(new Color(34, 139, 34));
        g.setFont(g.getFont().deriveFont(24.0f));
        g.drawString("Reciclaje Litoral - Registro Fotográfico", 50, 100);
        g.setColor(Color.BLACK);
        g.drawString("Punto: " + nombrePunto, 50, 180);
        g.drawString("Momento: " + (foto != null && foto.getMomento() != null ? foto.getMomento() : "REGISTRO"), 50, 240);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }
}
