package com.example.cloud_file_storage.modules.storage.service.resource;


import com.example.cloud_file_storage.modules.storage.service.shared.*;
import com.example.cloud_file_storage.modules.storage.dto.storage.MinioDto;
import com.example.cloud_file_storage.modules.storage.dto.storage.PathComponents;
import com.example.cloud_file_storage.modules.storage.dto.storage.ResourceType;
import com.example.cloud_file_storage.modules.storage.exception.DirectoryOrFileNotFound;
import com.example.cloud_file_storage.modules.storage.exception.InvalidPathException;
import com.example.cloud_file_storage.modules.storage.exception.MinioIsNotAvailable;
import io.minio.*;
import io.minio.errors.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ResourceInfoService {

    private final MinioHelper minioHelper;
    private final PathResolverService resolverService;
    private final UserPathResolver resolver;

    @Autowired
    public ResourceInfoService(PathResolverService resolverService, MinioHelper minioHelper, UserPathResolver resolver) {
        this.resolverService = resolverService;
        this.minioHelper = minioHelper;
        this.resolver = resolver;
    }

    public MinioDto getResourceInfo(String userPath, Long userId) throws InvalidPathException, DirectoryOrFileNotFound {
        try {
            log.info("Get resource for user. Path: {}, userID {}", userPath, userId);
            String fullPath = resolver.resolveFullPath(userPath, userId);
            if (!minioHelper.objectExist(fullPath)) {
                throw new DirectoryOrFileNotFound("Directory or file not found");
            }
            if (minioHelper.isDirectory(fullPath)) {
                return getDirectoryInfo(fullPath, userId);
            } else {
                return getFileInfo(fullPath, userId);
            }
        } catch (DirectoryOrFileNotFound | InvalidPathException e) {
            throw e;
        } catch (Exception e) {
            throw new MinioIsNotAvailable("Minio is not available", e);
        }
    }

    private MinioDto getFileInfo(String fullPath, Long id) throws DirectoryOrFileNotFound, ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        StatObjectResponse stat = minioHelper.statObject(fullPath);
        String relativePath = resolverService.getRelativePath(id, fullPath);
        PathComponents components = resolverService.extractPathComponents(relativePath);
        log.debug("Get file info complete successfully. Path: {}", fullPath);
        return new MinioDto(
                components.parentPath(),
                components.name(),
                stat.size(),
                ResourceType.FILE
        );
    }

    private MinioDto getDirectoryInfo(String fullPath, Long id) throws DirectoryOrFileNotFound {
        String relativePath = resolverService.getRelativePath(id, fullPath);
        PathComponents components = resolverService.extractPathComponents(relativePath);
        log.debug("Get directory info complete successfully. Path: {}", fullPath);
        return MinioDto.builder()
                .path(components.parentPath())
                .name(components.name())
                .type(ResourceType.DIRECTORY)
                .build();
    }
}


