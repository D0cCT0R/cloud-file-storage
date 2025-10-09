package com.example.cloud_file_storage.modules.resource.service;

import com.example.cloud_file_storage.modules.resource.exception.InitializeBucketException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MinioService {


    private final String bucketName;

    @Autowired
    public MinioService(MinioClient minioClient, @Value("${minio.bucket-name}") String bucketName) throws InitializeBucketException {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        initializeBucket();
    }

    public void initializeBucket() throws InitializeBucketException {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if(!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("Bucket {} успешно создан", bucketName);
            }
        } catch (Exception  e) {
            throw new InitializeBucketException("Ошибка инициализации Minio бакета" + e);
        }
    }



}
