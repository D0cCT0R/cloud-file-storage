package com.example.cloud_file_storage.modules.storage.service.directory;

import com.example.cloud_file_storage.modules.storage.dto.storage.MinioDto;
import com.example.cloud_file_storage.modules.storage.dto.storage.PathComponents;
import com.example.cloud_file_storage.modules.storage.dto.storage.ResourceType;
import com.example.cloud_file_storage.modules.storage.exception.DirectoryOrFileAlreadyExistException;
import com.example.cloud_file_storage.modules.storage.exception.FailInitializeUserRootDirectory;
import com.example.cloud_file_storage.modules.storage.exception.InvalidPathException;
import com.example.cloud_file_storage.modules.storage.exception.MinioIsNotAvailable;
import com.example.cloud_file_storage.modules.storage.service.shared.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DirectoryCreationService {

    private final MinioHelper minioHelper;
    private final PathResolverService resolverService;
    private final UserPathService pathService;
    private final UserPathResolver resolver;
    @Autowired
    public DirectoryCreationService(MinioHelper minioHelper, PathResolverService resolverService, UserPathService pathService, UserPathResolver resolver) {
        this.minioHelper = minioHelper;
        this.resolverService = resolverService;
        this.pathService = pathService;
        this.resolver = resolver;
    }

    public MinioDto createDirectory(String path, Long id) throws InvalidPathException, DirectoryOrFileAlreadyExistException {
        try {
            log.info("Creating directory for user. Path: {} , userID: {}", path, id);
            String fullPath = resolver.resolveFullPath(path, id);
            if(!minioHelper.isDirectory(fullPath)) {
                throw new InvalidPathException("Incorrect path");
            }
            if(minioHelper.objectExist(fullPath)) {
                throw new DirectoryOrFileAlreadyExistException("Directory already exist");
            }
            minioHelper.createDirectory(fullPath);
            String relativePath = resolverService.getRelativePath(id, fullPath);
            PathComponents components = resolverService.extractPathComponents(relativePath);
            return MinioDto.builder()
                    .path(components.parentPath())
                    .name(components.name())
                    .type(ResourceType.DIRECTORY)
                    .build();
        } catch (InvalidPathException | DirectoryOrFileAlreadyExistException e) {
            throw e;
        } catch (Exception e) {
            throw new MinioIsNotAvailable("Minio is not available", e);
        }
    }

    public void createUserDirectory(Long userId) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, FailInitializeUserRootDirectory {
        String userDir = pathService.getUserFolder(userId);
        if(minioHelper.objectExist(userDir)) {
            throw new FailInitializeUserRootDirectory("Fail to init user directory");
        }
        minioHelper.createDirectory(userDir);
    }

    public List<MinioDto> createParentDirectories(String filePath, String userPath, Long id) {
        try {
            List<MinioDto> results = new ArrayList<>();
            if (!filePath.contains("/")) {
                return results;
            }
            String basePath = resolver.resolveFullPath(userPath, id);
            String[] pathComponents = filePath.split("/");
            StringBuilder currentPath = new StringBuilder(basePath);
            for (int i = 0; i < pathComponents.length - 1; i++) {
                currentPath.append(pathComponents[i]).append("/");
                String directoryPath = currentPath.toString();
                String relativePath = resolverService.getRelativePath(id, directoryPath);
                if (!minioHelper.objectExist(directoryPath)) {
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
        } catch (Exception e) {
            throw new MinioIsNotAvailable("Minio is not available", e);
        }
    }
}
