package com.example.cloud_file_storage.modules.minio.resource.service;

import com.example.cloud_file_storage.modules.minio.exception.InvalidPathException;
import com.example.cloud_file_storage.modules.minio.service.MinioHelper;
import com.example.cloud_file_storage.modules.minio.exception.DirectoryOrFileNotFound;
import com.example.cloud_file_storage.modules.minio.exception.MinioIsNotAvailable;
import com.example.cloud_file_storage.modules.minio.service.PathResolverService;
import com.example.cloud_file_storage.modules.minio.service.ResourceValidationService;
import com.example.cloud_file_storage.modules.minio.service.UserPathService;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Slf4j
@Service
public class ResourceDeleteService {
    private final MinioHelper minioHelper;
    private final ResourceValidationService validationService;
    private final PathResolverService resolverService;
    private final UserPathService pathService;
    private final boolean RECURSIVE = true;

    public ResourceDeleteService(MinioHelper minioHelper,
                                 ResourceValidationService validationService,
                                 PathResolverService resolverService,
                                 UserPathService pathService) {
        this.minioHelper = minioHelper;
        this.validationService = validationService;
        this.resolverService = resolverService;
        this.pathService = pathService;
    }


    public void deleteResource(String userPath, Long userId) throws Exception {
        try {
            log.info("Delete resource for user. Path {} , userID: {}", userPath, userId);
            validationService.validateUserPath(userPath);
            String normalizedPath = resolverService.normalizeUserPath(userPath);
            String fullPath = pathService.toFullPath(userId, normalizedPath);
            if (!minioHelper.objectExist(fullPath)) {
                throw new DirectoryOrFileNotFound("Resource not found");
            }
            if (minioHelper.isDirectory(fullPath)) {
                deleteDirectory(fullPath);
            } else {
                deleteFile(fullPath);
            }
        } catch (DirectoryOrFileNotFound | InvalidPathException e) {
            throw e;
        } catch (Exception e) {
            throw new MinioIsNotAvailable("Minio is not available", e);
        }
    }

    private void deleteFile(String fullPath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioHelper.removeObject(fullPath);
        log.debug("Successfully delete file. Path: {}", fullPath);
    }

    private void deleteDirectory(String fullPath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<String> objectsToDelete = minioHelper.listObjectsInDirectory(fullPath, RECURSIVE);
        minioHelper.removeObjects(objectsToDelete);
        log.debug("Successfully delete directory. Path: {}", fullPath);
    }
}
