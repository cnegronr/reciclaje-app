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

        if (photoData.startsWith("http://") || photoData.startsWith("https://")) {
            return photoData;
        }

        String fileName = prefijo + "_" + UUID.randomUUID().toString() + ".jpg";
        String s3Key = "inspecciones/" + fileName;
        String s3Url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);

        if (!tieneCredencialesValidas()) {
            return s3Url;
        }

        try {
            String base64Content = photoData.contains(",") ? photoData.split(",")[1] : photoData;
            byte[] imageBytes = Base64.getDecoder().decode(base64Content);

            S3Client s3Client = crearS3Client();
            try {
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .contentType("image/jpeg")
                        .build();

                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));
            } catch (Exception ignored) {
            } finally {
                s3Client.close();
            }
        } catch (Exception ignored) {
        }

        return s3Url;
    }

    public S3Client crearS3Client() {
        S3ClientBuilder builder = S3Client.builder().region(Region.of(region));

        if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(secretAccessKey)) {
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

    public boolean tieneCredencialesValidas() {
        boolean tieneLlaves = StringUtils.hasText(accessKeyId) && StringUtils.hasText(secretAccessKey);
        boolean tienePerfil = StringUtils.hasText(awsProfile);
        return tieneLlaves || tienePerfil;
    }
}
