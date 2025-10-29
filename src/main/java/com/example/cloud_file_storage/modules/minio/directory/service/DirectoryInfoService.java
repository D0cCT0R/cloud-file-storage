package com.example.cloud_file_storage.modules.minio.directory.service;

import com.example.cloud_file_storage.modules.minio.exception.DirectoryOrFileNotFound;
import com.example.cloud_file_storage.modules.minio.exception.MinioIsNotAvailable;
import com.example.cloud_file_storage.modules.minio.service.MinioHelper;
import com.example.cloud_file_storage.modules.minio.dto.MinioDto;
import com.example.cloud_file_storage.modules.minio.dto.PathComponents;
import com.example.cloud_file_storage.modules.minio.dto.ResourceType;
import com.example.cloud_file_storage.modules.minio.exception.InvalidPathException;
import com.example.cloud_file_storage.modules.minio.service.PathResolverService;
import com.example.cloud_file_storage.modules.minio.service.ResourceValidationService;
import com.example.cloud_file_storage.modules.minio.service.UserPathService;
import io.minio.StatObjectResponse;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DirectoryInfoService {

    private final MinioHelper minioHelper;
    private final ResourceValidationService validationService;
    private final PathResolverService resolverService;
    private final UserPathService pathService;
    private final boolean RECURSIVE = false;

    @Autowired
    public DirectoryInfoService(MinioHelper minioHelper, ResourceValidationService validationService, PathResolverService resolverService, UserPathService pathService) {
        this.minioHelper = minioHelper;
        this.validationService = validationService;
        this.resolverService = resolverService;
        this.pathService = pathService;
    }

    public List<MinioDto> getDirectoryInfo(String path, Long userId) throws DirectoryOrFileNotFound, InvalidPathException {
        try {
            log.info("Starting get directory info. Path: {}, userID: {}", path, userId);
            validationService.validateUserPath(path);
            List<MinioDto> directoryInfo = new ArrayList<>();
            String normalizedUserPath = resolverService.normalizeUserPath(path);
            String fullPath = pathService.toFullPath(userId, normalizedUserPath);
            if(!minioHelper.objectExist(fullPath)) {
                throw new DirectoryOrFileNotFound("Resource not found");
            }
            String userDirectory = pathService.getUserFolder(userId);
            List<String> directoryPaths = minioHelper.listObjectsInDirectory(fullPath, RECURSIVE);
            log.debug("Success getting list directory objects. Path: {}, userID: {}, size: {}", path, userId, directoryPaths.size());
            for (String directoryPath : directoryPaths) {
                if (directoryPath.equals(fullPath)) {
                    continue;
                }
                String relativePath = resolverService.getRelativePath(userDirectory, directoryPath);
                PathComponents components = resolverService.extractPathComponents(relativePath);
                if (minioHelper.isDirectory(directoryPath)) {
                    directoryInfo.add(MinioDto.builder()
                            .path(components.parentPath())
                            .name(components.name())
                            .type(ResourceType.DIRECTORY)
                            .build());
                } else {
                    StatObjectResponse response = minioHelper.statObject(directoryPath);
                    directoryInfo.add(MinioDto.builder()
                            .path(components.parentPath())
                            .name(components.name())
                            .size(response.size())
                            .type(ResourceType.FILE)
                            .build());
                }
            }
            log.info("Get directory info complete successfully. Path: {} , userID: {}", path, userId);
            return directoryInfo;
        } catch (DirectoryOrFileNotFound | InvalidPathException e) {
            throw e;
        } catch (Exception e) {
            throw new MinioIsNotAvailable("Minio is not available", e);
        }
    }
}
