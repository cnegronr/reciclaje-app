package cl.reciclajelitoral.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

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
        assertNull(s3StorageService.subirFotoAS3("   ", "inicial"));
    }

    @Test
    @DisplayName("Debe retornar la misma URL si photoData ya es una URL HTTP/HTTPS")
    void subirFotoAS3UrlExistente() {
        String urlHttp = "https://reciclaje-litoral-fotos-prod.s3.us-east-1.amazonaws.com/inspecciones/foto.jpg";
        String resultado = s3StorageService.subirFotoAS3(urlHttp, "inicial");

        assertEquals(urlHttp, resultado);
    }

    @Test
    @DisplayName("Debe procesar contenido Base64 y generar URL de Amazon S3")
    void subirFotoAS3Base64() {
        String base64Image = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP==";
        String resultado = s3StorageService.subirFotoAS3(base64Image, "inicial_antes");

        assertNotNull(resultado);
        assertTrue(resultado.contains("s3.us-east-1.amazonaws.com"));
        assertTrue(resultado.contains("inspecciones/inicial_antes_"));
    }
}
