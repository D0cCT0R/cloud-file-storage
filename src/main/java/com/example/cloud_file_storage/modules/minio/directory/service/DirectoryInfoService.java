package com.example.cloud_file_storage.modules.minio.directory.service;

import com.example.cloud_file_storage.common.MinioHelper;
import com.example.cloud_file_storage.modules.minio.dto.MinioDto;
import com.example.cloud_file_storage.modules.minio.dto.PathComponents;
import com.example.cloud_file_storage.modules.minio.dto.ResourceType;
import com.example.cloud_file_storage.modules.minio.exception.InvalidPathException;
import com.example.cloud_file_storage.modules.minio.service.PathResolverService;
import com.example.cloud_file_storage.modules.minio.service.ResourceValidationService;
import com.example.cloud_file_storage.modules.minio.service.UserPathService;
import io.minio.StatObjectResponse;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
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

    public List<MinioDto> getDirectoryInfo(String path, Long userId) throws InvalidPathException, ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        validationService.validateUserPath(path);
        List<MinioDto> directoryInfo = new ArrayList<>();
        String normalizedUserPath = resolverService.normalizeUserPath(path);
        String fullPath = pathService.toFullPath(userId, normalizedUserPath);
        String userDirectory = pathService.getUserFolder(userId);
        List<String> directoryPaths = minioHelper.listObjectsInDirectory(fullPath, RECURSIVE);
        for(String directoryPath : directoryPaths) {
            if(directoryPath.equals(fullPath)) {
                continue;
            }
            String relativePath = resolverService.getRelativePath(userDirectory, directoryPath);
            PathComponents components = resolverService.extractPathComponents(relativePath);
            if(directoryPath.endsWith("/")) {
                directoryInfo.add(MinioDto.builder()
                        .path(components.parentPath())
                        .name(components.name())
                        .type(ResourceType.DIRECTORY)
                        .build());
            } else  {
                StatObjectResponse response = minioHelper.statObject(directoryPath);
                directoryInfo.add(MinioDto.builder()
                        .path(components.parentPath())
                        .name(components.name())
                        .size(response.size())
                        .type(ResourceType.FILE)
                        .build());
            }
        }
        return directoryInfo;
    }
}
