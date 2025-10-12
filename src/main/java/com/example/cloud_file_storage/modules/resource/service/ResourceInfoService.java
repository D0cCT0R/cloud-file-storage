package com.example.cloud_file_storage.modules.resource.service;


import com.example.cloud_file_storage.modules.resource.dto.MinioDto;
import com.example.cloud_file_storage.modules.resource.dto.PathComponents;
import com.example.cloud_file_storage.modules.resource.dto.ResourceType;
import com.example.cloud_file_storage.modules.resource.exception.DirectoryOrFileNotFound;
import com.example.cloud_file_storage.modules.resource.exception.InvalidPathException;
import com.example.cloud_file_storage.modules.resource.exception.MinioIsNotAvailable;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ResourceInfoService {
    private final MinioClient minioClient;
    private final ResourceValidationService validationService;
    private final PathResolverService resolverService;
    private final UserPathService pathService;
    private final String bucketName;

    @Autowired
    public ResourceInfoService(ResourceValidationService validationService, PathResolverService resolverService, UserPathService pathService, @Value("${minio.bucket-name}") String bucketName, MinioClient minioClient) {
        this.validationService = validationService;
        this.resolverService = resolverService;
        this.pathService = pathService;
        this.minioClient = minioClient;
        this.bucketName = bucketName;
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
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fullPath)
                            .build()
            );
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
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(fullPath)
                            .maxKeys(1)
                            .build()
            );
            if(!results.iterator().hasNext()) {
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
