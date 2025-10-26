package com.example.cloud_file_storage.modules.minio.resource.service;

import com.example.cloud_file_storage.common.MinioHelper;
import com.example.cloud_file_storage.modules.minio.dto.MinioDto;
import com.example.cloud_file_storage.modules.minio.dto.PathComponents;
import com.example.cloud_file_storage.modules.minio.dto.ResourceType;
import com.example.cloud_file_storage.modules.minio.exception.DirectoryExistException;
import com.example.cloud_file_storage.modules.minio.exception.DirectoryOrFileNotFound;
import com.example.cloud_file_storage.modules.minio.exception.FileAlreadyExistException;
import com.example.cloud_file_storage.modules.minio.exception.MinioIsNotAvailable;
import com.example.cloud_file_storage.modules.minio.service.PathResolverService;
import com.example.cloud_file_storage.modules.minio.service.ResourceValidationService;
import com.example.cloud_file_storage.modules.minio.service.UserPathService;
import io.minio.*;
import io.minio.errors.*;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ResourceMoveService {
    private final MinioHelper minioHelper;
    private final ResourceValidationService validationService;
    private final PathResolverService resolverService;
    private final UserPathService pathService;

    public ResourceMoveService(MinioHelper minioHelper,
                               ResourceValidationService validationService,
                               PathResolverService resolverService,
                               UserPathService pathService) {
        this.minioHelper = minioHelper;
        this.validationService = validationService;
        this.resolverService = resolverService;
        this.pathService = pathService;
    }

    public MinioDto moveOrRenameResource(String fromPath, String toPath, Long userId) throws Exception {
        validationService.validateUserPath(fromPath);
        validationService.validateUserPath(toPath);
        String normalizeFromPath = resolverService.normalizeUserPath(fromPath);
        String normalizeToPath = resolverService.normalizeUserPath(toPath);
        PathComponents toComponents = resolverService.extractPathComponents(toPath);
        String fullFromPath = pathService.toFullPath(userId, normalizeFromPath);
        String fullToPath = pathService.toFullPath(userId, normalizeToPath);
        boolean isDirectoryFrom = fullFromPath.endsWith("/");
        boolean isDirectoryTo = fullToPath.endsWith("/");
        if (isDirectoryFrom && isDirectoryTo) {
            return renameOrMoveDirectory(fullFromPath, fullToPath, toComponents);
        } else {
            return renameOrMoveFile(fullFromPath, fullToPath, toComponents);
        }
    }

    private MinioDto renameOrMoveFile(String fullFromPath, String fullToPath, PathComponents toComponents) throws Exception {
        try {
            minioHelper.copyObject(fullToPath, fullFromPath);
            minioHelper.removeObject(fullFromPath);
            StatObjectResponse stat = minioHelper.statObject(fullToPath);
            return new MinioDto(
                    toComponents.parentPath(),
                    toComponents.name(),
                    stat.size(),
                    ResourceType.FILE
            );
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                throw new DirectoryOrFileNotFound("Ресурс не найден");
            } else if ("BucketAlreadyExists".equals(e.errorResponse().code()) ||
                    "409".equals(e.errorResponse().code())) {
                throw new FileAlreadyExistException("Ресурс уже существует о пути " + toComponents.parentPath());
            }
        } catch (Exception e) {
            throw new MinioIsNotAvailable("Ошибка минио " + e);
        }
        return null;
    }

    private MinioDto renameOrMoveDirectory(String fullFromPath, String fullToPath, PathComponents components) throws Exception {
        List<String> objectsToMove = minioHelper.listObjectsInDirectory(fullFromPath, true);
        if(objectsToMove.isEmpty()) {
            throw new DirectoryOrFileNotFound("Директория не найдена");
        }
        if(minioHelper.directoryExists(fullToPath)) {
            throw new DirectoryExistException("Директория уже существует");
        }
        for(String sourceObject: objectsToMove) {
            String targetObject = sourceObject.replace(fullFromPath, fullToPath);
            minioHelper.copyObject(targetObject, sourceObject);
        }
        minioHelper.removeObjects(objectsToMove);
        return new MinioDto(
                components.parentPath(),
                components.name(),
                null,
                ResourceType.DIRECTORY
        );
    }
}
