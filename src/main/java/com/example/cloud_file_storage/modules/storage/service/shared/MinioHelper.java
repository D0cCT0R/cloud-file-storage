package com.example.cloud_file_storage.modules.storage.service.shared;


import com.example.cloud_file_storage.modules.storage.dto.resource.FileData;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MinioHelper {
    private final MinioClient minioClient;
    private final String bucketName;

    public MinioHelper(@Value("${minio.bucket-name}") String bucketName, MinioClient minioClient) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    public StatObjectResponse statObject(String fullPath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.statObject(StatObjectArgs.builder()
                .bucket(bucketName)
                .object(fullPath)
                .build());
    }

    public void putObject(String path, InputStream stream, Long size) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(path)
                .stream(stream, size, -1)
                .build());
    }

    public void createDirectory(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(path)
                .stream(
                        new ByteArrayInputStream(new byte[0]),
                        0,
                        -1
                )
                .build());
    }

    public List<String> listObjectsInDirectory(String fullPath, boolean recursive) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<String> objects = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(fullPath)
                        .recursive(recursive)
                        .build()
        );
        for (Result<Item> result : results) {
            objects.add(result.get().objectName());
        }
        return objects;
    }

    public void copyObject(String fullToPath, String fullFromPath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.copyObject(CopyObjectArgs.builder()
                .bucket(bucketName)
                .object(fullToPath)
                .source(CopySource.builder()
                        .bucket(bucketName)
                        .object(fullFromPath)
                        .build())
                .build());
    }

    public void removeObject(String fullPath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(fullPath)
                .build());
    }

    public void removeObjects(List<String> objectsToDelete) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        for (String object : objectsToDelete) {
            removeObject(object);
        }
    }

    public InputStream downloadFile(String fullPath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fullPath)
                        .build()
        );
    }

    public List<FileData> downloadAllFileInDirectory(String fullPath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<FileData> objects = new ArrayList<>();
        List<String> allFilePathsInDirectory = listObjectsInDirectory(fullPath, true);
        for (String objectPath : allFilePathsInDirectory) {
            StatObjectResponse stat = statObject(objectPath);
            InputStream stream = downloadFile(objectPath);
            objects.add(new FileData(objectPath, stream, stat.size()));
        }
        return objects;
    }

    public boolean objectExist(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if(isDirectory(path)) {
            return !listObjectsInDirectory(path, false).isEmpty();
        }
        try {
            statObject(path);
            return true;
        } catch (ErrorResponseException e) {
            if(e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            throw e;
        }
    }

    public boolean isDirectory(String path) {
        return path.endsWith("/");
    }
}

