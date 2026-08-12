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
        ReflectionTestUtils.setField(s3StorageService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(s3StorageService, "region", "us-east-1");
    }

    @Test
    @DisplayName("Debe generar URL de foto formateada hacia el bucket S3")
    void generarUrlFotoS3() {
        String url = s3StorageService.generarUrlFotoS3("antes");

        assertNotNull(url);
        assertTrue(url.startsWith("https://test-bucket.s3.us-east-1.amazonaws.com/inspecciones/antes_"));
        assertTrue(url.endsWith(".jpg"));
    }
}
