package com.example.cloud_file_storage.modules.storage.service.directory;

import com.example.cloud_file_storage.modules.storage.exception.DirectoryOrFileNotFoundException;
import com.example.cloud_file_storage.modules.storage.exception.MinioIsNotAvailableException;
import com.example.cloud_file_storage.modules.storage.service.shared.*;
import com.example.cloud_file_storage.modules.storage.dto.storage.MinioDto;
import com.example.cloud_file_storage.modules.storage.dto.storage.PathComponents;
import com.example.cloud_file_storage.modules.storage.dto.storage.ResourceType;
import com.example.cloud_file_storage.modules.storage.exception.InvalidPathException;
import io.minio.StatObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DirectoryInfoService {

    private final MinioHelper minioHelper;
    private final PathResolverService resolverService;
    private final UserPathResolver resolver;
    private final boolean RECURSIVE = false;

    @Autowired
    public DirectoryInfoService(MinioHelper minioHelper, PathResolverService resolverService, UserPathResolver resolver) {
        this.minioHelper = minioHelper;
        this.resolverService = resolverService;
        this.resolver = resolver;
    }

    public List<MinioDto> getDirectoryInfo(String path, Long userId) {
        try {
            log.info("Starting get directory info. Path: {}, userID: {}", path, userId);
            String fullPath = resolver.resolveFullPath(path, userId);
            List<MinioDto> directoryInfo = new ArrayList<>();
            if(!minioHelper.objectExist(fullPath)) {
                throw new DirectoryOrFileNotFoundException("Resource not found");
            }
            List<String> directoryPaths = minioHelper.listObjectsInDirectory(fullPath, RECURSIVE);
            log.debug("Success getting list directory objects. Path: {}, userID: {}, size: {}", path, userId, directoryPaths.size());
            for (String directoryPath : directoryPaths) {
                if (directoryPath.equals(fullPath)) {
                    continue;
                }
                log.debug("Path: {}", directoryPath);
                String relativePath = resolverService.getRelativePath(userId, directoryPath);
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
        } catch (DirectoryOrFileNotFoundException | InvalidPathException e) {
            throw e;
        } catch (Exception e) {
            throw new MinioIsNotAvailableException("Minio is not available", e);
        }
    }
}


