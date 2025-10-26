package com.example.cloud_file_storage.modules.minio.resource.service;

import com.example.cloud_file_storage.common.MinioHelper;
import com.example.cloud_file_storage.modules.minio.exception.DirectoryOrFileNotFound;
import com.example.cloud_file_storage.modules.minio.exception.MinioIsNotAvailable;
import com.example.cloud_file_storage.modules.minio.service.PathResolverService;
import com.example.cloud_file_storage.modules.minio.service.ResourceValidationService;
import com.example.cloud_file_storage.modules.minio.service.UserPathService;
import io.minio.errors.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ResourceDeleteService {
    private final MinioHelper minioHelper;
    private final ResourceValidationService validationService;
    private final PathResolverService resolverService;
    private final UserPathService pathService;

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
        log.info("Удаление ресурса для пользователя");
        validationService.validateUserPath(userPath);
        String normalizedPath = resolverService.normalizeUserPath(userPath);
        String fullPath = pathService.toFullPath(userId, normalizedPath);
        boolean isDirectory = userPath.endsWith("/");
        if(isDirectory) {
            deleteDirectory(fullPath);
        } else {
            deleteFile(fullPath);
        }
    }

    private void deleteFile(String fullPath) throws DirectoryOrFileNotFound {
        try {
            minioHelper.removeObject(fullPath);
        } catch (ErrorResponseException e) {
            throw new DirectoryOrFileNotFound("Ошибка при удалении, файл не найден" + e);
        } catch (Exception e) {
            throw new MinioIsNotAvailable("Ошибка минио" + e);
        }
    }

    private void deleteDirectory(String fullPath) throws Exception {
        List<String> objectsToDelete = minioHelper.listObjectsInDirectory(fullPath, true);
        if(objectsToDelete.isEmpty()) {
            throw new DirectoryOrFileNotFound("Директория не найдена");
        }
        minioHelper.removeObjects(objectsToDelete);
    }
}
