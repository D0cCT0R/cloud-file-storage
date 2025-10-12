package com.example.cloud_file_storage.modules.resource.service;

import com.example.cloud_file_storage.modules.resource.dto.MinioDto;
import com.example.cloud_file_storage.modules.resource.dto.PathComponents;
import com.example.cloud_file_storage.modules.resource.exception.DirectoryOrFileNotFound;
import com.example.cloud_file_storage.modules.resource.exception.FileAlreadyExistException;
import com.example.cloud_file_storage.modules.resource.exception.MinioIsNotAvailable;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



@Service
public class ResourceMoveService {
    private final MinioClient minioClient;
    private final ResourceValidationService validationService;
    private final PathResolverService resolverService;
    private final UserPathService pathService;
    private final String bucketName;

    public ResourceMoveService(MinioClient minioClient,
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

    public MinioDto moveOrRenameResource(String from, String to, Long userId) throws Exception {
        validationService.validateUserPath(from);
        validationService.validateUserPath(to);
        String normalizeFromPath = resolverService.normalizeUserPath(from);
        String normalizeToPath = resolverService.normalizeUserPath(to);
        PathComponents toComponents = resolverService.extractPathComponents(to);
        String fullFromPath = pathService.toFullPath(userId, normalizeFromPath);
        String fullToPath = pathService.toFullPath(userId, normalizeToPath);
        return renameOrMoveFile(fullFromPath, fullToPath, toComponents);
    }
    //#TODO сделать перемещение для директории и отрефакторить код для переименования или перемещения файла
    private MinioDto renameOrMoveFile(String fullFromPath, String fullToPath, PathComponents toComponents) throws Exception {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fullToPath)
                    .source(CopySource.builder()
                            .bucket(bucketName)
                            .object(fullFromPath)
                            .build())
                    .build());
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fullFromPath)
                    .build());
            return new MinioDto(
                    toComponents.parentPath(),
                    toComponents.name(),
                    123,
            )
        } catch (ErrorResponseException e) {
            if("NoSuchKey".equals(e.errorResponse().code())) {
                throw new DirectoryOrFileNotFound("Ресурс не найден");
            } else if ("BucketAlreadyExists".equals(e.errorResponse().code()) ||
                    "409".equals(e.errorResponse().code())) {
                throw new FileAlreadyExistException("Ресурс уже существует о пути " + toComponents.parentPath());
            }
        } catch (Exception e) {
            throw new MinioIsNotAvailable("Ошибка минио " + e);
        }
    }

}
