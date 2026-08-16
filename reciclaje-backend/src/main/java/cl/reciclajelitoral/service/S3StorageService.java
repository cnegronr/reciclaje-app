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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
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
     * Sube una imagen (Base64 o URL) a Amazon S3 previa optimización/compresión y devuelve la URL final (o Data URL en desarrollo local).
     */
    public String subirFotoAS3(String photoData, String prefijo) {
        if (photoData == null || photoData.trim().isEmpty()) {
            return null;
        }

        if (photoData.startsWith("http://") || photoData.startsWith("https://")) {
            return photoData;
        }

        // Si no existen credenciales AWS configuradas en local, retornar el Data URL Base64 directamente
        if (!tieneCredencialesValidas()) {
            System.out.println("S3StorageService: Modo Simulación Local (Retornando Data URL directamente).");
            return photoData;
        }

        String fileName = prefijo + "_" + UUID.randomUUID().toString() + ".jpg";
        String s3Key = "inspecciones/" + fileName;

        try {
            String base64Content = photoData.contains(",") ? photoData.split(",")[1] : photoData;
            byte[] rawBytes = Base64.getDecoder().decode(base64Content);
            byte[] compressedBytes = compresionarImagen(rawBytes);

            S3Client s3Client = crearS3Client();
            try {
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .contentType("image/jpeg")
                        .build();

                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(compressedBytes));

                String s3Url = generarPresignedUrl(s3Key);
                System.out.println("S3StorageService: Subida exitosa a Amazon S3 -> " + s3Url);
                return s3Url;
            } finally {
                s3Client.close();
            }
        } catch (Exception e) {
            System.err.println("Aviso S3: Falló la subida a AWS S3 (" + e.getMessage() + "). Se retorna Data URL de respaldo.");
            return photoData;
        }
    }

    /**
     * Genera una URL firmada (Presigned URL) para acceder de forma segura a objetos en buckets S3 privados sin Access Denied.
     */
    public String generarPresignedUrl(String s3Key) {
        try (S3Presigner presigner = crearS3Presigner()) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofDays(7))
                    .getObjectRequest(getObjectRequest)
                    .build();

            return presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
        }
    }

    /**
     * Redimensiona (máximo 1280px) y comprime (75% calidad JPEG) la imagen para optimizar almacenamiento y transferencia.
     */
    public byte[] compresionarImagen(byte[] rawBytes) {
        if (rawBytes == null || rawBytes.length == 0) {
            return rawBytes;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(rawBytes)) {
            BufferedImage originalImage = ImageIO.read(bais);
            if (originalImage == null) {
                return rawBytes;
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            int maxDimension = 1280;

            int targetWidth = originalWidth;
            int targetHeight = originalHeight;

            int maxActual = Math.max(originalWidth, originalHeight);
            if (maxActual > maxDimension) {
                targetWidth = (originalWidth * maxDimension) / maxActual;
                targetHeight = (originalHeight * maxDimension) / maxActual;
            }

            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.75f);

            try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(resizedImage, null, null), param);
            } finally {
                writer.dispose();
            }

            return baos.toByteArray();
        } catch (Exception e) {
            return rawBytes;
        }
    }

    public software.amazon.awssdk.auth.credentials.AwsCredentialsProvider resolveCredentialsProvider() {
        if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(secretAccessKey)) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            );
        }
        if (StringUtils.hasText(awsProfile) && !"default".equalsIgnoreCase(awsProfile.trim())) {
            try {
                return ProfileCredentialsProvider.create(awsProfile.trim());
            } catch (Exception e) {
                return DefaultCredentialsProvider.create();
            }
        }
        return DefaultCredentialsProvider.create();
    }

    public S3Client crearS3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(resolveCredentialsProvider())
                .build();
    }

    public S3Presigner crearS3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(resolveCredentialsProvider())
                .build();
    }

    public boolean tieneCredencialesValidas() {
        boolean tieneLlaves = StringUtils.hasText(accessKeyId) && StringUtils.hasText(secretAccessKey);
        boolean tienePerfilNombrado = StringUtils.hasText(awsProfile) && !"default".equalsIgnoreCase(awsProfile.trim());
        boolean enEntornoAWS = StringUtils.hasText(System.getenv("AWS_ACCESS_KEY_ID")) ||
                               StringUtils.hasText(System.getenv("AWS_CONTAINER_CREDENTIALS_RELATIVE_URI")) ||
                               StringUtils.hasText(System.getenv("AWS_WEB_IDENTITY_TOKEN_FILE")) ||
                               StringUtils.hasText(System.getenv("AWS_ROLE_ARN")) ||
                               StringUtils.hasText(System.getenv("AWS_EXECUTION_ENV"));
        return tieneLlaves || tienePerfilNombrado || enEntornoAWS;
    }
}
