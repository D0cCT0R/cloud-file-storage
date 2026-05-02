package com.example.cloud_file_storage.service.storage.resource;

import com.example.cloud_file_storage.exception.storage.InvalidPathException;
import com.example.cloud_file_storage.exception.storage.DirectoryOrFileNotFoundException;
import com.example.cloud_file_storage.exception.storage.MinioIsNotAvailableException;
import com.example.cloud_file_storage.util.MinioHelper;
import com.example.cloud_file_storage.util.UserPathResolver;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class ResourceDeleteService {
    private final MinioHelper minioHelper;
    private final UserPathResolver resolver;
    private final boolean RECURSIVE = true;

    public ResourceDeleteService(MinioHelper minioHelper, UserPathResolver resolver) {
        this.minioHelper = minioHelper;
        this.resolver = resolver;
    }
    @WithSpan
    public void deleteResource(String userPath, Long userId) {
        try {
            log.info("Delete resource for user. Path {} , userID: {}", userPath, userId);
            String fullPath = resolver.resolveFullPath(userPath, userId);
            if (!minioHelper.objectExist(fullPath)) {
                throw new DirectoryOrFileNotFoundException("Resource not found");
            }
            if (minioHelper.isDirectory(fullPath)) {
                deleteDirectory(fullPath);
            } else {
                deleteFile(fullPath);
            }
        } catch (DirectoryOrFileNotFoundException | InvalidPathException e) {
            throw e;
        } catch (Exception e) {
            throw new MinioIsNotAvailableException("Minio is not available", e);
        }
    }
    @WithSpan
    private void deleteFile(String fullPath) throws Exception {
        minioHelper.removeObject(fullPath);
        log.debug("Successfully delete file. Path: {}", fullPath);
    }
    @WithSpan
    private void deleteDirectory(String fullPath) throws Exception {
        List<String> objectsToDelete = minioHelper.listObjectsInDirectory(fullPath, RECURSIVE);
        minioHelper.removeObjects(objectsToDelete);
        log.debug("Successfully delete directory. Path: {}", fullPath);
    }
}


