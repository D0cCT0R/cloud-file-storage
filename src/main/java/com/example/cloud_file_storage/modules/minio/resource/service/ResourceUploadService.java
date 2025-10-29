package com.example.cloud_file_storage.modules.minio.resource.service;

import com.example.cloud_file_storage.modules.minio.exception.DirectoryOrFileAlreadyExistException;
import com.example.cloud_file_storage.modules.minio.exception.InvalidPathException;
import com.example.cloud_file_storage.modules.minio.exception.MinioIsNotAvailable;
import com.example.cloud_file_storage.modules.minio.service.*;
import com.example.cloud_file_storage.modules.minio.dto.MinioDto;
import com.example.cloud_file_storage.modules.minio.dto.PathComponents;
import com.example.cloud_file_storage.modules.minio.dto.ResourceType;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ResourceUploadService {

    private final PathResolverService resolverService;
    private final UserPathService pathService;
    private final MinioHelper helper;
    private final CreateDirectoryService createService;
    private final ResourceValidationService validationService;

    @Autowired
    public ResourceUploadService(PathResolverService resolverService, UserPathService pathService, MinioHelper helper, CreateDirectoryService createService, ResourceValidationService validationService) {
        this.resolverService = resolverService;
        this.pathService = pathService;
        this.helper = helper;
        this.createService = createService;
        this.validationService = validationService;
    }

    public List<MinioDto> upload(String path, Long id, List<MultipartFile> files) throws Exception {
        try {
            log.info("Upload user file. Path: {}, userID: {}", path, id);
            if (files.isEmpty()) {
                throw new Exception("Upload error");
            }
            validationService.validateUserPath(path);
            String normalizedUserPath = resolverService.normalizeUserPath(path);
            String firstComponent = resolverService.extractFirstComponent(files.getFirst().getOriginalFilename());
            String relativeFirstComponentPath = normalizedUserPath + firstComponent;
            String fullFirstComponentPath = pathService.toFullPath(id, relativeFirstComponentPath);
            if (helper.objectExist(fullFirstComponentPath)) {
                throw new DirectoryOrFileAlreadyExistException("Resource already exist");
            }
            if (helper.isDirectory(fullFirstComponentPath)) {
                return uploadDirectory(files, id, normalizedUserPath);
            }
            return uploadFile(fullFirstComponentPath, files.getFirst().getInputStream(), files.getFirst().getSize(), id);
        } catch (InvalidPathException | DirectoryOrFileAlreadyExistException e) {
            throw e;
        }
        catch (Exception e) {
            throw new MinioIsNotAvailable("Minio is not available", e);
        }
    }

    private List<MinioDto> uploadFile(String fullFilePathToUpload, InputStream fileInputStream, Long fileSize, Long id) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<MinioDto> uploadedFiles = new ArrayList<>();
        helper.putObject(fullFilePathToUpload, fileInputStream, fileSize);
        String userRootFolder = pathService.getUserFolder(id);
        String relativePath = resolverService.getRelativePath(userRootFolder, fullFilePathToUpload);
        PathComponents components = resolverService.extractPathComponents(relativePath);
        uploadedFiles.add(MinioDto.builder()
                .path(components.parentPath())
                .name(components.name())
                .size(fileSize)
                .type(ResourceType.FILE)
                .build());
        log.debug("Upload file complete successfully. Path: {}, userID: {}", fullFilePathToUpload, id);
        return uploadedFiles;
    }

    private List<MinioDto> uploadDirectory(List<MultipartFile> files, Long id, String normalizedUserPath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<MinioDto> uploadedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            List<MinioDto> directories = createService.createParentDirectories(file.getOriginalFilename(), normalizedUserPath, id);
            uploadedFiles.addAll(directories);
            Long fileSize = file.getSize();
            String relativeFilePath = normalizedUserPath + file.getOriginalFilename();
            String fullFilePath = pathService.toFullPath(id, relativeFilePath);
            helper.putObject(fullFilePath, file.getInputStream(), fileSize);
            PathComponents components = resolverService.extractPathComponents(relativeFilePath);
            uploadedFiles.add(MinioDto.builder()
                    .path(components.parentPath())
                    .name(components.name())
                    .size(fileSize)
                    .type(ResourceType.FILE)
                    .build());
        }
        log.debug("Upload directory complete successfully. Path: {}, userID: {}", normalizedUserPath, id);
        return uploadedFiles;
    }
}
