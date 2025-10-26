package com.example.cloud_file_storage.modules.minio.directory.service;

import com.example.cloud_file_storage.common.MinioHelper;
import com.example.cloud_file_storage.modules.minio.dto.MinioDto;
import com.example.cloud_file_storage.modules.minio.dto.PathComponents;
import com.example.cloud_file_storage.modules.minio.dto.ResourceType;
import com.example.cloud_file_storage.modules.minio.service.PathResolverService;
import com.example.cloud_file_storage.modules.minio.service.UserPathService;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class DirectoryCreateService {
    private final MinioHelper minioHelper;
    private final PathResolverService resolverService;
    private final UserPathService pathService;
    @Autowired
    public DirectoryCreateService(MinioHelper minioHelper, PathResolverService resolverService, UserPathService pathService) {
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
}
