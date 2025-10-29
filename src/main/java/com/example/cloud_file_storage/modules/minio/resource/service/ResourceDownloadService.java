package com.example.cloud_file_storage.modules.minio.resource.service;

import com.example.cloud_file_storage.modules.minio.exception.DirectoryOrFileNotFound;
import com.example.cloud_file_storage.modules.minio.exception.MinioIsNotAvailable;
import com.example.cloud_file_storage.modules.minio.service.MinioHelper;
import com.example.cloud_file_storage.modules.minio.exception.InvalidPathException;
import com.example.cloud_file_storage.modules.minio.service.PathResolverService;
import com.example.cloud_file_storage.modules.minio.service.ResourceValidationService;
import com.example.cloud_file_storage.modules.minio.service.UserPathService;
import com.example.cloud_file_storage.modules.minio.service.ZipService;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
@Slf4j
public class ResourceDownloadService {
    private final ZipService zipService;
    private final MinioHelper minioHelper;
    private final ResourceValidationService validationService;
    private final PathResolverService resolverService;
    private final UserPathService pathService;
    private final boolean RECURSIVE = true;

    @Autowired
    public ResourceDownloadService(ZipService zipService, MinioHelper minioHelper,
                                   ResourceValidationService validationService,
                                   PathResolverService resolverService,
                                   UserPathService pathService) {
        this.zipService = zipService;
        this.minioHelper = minioHelper;
        this.validationService = validationService;
        this.resolverService = resolverService;
        this.pathService = pathService;
    }

    public Resource downloadResource(String userPath, Long userId) throws DirectoryOrFileNotFound, InvalidPathException {
        try {
            log.info("Download resource for user.Path: {} , userID: {}", userPath, userId);
            validationService.validateUserPath(userPath);
            String normalizePath = resolverService.normalizeUserPath(userPath);
            String fullPath = pathService.toFullPath(userId, normalizePath);
            if(!minioHelper.objectExist(fullPath)) {
                throw new DirectoryOrFileNotFound("Directory or file not found");
            }
            if (minioHelper.isDirectory(fullPath)) {
                return downloadDirectory(fullPath);
            } else {
                return downloadFile(fullPath);
            }
        } catch (InvalidPathException | DirectoryOrFileNotFound e) {
            throw e;
        } catch (Exception e) {
            throw new MinioIsNotAvailable("Minio is not available", e);
        }
    }

    private Resource downloadFile(String fullPath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Resource resource = minioHelper.downloadFile(fullPath);
        log.debug("Download file complete successfully. Path: {}", fullPath);
        return resource;
    }

    private Resource downloadDirectory(String fullPath) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<InputStreamResource> filesInDirectory = minioHelper.downloadAllFileInDirectory(fullPath);
        List<String> filesFullPaths = minioHelper.listObjectsInDirectory(fullPath, RECURSIVE);
        List<String> relativePaths = resolverService.getRelativePaths(filesFullPaths, fullPath);
        Resource resource = zipService.createZip(filesInDirectory, relativePaths);
        log.debug("Download directory complete successfully. Path: {}", fullPath);
        return resource;
    }
}
