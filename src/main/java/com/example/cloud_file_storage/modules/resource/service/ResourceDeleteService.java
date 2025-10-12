package com.example.cloud_file_storage.modules.resource.service;

import com.example.cloud_file_storage.modules.resource.exception.DirectoryOrFileNotFound;
import com.example.cloud_file_storage.modules.resource.exception.MinioIsNotAvailable;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ResourceDeleteService {
    private final MinioClient minioClient;
    private final ResourceValidationService validationService;
    private final PathResolverService resolverService;
    private final UserPathService pathService;
    private final String bucketName;

    public ResourceDeleteService(MinioClient minioClient,
                                 ResourceValidationService validationService,
                                 PathResolverService resolverService,
                                 UserPathService pathService,
                                 @Value("${minio.bucket-name}") String bucketName) {
        this.minioClient = minioClient;
        this.validationService = validationService;
        this.resolverService = resolverService;
        this.pathService = pathService;
        this.bucketName = bucketName;
    }


    public void deleteResource(String userPath, Long userId) throws Exception {
        log.info("Удаление ресурса для пользователя");
        validationService.validateUserPath(userPath);
        String normalizedPath = resolverService.normalizeUserPath(userPath);
        String fullPath = pathService.toFullPath(userId, normalizedPath);
        boolean isDirectory = userPath.endsWith("/");
        if(isDirectory) {
            deleteDirectory(fullPath);
        } else {
            deleteFile(fullPath);
        }
    }

    private void deleteDirectory(String fullPath) throws Exception {
        List<String> objectsToDelete = listObjectsInDirectory(fullPath);
        if(objectsToDelete.isEmpty()) {
            throw new DirectoryOrFileNotFound("Директория не найдена");
        }
        List<DeleteObject> deleteObjects = objectsToDelete.stream()
                .map(DeleteObject::new)
                .toList();
        minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucketName)
                        .objects(deleteObjects)
                        .build()
        );
    }

    private void deleteFile(String fullPath) throws DirectoryOrFileNotFound {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fullPath)
                    .build());
        } catch (ErrorResponseException e) {
            throw new DirectoryOrFileNotFound("Ошибка при удалении, файл не найден" + e);
        } catch (Exception e) {
            throw new MinioIsNotAvailable("Ошибка минио" + e);
        }
    }
    private List<String> listObjectsInDirectory(String path) throws Exception {
        List<String> objects = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .recursive(true)
                        .build()
        );
        for (Result<Item> result : results) {
            objects.add(result.get().objectName());
        }
        return objects;
    }
}
