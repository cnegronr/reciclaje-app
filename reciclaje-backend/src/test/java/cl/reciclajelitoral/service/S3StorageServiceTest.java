package cl.reciclajelitoral.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

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
    @DisplayName("Debe procesar contenido Base64 en modo simulado cuando no hay credenciales AWS")
    void subirFotoAS3Base64Simulado() {
        ReflectionTestUtils.setField(s3StorageService, "accessKeyId", "");
        ReflectionTestUtils.setField(s3StorageService, "secretAccessKey", "");
        ReflectionTestUtils.setField(s3StorageService, "awsProfile", "");

        assertFalse(s3StorageService.tieneCredencialesValidas());

        String base64Image = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP==";
        String resultado = s3StorageService.subirFotoAS3(base64Image, "inicial_antes");

        assertNotNull(resultado);
        assertTrue(resultado.contains("s3.us-east-1.amazonaws.com"));
        assertTrue(resultado.contains("inspecciones/inicial_antes_"));
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

        String base64Image = "data:image/jpeg;base64,/9j/4AAQSkZJRg==";
        String resultado = serviceSpy.subirFotoAS3(base64Image, "test_exitoso");

        assertNotNull(resultado);
        assertTrue(resultado.contains("inspecciones/test_exitoso_"));
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
        assertTrue(resultado.contains("inspecciones/error_test_"));
    }

    @Test
    @DisplayName("Debe intentar crear cliente S3 con credenciales estaticas")
    void subirFotoAS3CredencialesEstaticas() {
        ReflectionTestUtils.setField(s3StorageService, "accessKeyId", "AKIAIOSFODNN7EXAMPLE");
        ReflectionTestUtils.setField(s3StorageService, "secretAccessKey", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        ReflectionTestUtils.setField(s3StorageService, "awsProfile", "");

        assertTrue(s3StorageService.tieneCredencialesValidas());

        S3Client client = s3StorageService.crearS3Client();
        assertNotNull(client);

        String base64Image = "data:image/jpeg;base64,/9j/4AAQSkZJRg==";
        String resultado = s3StorageService.subirFotoAS3(base64Image, "act_antes");

        assertNotNull(resultado);
        assertTrue(resultado.contains("inspecciones/act_antes_"));
    }

    @Test
    @DisplayName("Debe evaluar todas las combinaciones posibles de tieneCredencialesValidas y crearS3Client")
    void evaluarCombinacionesCredenciales() {
        // 1. Ambas llaves presentes
        ReflectionTestUtils.setField(s3StorageService, "accessKeyId", "key");
        ReflectionTestUtils.setField(s3StorageService, "secretAccessKey", "secret");
        ReflectionTestUtils.setField(s3StorageService, "awsProfile", "");
        assertTrue(s3StorageService.tieneCredencialesValidas());
        assertNotNull(s3StorageService.crearS3Client());

        // 2. Solo accessKeyId
        ReflectionTestUtils.setField(s3StorageService, "accessKeyId", "key");
        ReflectionTestUtils.setField(s3StorageService, "secretAccessKey", "");
        ReflectionTestUtils.setField(s3StorageService, "awsProfile", "");
        assertFalse(s3StorageService.tieneCredencialesValidas());
        assertNotNull(s3StorageService.crearS3Client());

        // 3. Solo secretAccessKey
        ReflectionTestUtils.setField(s3StorageService, "accessKeyId", "");
        ReflectionTestUtils.setField(s3StorageService, "secretAccessKey", "secret");
        ReflectionTestUtils.setField(s3StorageService, "awsProfile", "");
        assertFalse(s3StorageService.tieneCredencialesValidas());
        assertNotNull(s3StorageService.crearS3Client());

        // 4. Solo awsProfile
        ReflectionTestUtils.setField(s3StorageService, "accessKeyId", "");
        ReflectionTestUtils.setField(s3StorageService, "secretAccessKey", "");
        ReflectionTestUtils.setField(s3StorageService, "awsProfile", "default");
        assertTrue(s3StorageService.tieneCredencialesValidas());
        assertNotNull(s3StorageService.crearS3Client());

        // 5. Ninguna credencial ni perfil
        ReflectionTestUtils.setField(s3StorageService, "accessKeyId", "");
        ReflectionTestUtils.setField(s3StorageService, "secretAccessKey", "");
        ReflectionTestUtils.setField(s3StorageService, "awsProfile", "");
        assertFalse(s3StorageService.tieneCredencialesValidas());
        assertNotNull(s3StorageService.crearS3Client());
    }

    @Test
    @DisplayName("Debe intentar crear cliente S3 con perfil AWS")
    void subirFotoAS3PerfilAWS() {
        ReflectionTestUtils.setField(s3StorageService, "accessKeyId", "");
        ReflectionTestUtils.setField(s3StorageService, "secretAccessKey", "");
        ReflectionTestUtils.setField(s3StorageService, "awsProfile", "default");

        assertTrue(s3StorageService.tieneCredencialesValidas());

        S3Client client = s3StorageService.crearS3Client();
        assertNotNull(client);

        String base64Image = "/9j/4AAQSkZJRg==";
        String resultado = s3StorageService.subirFotoAS3(base64Image, "act_despues");

        assertNotNull(resultado);
        assertTrue(resultado.contains("inspecciones/act_despues_"));
    }
}
