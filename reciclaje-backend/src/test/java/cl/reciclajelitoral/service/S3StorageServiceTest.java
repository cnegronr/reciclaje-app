package cl.reciclajelitoral.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class S3StorageServiceTest {

    private S3StorageService s3StorageService;

    @BeforeEach
    void setUp() {
        s3StorageService = new S3StorageService();
        ReflectionTestUtils.setField(s3StorageService, "bucketName", "reciclaje-litoral-fotos-prod");
        ReflectionTestUtils.setField(s3StorageService, "region", "us-east-1");
    }

    @Test
    @DisplayName("Debe retornar null cuando el photoData sea nulo o vacio")
    void subirFotoAS3NullOVacia() {
        assertNull(s3StorageService.subirFotoAS3(null, "inicial"));
        assertNull(s3StorageService.subirFotoAS3("", "inicial"));
        assertNull(s3StorageService.subirFotoAS3("   ", "inicial"));
    }

    @Test
    @DisplayName("Debe retornar la misma URL si photoData ya es una URL HTTP o HTTPS")
    void subirFotoAS3UrlExistente() {
        String urlHttp = "https://reciclaje-litoral-fotos-prod.s3.us-east-1.amazonaws.com/inspecciones/foto.jpg";
        assertEquals(urlHttp, s3StorageService.subirFotoAS3(urlHttp, "inicial"));

        String urlHttp2 = "http://reciclaje-litoral-fotos-prod.s3.us-east-1.amazonaws.com/inspecciones/foto2.jpg";
        assertEquals(urlHttp2, s3StorageService.subirFotoAS3(urlHttp2, "inicial"));
    }

    @Test
    @DisplayName("Debe procesar contenido Base64 en modo simulado retornando el Data URL directamente sin Access Denied")
    void subirFotoAS3Base64Simulado() {
        ReflectionTestUtils.setField(s3StorageService, "accessKeyId", "");
        ReflectionTestUtils.setField(s3StorageService, "secretAccessKey", "");
        ReflectionTestUtils.setField(s3StorageService, "awsProfile", "");

        assertFalse(s3StorageService.tieneCredencialesValidas());

        String base64Image = crearImagenBase64DePrueba(1600, 1200);
        String resultado = s3StorageService.subirFotoAS3(base64Image, "inicial_antes");

        assertNotNull(resultado);
        assertEquals(base64Image, resultado);
    }

    @Test
    @DisplayName("Debe ejecutar exitosamente subirFotoAS3 con S3Client mockeado")
    void subirFotoAS3ExitosoMockS3Client() {
        S3StorageService serviceSpy = spy(s3StorageService);
        ReflectionTestUtils.setField(serviceSpy, "accessKeyId", "AKIAIOSFODNN7EXAMPLE");
        ReflectionTestUtils.setField(serviceSpy, "secretAccessKey", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");

        S3Client mockS3Client = mock(S3Client.class);
        doReturn(mockS3Client).when(serviceSpy).crearS3Client();
        when(mockS3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String base64Image = crearImagenBase64DePrueba(800, 600);
        String resultado = serviceSpy.subirFotoAS3(base64Image, "test_exitoso");

        assertNotNull(resultado);
        assertTrue(resultado.contains("reciclaje-litoral-fotos-prod"));
        verify(mockS3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(mockS3Client).close();
    }

    @Test
    @DisplayName("Debe capturar excepciones al procesar Base64 invalido o cuando falle S3")
    void subirFotoAS3ExcepcionBase64Invalido() {
        ReflectionTestUtils.setField(s3StorageService, "accessKeyId", "AKIAIOSFODNN7EXAMPLE");
        ReflectionTestUtils.setField(s3StorageService, "secretAccessKey", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");

        String invalidBase64 = "%%%_NOT_VALID_BASE64_!!!";
        String resultado = s3StorageService.subirFotoAS3(invalidBase64, "error_test");

        assertNotNull(resultado);
        assertEquals(invalidBase64, resultado);
    }

    @Test
    @DisplayName("Debe generar Presigned URL exitosamente con S3Presigner")
    void generarPresignedUrlExitoso() {
        ReflectionTestUtils.setField(s3StorageService, "accessKeyId", "AKIAIOSFODNN7EXAMPLE");
        ReflectionTestUtils.setField(s3StorageService, "secretAccessKey", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");

        String presignedUrl = s3StorageService.generarPresignedUrl("inspecciones/test_foto.jpg");
        assertNotNull(presignedUrl);
        assertTrue(presignedUrl.contains("reciclaje-litoral-fotos-prod"));
        assertTrue(presignedUrl.contains("X-Amz-Algorithm="));
    }

    @Test
    @DisplayName("Debe compresión e imagen nula o no valida retornar rawBytes")
    void compresionarImagenCasosBorde() {
        assertNull(s3StorageService.compresionarImagen(null));

        byte[] vacio = new byte[0];
        assertArrayEquals(vacio, s3StorageService.compresionarImagen(vacio));

        byte[] noImagen = new byte[]{1, 2, 3, 4, 5};
        assertArrayEquals(noImagen, s3StorageService.compresionarImagen(noImagen));

        byte[] truncadoJpeg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xC0, 0x00, 0x02, 0x00, 0x00};
        assertArrayEquals(truncadoJpeg, s3StorageService.compresionarImagen(truncadoJpeg));
    }

    @Test
    @DisplayName("Debe redimensionar imagen grande apaisada (width > height)")
    void compresionarImagenGrandeApaisada() throws Exception {
        byte[] bytesOriginales = crearImagenByteDePrueba(2000, 1500);
        byte[] bytesCompresos = s3StorageService.compresionarImagen(bytesOriginales);

        assertNotNull(bytesCompresos);

        BufferedImage imgCompresa = ImageIO.read(new ByteArrayInputStream(bytesCompresos));
        assertNotNull(imgCompresa);
        assertEquals(1280, imgCompresa.getWidth());
        assertEquals(960, imgCompresa.getHeight());
    }

    @Test
    @DisplayName("Debe redimensionar imagen grande vertical (height > width)")
    void compresionarImagenGrandeVertical() throws Exception {
        byte[] bytesOriginales = crearImagenByteDePrueba(1500, 2000);
        byte[] bytesCompresos = s3StorageService.compresionarImagen(bytesOriginales);

        assertNotNull(bytesCompresos);

        BufferedImage imgCompresa = ImageIO.read(new ByteArrayInputStream(bytesCompresos));
        assertNotNull(imgCompresa);
        assertEquals(960, imgCompresa.getWidth());
        assertEquals(1280, imgCompresa.getHeight());
    }

    @Test
    @DisplayName("Debe redimensionar imagen grande cuadrada (width == height)")
    void compresionarImagenGrandeCuadrada() throws Exception {
        byte[] bytesOriginales = crearImagenByteDePrueba(1600, 1600);
        byte[] bytesCompresos = s3StorageService.compresionarImagen(bytesOriginales);

        assertNotNull(bytesCompresos);

        BufferedImage imgCompresa = ImageIO.read(new ByteArrayInputStream(bytesCompresos));
        assertNotNull(imgCompresa);
        assertEquals(1280, imgCompresa.getWidth());
        assertEquals(1280, imgCompresa.getHeight());
    }

    @Test
    @DisplayName("Debe comprimir imagen pequeña sin modificar sus dimensiones")
    void compresionarImagenPequena() throws Exception {
        byte[] bytesOriginales = crearImagenByteDePrueba(400, 300);
        byte[] bytesCompresos = s3StorageService.compresionarImagen(bytesOriginales);

        assertNotNull(bytesCompresos);

        BufferedImage imgCompresa = ImageIO.read(new ByteArrayInputStream(bytesCompresos));
        assertNotNull(imgCompresa);
        assertEquals(400, imgCompresa.getWidth());
        assertEquals(300, imgCompresa.getHeight());
    }

    @Test
    @DisplayName("Debe evaluar todas las combinaciones posibles de tieneCredencialesValidas, crearS3Client y crearS3Presigner")
    void evaluarCombinacionesCredenciales() {
        // 1. Ambas llaves presentes
        ReflectionTestUtils.setField(s3StorageService, "accessKeyId", "key");
        ReflectionTestUtils.setField(s3StorageService, "secretAccessKey", "secret");
        ReflectionTestUtils.setField(s3StorageService, "awsProfile", "");
        assertTrue(s3StorageService.tieneCredencialesValidas());
        assertNotNull(s3StorageService.crearS3Client());
        assertNotNull(s3StorageService.crearS3Presigner());

        // 2. Solo accessKeyId
        ReflectionTestUtils.setField(s3StorageService, "accessKeyId", "key");
        ReflectionTestUtils.setField(s3StorageService, "secretAccessKey", "");
        ReflectionTestUtils.setField(s3StorageService, "awsProfile", "");
        assertFalse(s3StorageService.tieneCredencialesValidas());
        assertNotNull(s3StorageService.crearS3Client());
        assertNotNull(s3StorageService.crearS3Presigner());

        // 3. Solo secretAccessKey
        ReflectionTestUtils.setField(s3StorageService, "accessKeyId", "");
        ReflectionTestUtils.setField(s3StorageService, "secretAccessKey", "secret");
        ReflectionTestUtils.setField(s3StorageService, "awsProfile", "");
        assertFalse(s3StorageService.tieneCredencialesValidas());
        assertNotNull(s3StorageService.crearS3Client());
        assertNotNull(s3StorageService.crearS3Presigner());

        // 4. Perfil nombrado personalizado
        ReflectionTestUtils.setField(s3StorageService, "accessKeyId", "");
        ReflectionTestUtils.setField(s3StorageService, "secretAccessKey", "");
        ReflectionTestUtils.setField(s3StorageService, "awsProfile", "prod-profile");
        assertTrue(s3StorageService.tieneCredencialesValidas());
        assertNotNull(s3StorageService.crearS3Client());
        assertNotNull(s3StorageService.crearS3Presigner());

        // 5. Ninguna credencial ni perfil
        ReflectionTestUtils.setField(s3StorageService, "accessKeyId", "");
        ReflectionTestUtils.setField(s3StorageService, "secretAccessKey", "");
        ReflectionTestUtils.setField(s3StorageService, "awsProfile", "");
        assertFalse(s3StorageService.tieneCredencialesValidas());
        assertNotNull(s3StorageService.crearS3Client());
        assertNotNull(s3StorageService.crearS3Presigner());
    }

    @Test
    @DisplayName("Debe generar URL de fallback si presigner falla")
    void generarPresignedUrlFallback() {
        S3StorageService serviceSpy = spy(s3StorageService);
        doThrow(new RuntimeException("Fallo presigner")).when(serviceSpy).crearS3Presigner();

        String url = serviceSpy.generarPresignedUrl("inspecciones/foto.jpg");
        assertNotNull(url);
        assertTrue(url.contains("reciclaje-litoral-fotos-prod.s3.us-east-1.amazonaws.com/inspecciones/foto.jpg"));
    }

    private byte[] crearImagenByteDePrueba(int width, int height) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        return baos.toByteArray();
    }

    private String crearImagenBase64DePrueba(int width, int height) {
        try {
            byte[] bytes = crearImagenByteDePrueba(width, height);
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
