package com.example.cloud_file_storage.modules.minio.resource.service;


import com.example.cloud_file_storage.modules.minio.service.MinioHelper;
import com.example.cloud_file_storage.modules.minio.dto.MinioDto;
import com.example.cloud_file_storage.modules.minio.dto.PathComponents;
import com.example.cloud_file_storage.modules.minio.dto.ResourceType;
import com.example.cloud_file_storage.modules.minio.exception.DirectoryOrFileNotFound;
import com.example.cloud_file_storage.modules.minio.exception.InvalidPathException;
import com.example.cloud_file_storage.modules.minio.exception.MinioIsNotAvailable;
import com.example.cloud_file_storage.modules.minio.service.PathResolverService;
import com.example.cloud_file_storage.modules.minio.service.ResourceValidationService;
import com.example.cloud_file_storage.modules.minio.service.UserPathService;
import io.minio.*;
import io.minio.errors.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ResourceInfoService {
    private final MinioHelper minioHelper;
    private final ResourceValidationService validationService;
    private final PathResolverService resolverService;
    private final UserPathService pathService;

    @Autowired
    public ResourceInfoService(ResourceValidationService validationService, PathResolverService resolverService, UserPathService pathService, MinioHelper minioHelper) {
        this.validationService = validationService;
        this.resolverService = resolverService;
        this.pathService = pathService;
        this.minioHelper = minioHelper;
    }

    public MinioDto getResourceInfo(String userPath, Long userId) throws InvalidPathException, DirectoryOrFileNotFound {
        try {
            log.info("Get resource for user. Path: {}, userID {}", userPath, userId);
            validationService.validateUserPath(userPath);
            String normalizedUserPath = resolverService.normalizeUserPath(userPath);
            String fullPath = pathService.toFullPath(userId, normalizedUserPath);
            if (!minioHelper.objectExist(fullPath)) {
                throw new DirectoryOrFileNotFound("Directory or file not found");
            }
            if (minioHelper.isDirectory(fullPath)) {
                return getDirectoryInfo(fullPath, normalizedUserPath);
            } else {
                return getFileInfo(fullPath, normalizedUserPath);
            }
        } catch (DirectoryOrFileNotFound | InvalidPathException e) {
            throw e;
        } catch (Exception e) {
            throw new MinioIsNotAvailable("Minio is not available", e);
        }
    }

    private MinioDto getFileInfo(String fullPath, String normalizedPath) throws DirectoryOrFileNotFound, ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        StatObjectResponse stat = minioHelper.statObject(fullPath);
        PathComponents components = resolverService.extractPathComponents(normalizedPath);
        log.debug("Get file info complete successfully. Path: {}", fullPath);
        return new MinioDto(
                components.parentPath(),
                components.name(),
                stat.size(),
                ResourceType.FILE
        );
    }

    private MinioDto getDirectoryInfo(String fullPath, String normalizedPath) throws DirectoryOrFileNotFound {
        PathComponents components = resolverService.extractPathComponents(normalizedPath);
        log.debug("Get directory info complete successfully. Path: {}", fullPath);
        return MinioDto.builder()
                .path(components.parentPath())
                .name(components.name())
                .type(ResourceType.DIRECTORY)
                .build();
    }
}
