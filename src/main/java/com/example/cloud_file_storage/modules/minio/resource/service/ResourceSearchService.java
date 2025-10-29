package com.example.cloud_file_storage.modules.minio.resource.service;


import com.example.cloud_file_storage.modules.minio.exception.MinioIsNotAvailable;
import com.example.cloud_file_storage.modules.minio.service.MinioHelper;
import com.example.cloud_file_storage.modules.minio.dto.MinioDto;
import com.example.cloud_file_storage.modules.minio.dto.PathComponents;
import com.example.cloud_file_storage.modules.minio.dto.ResourceType;
import com.example.cloud_file_storage.modules.minio.service.PathResolverService;
import com.example.cloud_file_storage.modules.minio.service.UserPathService;
import io.minio.StatObjectResponse;
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
public class ResourceSearchService {

    private final PathResolverService pathResolverService;
    private final MinioHelper minioHelper;
    private final UserPathService userPathService;
    private final boolean RECURSIVE = true;

    @Autowired
    public ResourceSearchService(PathResolverService pathResolverService, MinioHelper minioHelper, UserPathService userPathService) {
        this.pathResolverService = pathResolverService;
        this.minioHelper = minioHelper;
        this.userPathService = userPathService;
    }

    public List<MinioDto> search(String query, Long userId) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        try {
            log.info("Start search. Query: {}, userID: {}", query, userId);
            List<MinioDto> searchResults = new ArrayList<>();
            String userDirectory = userPathService.getUserFolder(userId);
            List<String> allUserObjects = minioHelper.listObjectsInDirectory(userDirectory, RECURSIVE);
            for (String object : allUserObjects) {
                if (object.endsWith("/")) {
                    String folderName = pathResolverService.extractFolderName(object);
                    if (folderName.toLowerCase().contains(query.toLowerCase())) {
                        String relative = pathResolverService.getRelativePath(userDirectory, object);
                        PathComponents components = pathResolverService.extractPathComponents(relative);
                        searchResults.add(MinioDto.builder()
                                .path(components.parentPath())
                                .name(components.name())
                                .type(ResourceType.DIRECTORY)
                                .build());
                    }
                } else {
                    String fileName = pathResolverService.extractFileName(object);
                    if (fileName.toLowerCase().contains(query.toLowerCase())) {
                        String relativePath = pathResolverService.getRelativePath(userDirectory, object);
                        PathComponents components = pathResolverService.extractPathComponents(relativePath);
                        StatObjectResponse stat = minioHelper.statObject(object);
                        searchResults.add(MinioDto.builder()
                                .path(components.parentPath())
                                .name(components.name())
                                .size(stat.size())
                                .type(ResourceType.FILE)
                                .build());
                    }
                }
                log.debug("Search result add to list. Path: {}, userID: {}", object, userId);
            }
            log.debug("Search complete successfully. Query: {}, userID: {}, size: {}", query, userId, searchResults.size());
            return searchResults;
        } catch (Exception e) {
            throw new MinioIsNotAvailable("Minio is not available", e);
        }
    }
}
