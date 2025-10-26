package com.example.cloud_file_storage.modules.minio.resource.service;


import com.example.cloud_file_storage.common.MinioHelper;
import com.example.cloud_file_storage.modules.minio.dto.MinioDto;
import com.example.cloud_file_storage.modules.minio.dto.PathComponents;
import com.example.cloud_file_storage.modules.minio.dto.ResourceType;
import com.example.cloud_file_storage.modules.minio.service.PathResolverService;
import com.example.cloud_file_storage.modules.minio.service.UserPathService;
import io.minio.StatObjectResponse;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResourceSearchService {

    private final PathResolverService pathResolverService;
    private final MinioHelper minioHelper;
    private final UserPathService userPathService;

    @Autowired
    public ResourceSearchService(PathResolverService pathResolverService, MinioHelper minioHelper, UserPathService userPathService) {
        this.pathResolverService = pathResolverService;
        this.minioHelper = minioHelper;
        this.userPathService = userPathService;
    }

    public List<MinioDto> search(String query, Long userId) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<MinioDto> searchResults =  new ArrayList<>();
        String path = userPathService.getUserFolder(userId);
        List<String> allUserObjects = minioHelper.listObjectsInDirectory(path, true);
        for(String object: allUserObjects){
            if(object.endsWith("/")) {
                String fileName = pathResolverService.extractFolderName(object);
                if(fileName.toLowerCase().contains(query.toLowerCase())) {
                    String relative = pathResolverService.getRelativePath(path, object);
                    PathComponents components = pathResolverService.extractPathComponents(relative);
                    searchResults.add(new MinioDto(components.parentPath(), components.name(), null, ResourceType.DIRECTORY));
                }
            } else {
                String fileName = pathResolverService.extractFileName(object);
                if (fileName.toLowerCase().contains(query.toLowerCase())) {
                    String relative = pathResolverService.getRelativePath(path, object);
                    PathComponents components = pathResolverService.extractPathComponents(relative);
                    StatObjectResponse stat = minioHelper.statObject(object);
                    searchResults.add(new MinioDto(components.parentPath(), components.name(), stat.size(), ResourceType.FILE));
                }
            }
        }
        return searchResults;
    }
}
