package com.example.cloud_file_storage.modules.minio.resource.service;

import com.example.cloud_file_storage.modules.minio.exception.*;
import com.example.cloud_file_storage.modules.minio.service.MinioHelper;
import com.example.cloud_file_storage.modules.minio.dto.MinioDto;
import com.example.cloud_file_storage.modules.minio.dto.PathComponents;
import com.example.cloud_file_storage.modules.minio.dto.ResourceType;
import com.example.cloud_file_storage.modules.minio.service.PathResolverService;
import com.example.cloud_file_storage.modules.minio.service.ResourceValidationService;
import com.example.cloud_file_storage.modules.minio.service.UserPathService;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class ResourceMoveService {
    private final MinioHelper minioHelper;
    private final ResourceValidationService validationService;
    private final PathResolverService resolverService;
    private final UserPathService pathService;
    private final boolean RECURSIVE = true;

    public ResourceMoveService(MinioHelper minioHelper,
                               ResourceValidationService validationService,
                               PathResolverService resolverService,
                               UserPathService pathService) {
        this.minioHelper = minioHelper;
        this.validationService = validationService;
        this.resolverService = resolverService;
        this.pathService = pathService;
    }

    public MinioDto moveOrRenameResource(String fromUserPath, String toUserPath, Long userId) throws Exception {
        try {
            log.info("Move or Rename resource. From path: {}, To path: {}, userID: {}", fromUserPath, toUserPath, userId);
            validationService.validateUserPath(fromUserPath);
            validationService.validateUserPath(toUserPath);
            String normalizeFromPath = resolverService.normalizeUserPath(fromUserPath);
            String normalizeToPath = resolverService.normalizeUserPath(toUserPath);
            String fullFromPath = pathService.toFullPath(userId, normalizeFromPath);
            String fullToPath = pathService.toFullPath(userId, normalizeToPath);
            if (!minioHelper.objectExist(fullFromPath)) {
                throw new DirectoryOrFileNotFound("Resource not found");
            }
            if (minioHelper.objectExist(fullToPath)) {
                throw new DirectoryOrFileAlreadyExistException("Directory or file already exist");
            }
            if (minioHelper.isDirectory(fullFromPath) && minioHelper.isDirectory(fullToPath)) {
                return renameOrMoveDirectory(fullFromPath, fullToPath, userId);
            } else {
                return renameOrMoveFile(fullFromPath, fullToPath, userId);
            }
        } catch (InvalidPathException | DirectoryOrFileNotFound | DirectoryOrFileAlreadyExistException e) {
            throw e;
        } catch (Exception e) {
            throw new MinioIsNotAvailable("Minio is not available", e);
        }
    }

    private MinioDto renameOrMoveFile(String fullFromPath, String fullToPath, Long id) throws Exception {
        minioHelper.copyObject(fullToPath, fullFromPath);
        minioHelper.removeObject(fullFromPath);
        StatObjectResponse stat = minioHelper.statObject(fullToPath);
        String userDirectory = pathService.getUserFolder(id);
        String relativeToPath = resolverService.getRelativePath(userDirectory, fullToPath);
        PathComponents toComponents = resolverService.extractPathComponents(relativeToPath);
        log.debug("Move or Rename file complete successfully. Path: {}, userID: {}", fullToPath, id);
        return MinioDto.builder()
                .path(toComponents.parentPath())
                .name(toComponents.name())
                .size(stat.size())
                .type(ResourceType.FILE)
                .build();
    }

    private MinioDto renameOrMoveDirectory(String fullFromPath, String fullToPath, Long id) throws Exception {
        List<String> objectsToMove = minioHelper.listObjectsInDirectory(fullFromPath, RECURSIVE);
        for (String sourceObject : objectsToMove) {
            String targetObject = sourceObject.replace(fullFromPath, fullToPath);
            minioHelper.copyObject(targetObject, sourceObject);
        }
        String userDirectory = pathService.getUserFolder(id);
        String relativeToPath = resolverService.getRelativePath(userDirectory, fullToPath);
        PathComponents toComponents = resolverService.extractPathComponents(relativeToPath);
        minioHelper.removeObjects(objectsToMove);
        log.debug("Rename of Move directory complete successfully. Path: {}, userID: {}", fullToPath, id);
        return MinioDto.builder()
                .path(toComponents.parentPath())
                .name(toComponents.name())
                .type(ResourceType.DIRECTORY)
                .build();
    }
}
