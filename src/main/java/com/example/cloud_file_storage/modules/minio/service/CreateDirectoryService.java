package com.example.cloud_file_storage.modules.minio.service;

import com.example.cloud_file_storage.modules.minio.dto.MinioDto;
import com.example.cloud_file_storage.modules.minio.dto.PathComponents;
import com.example.cloud_file_storage.modules.minio.dto.ResourceType;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CreateDirectoryService {

    private final MinioHelper minioHelper;
    private final PathResolverService resolverService;
    private final UserPathService pathService;

    @Autowired
    public CreateDirectoryService(MinioHelper minioHelper, PathResolverService resolverService, UserPathService pathService) {
        this.minioHelper = minioHelper;
        this.resolverService = resolverService;
        this.pathService = pathService;
    }

    public MinioDto createDirectory(String path, Long id) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String normalizeUserPath = resolverService.normalizeUserPath(path);
        String userDirectory = pathService.getUserFolder(id);
        String fullPath = pathService.toFullPath(id, normalizeUserPath);
        minioHelper.createDirectory(fullPath);
        String relativePath = resolverService.getRelativePath(userDirectory, fullPath);
        PathComponents components = resolverService.extractPathComponents(relativePath);
        return MinioDto.builder()
                .path(components.parentPath())
                .name(components.name())
                .type(ResourceType.DIRECTORY)
                .build();
    }

    public List<MinioDto> createParentDirectories(String filePath, String userPath, Long id) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<MinioDto> results = new ArrayList<>();
        if(!filePath.contains("/")) {
            return results;
        }
        String userDirectory = pathService.getUserFolder(id);
        String basePath = userDirectory + userPath;
        String[] pathComponents = filePath.split("/");
        StringBuilder currentPath = new StringBuilder(basePath);
        for(int i = 0; i < pathComponents.length - 1; i++) {
            currentPath.append(pathComponents[i]).append("/");
            String directoryPath = currentPath.toString();
            String relativePath = directoryPath.substring(userDirectory.length());
            if(!minioHelper.objectExist(directoryPath)) {
                minioHelper.createDirectory(directoryPath);
                PathComponents components = resolverService.extractPathComponents(relativePath);
                results.add(MinioDto.builder()
                        .path(components.parentPath())
                        .name(components.name())
                        .type(ResourceType.DIRECTORY)
                        .build());
            }
        }
        return results;
    }
}
