package cl.reciclajelitoral.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class S3StorageService {

    @Value("${aws.s3.bucket-name:reciclaje-litoral-fotos-prod}")
    private String bucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    /**
     * Genera una URL de S3 simulada / presignada para almacenamiento de fotos de inspección.
     */
    public String generarUrlFotoS3(String prefijo) {
        String fileName = prefijo + "_" + UUID.randomUUID().toString() + ".jpg";
        return String.format("https://%s.s3.%s.amazonaws.com/inspecciones/%s", bucketName, region, fileName);
    }
}
