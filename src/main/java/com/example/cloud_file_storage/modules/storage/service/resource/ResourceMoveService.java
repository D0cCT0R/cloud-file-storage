package com.example.cloud_file_storage.modules.storage.service.resource;

import com.example.cloud_file_storage.modules.storage.exception.*;
import com.example.cloud_file_storage.modules.storage.service.shared.*;
import com.example.cloud_file_storage.modules.storage.dto.storage.MinioDto;
import com.example.cloud_file_storage.modules.storage.dto.storage.PathComponents;
import com.example.cloud_file_storage.modules.storage.dto.storage.ResourceType;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class ResourceMoveService {
    private final MinioHelper minioHelper;
    private final PathResolverService resolverService;
    private final UserPathResolver resolver;
    private final boolean RECURSIVE = true;

    public ResourceMoveService(MinioHelper minioHelper,
                               PathResolverService resolverService, UserPathResolver resolver) {
        this.minioHelper = minioHelper;
        this.resolverService = resolverService;
        this.resolver = resolver;
    }

    public MinioDto moveOrRenameResource(String fromUserPath, String toUserPath, Long userId) throws Exception {
        try {
            log.info("Move or Rename resource. From path: {}, To path: {}, userID: {}", fromUserPath, toUserPath, userId);
            String fullFromPath = resolver.resolveFullPath(fromUserPath, userId);
            String fullToPath = resolver.resolveFullPath(toUserPath, userId);
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
        String relativeToPath = resolverService.getRelativePath(id, fullToPath);
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
        String relativeToPath = resolverService.getRelativePath(id, fullToPath);
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


