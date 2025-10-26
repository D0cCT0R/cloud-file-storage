package com.example.cloud_file_storage.modules.minio.resource.service;


import com.example.cloud_file_storage.common.MinioHelper;
import com.example.cloud_file_storage.modules.minio.dto.MinioDto;
import com.example.cloud_file_storage.modules.minio.dto.PathComponents;
import com.example.cloud_file_storage.modules.minio.dto.ResourceType;
import com.example.cloud_file_storage.modules.minio.exception.DirectoryOrFileNotFound;
import com.example.cloud_file_storage.modules.minio.exception.InvalidPathException;
import com.example.cloud_file_storage.modules.minio.exception.MinioIsNotAvailable;
import com.example.cloud_file_storage.modules.minio.service.PathResolverService;
import com.example.cloud_file_storage.modules.minio.service.ResourceValidationService;
import com.example.cloud_file_storage.modules.minio.service.UserPathService;
import io.minio.*;
import io.minio.errors.*;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ResourceInfoService {
    private final MinioHelper minioHelper;
    private final ResourceValidationService validationService;
    private final PathResolverService resolverService;
    private final UserPathService pathService;

    @Autowired
    public ResourceInfoService(ResourceValidationService validationService, PathResolverService resolverService, UserPathService pathService, MinioHelper minioHelper) {
        this.validationService = validationService;
        this.resolverService = resolverService;
        this.pathService = pathService;
        this.minioHelper = minioHelper;
    }

    public MinioDto getResourceInfo(String userPath, Long userId) throws InvalidPathException, DirectoryOrFileNotFound {
        log.info("Берем информацию о пути: {}", userPath);
        validationService.validateUserPath(userPath);
        String normalizedUserPath = resolverService.normalizeUserPath(userPath);
        String fullPath = pathService.toFullPath(userId, normalizedUserPath);
        boolean isDirectory = userPath.endsWith("/");
        MinioDto minioDto;
        if (isDirectory) {
            minioDto = getDirectoryInfo(fullPath, normalizedUserPath);
        } else {
            minioDto = getFileInfo(fullPath, normalizedUserPath);
        }
        log.info("Информация о пути {} успешна взята", userPath);
        return minioDto;
    }

    private MinioDto getFileInfo(String fullPath, String normalizedPath) throws DirectoryOrFileNotFound {
        try {
            StatObjectResponse stat = minioHelper.statObject(fullPath);
            PathComponents components = resolverService.extractPathComponents(normalizedPath);
            return new MinioDto(
                    components.parentPath(),
                    components.name(),
                    stat.size(),
                    ResourceType.FILE
            );
        } catch (ErrorResponseException e) {
            throw new DirectoryOrFileNotFound("Файл не найден");
        } catch (Exception e) {
            throw new MinioIsNotAvailable("Неизвестная ошибка");
        }
    }

    private MinioDto getDirectoryInfo(String fullPath, String normalizedPath) throws DirectoryOrFileNotFound {
        try {
            List<String> objects = minioHelper.listObjectsInDirectory(fullPath, true);
            if(objects.isEmpty()) {
                throw new DirectoryOrFileNotFound("Директория не найдена");
            }
            PathComponents components = resolverService.extractPathComponents(normalizedPath);
            return new MinioDto(
                    components.parentPath(),
                    components.name(),
                    null,
                    ResourceType.DIRECTORY
            );
        } catch (DirectoryOrFileNotFound e) {
                throw e;
        } catch (Exception e) {
            throw new MinioIsNotAvailable("Неизвестная ошибка");
        }
    }
}
