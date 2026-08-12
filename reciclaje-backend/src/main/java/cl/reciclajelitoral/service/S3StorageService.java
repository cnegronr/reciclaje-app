package cl.reciclajelitoral.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.util.Base64;
import java.util.UUID;

@Service
public class S3StorageService {

    @Value("${aws.s3.bucket-name:reciclaje-litoral-fotos-prod}")
    private String bucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

    @Value("${aws.profile:}")
    private String awsProfile;

    /**
     * Sube una imagen (Base64 o URL) a Amazon S3 y devuelve la URL final de S3.
     */
    public String subirFotoAS3(String photoData, String prefijo) {
        if (photoData == null || photoData.trim().isEmpty()) {
            return null;
        }

        // Si ya es una URL formateada de S3 o HTTP, mantenerla
        if (photoData.startsWith("http://") || photoData.startsWith("https://")) {
            return photoData;
        }

        String fileName = prefijo + "_" + UUID.randomUUID().toString() + ".jpg";
        String s3Key = "inspecciones/" + fileName;
        String s3Url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);

        // Si no hay credenciales o perfil AWS válido configurado, retornar URL simulada limpiamente
        if (!tieneCredencialesValidas()) {
            System.out.println("S3StorageService: Modo Simulación AWS (Credenciales/Perfil no detectado). URL generada: " + s3Url);
            return s3Url;
        }

        try {
            String base64Content = photoData;
            if (photoData.contains(",")) {
                base64Content = photoData.split(",")[1];
            }

            byte[] imageBytes = Base64.getDecoder().decode(base64Content);

            try (S3Client s3Client = crearS3Client()) {
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .contentType("image/jpeg")
                        .build();

                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));
                System.out.println("S3StorageService: Subida exitosa a Amazon S3 -> " + s3Url);
                return s3Url;
            }
        } catch (Exception e) {
            System.err.println("Aviso S3: Falló la subida a AWS S3 (" + e.getMessage() + "). Se retorna la URL asignada: " + s3Url);
            return s3Url;
        }
    }

    private S3Client crearS3Client() {
        S3ClientBuilder builder = S3Client.builder().region(Region.of(region));

        if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(secretAccessKey)
                && !accessKeyId.equalsIgnoreCase("dummy") && !accessKeyId.equalsIgnoreCase("test")) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            ));
        } else if (StringUtils.hasText(awsProfile)) {
            builder.credentialsProvider(ProfileCredentialsProvider.create(awsProfile));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }

    private boolean tieneCredencialesValidas() {
        // 1. Verificar credenciales estáticas explícitas
        if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(secretAccessKey)
                && !accessKeyId.equalsIgnoreCase("dummy") && !accessKeyId.equalsIgnoreCase("test")) {
            return true;
        }

        // 2. Verificar si se ha especificado un perfil AWS (ej. AWS_PROFILE=default)
        if (StringUtils.hasText(awsProfile) || StringUtils.hasText(System.getenv("AWS_PROFILE"))) {
            return true;
        }

        // 3. Verificar si existen variables de entorno globales del sistema o archivo ~/.aws/credentials
        String envKey = System.getenv("AWS_ACCESS_KEY_ID");
        String envSecret = System.getenv("AWS_SECRET_ACCESS_KEY");
        if (StringUtils.hasText(envKey) && StringUtils.hasText(envSecret)) {
            return true;
        }

        File awsCredentialsFile = new File(System.getProperty("user.home"), ".aws/credentials");
        return awsCredentialsFile.exists() && awsCredentialsFile.length() > 0;
    }
}
