package com.example.cloud_file_storage.modules.storage.service.resource;

import com.example.cloud_file_storage.modules.storage.exception.InvalidPathException;
import com.example.cloud_file_storage.modules.storage.service.shared.*;
import com.example.cloud_file_storage.modules.storage.exception.DirectoryOrFileNotFound;
import com.example.cloud_file_storage.modules.storage.exception.MinioIsNotAvailable;
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
    private final UserPathResolver resolver;
    private final boolean RECURSIVE = true;

    public ResourceDeleteService(MinioHelper minioHelper, UserPathResolver resolver) {
        this.minioHelper = minioHelper;
        this.resolver = resolver;
    }

    public void deleteResource(String userPath, Long userId) throws Exception {
        try {
            log.info("Delete resource for user. Path {} , userID: {}", userPath, userId);
            String fullPath = resolver.resolveFullPath(userPath, userId);
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
