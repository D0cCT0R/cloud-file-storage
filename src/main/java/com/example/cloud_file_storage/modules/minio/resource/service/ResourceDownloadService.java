package com.example.cloud_file_storage.modules.minio.resource.service;

import com.example.cloud_file_storage.common.MinioHelper;
import com.example.cloud_file_storage.modules.minio.exception.InvalidPathException;
import com.example.cloud_file_storage.modules.minio.service.PathResolverService;
import com.example.cloud_file_storage.modules.minio.service.ResourceValidationService;
import com.example.cloud_file_storage.modules.minio.service.UserPathService;
import com.example.cloud_file_storage.modules.minio.service.ZipService;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class ResourceDownloadService {
    private final ZipService zipService;
    private final MinioHelper minioHelper;
    private final ResourceValidationService validationService;
    private final PathResolverService resolverService;
    private final UserPathService pathService;

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

    public Resource downloadResource(String fullUserPath, Long userId) throws InvalidPathException, ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        validationService.validateUserPath(fullUserPath);
        String normalizePath = resolverService.normalizeUserPath(fullUserPath);
        String fullPath = pathService.toFullPath(userId, normalizePath);
        boolean isDirectory = fullPath.endsWith("/");
        if(isDirectory) {
            return downloadDirectory(fullPath);
        } else {
            return downloadFile(fullPath);
        }
    }

    private Resource downloadFile(String fullPath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioHelper.downloadFile(fullPath);
    }

    private Resource downloadDirectory(String fullPath) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<InputStreamResource> filesInDirectory = minioHelper.downloadAllFileInDirectory(fullPath);
        List<String> filesFullPaths = minioHelper.listObjectsInDirectory(fullPath, true);
        List<String> relativePaths = resolverService.getRelativePaths(filesFullPaths, fullPath);
        return zipService.createZip(filesInDirectory, relativePaths);
    }
}
