package com.example.cloud_file_storage.modules.storage.service.shared;


import com.example.cloud_file_storage.modules.storage.dto.storage.PathComponents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PathResolverService {

    private final UserPathService pathService;

    @Autowired
    public PathResolverService(UserPathService pathService) {
        this.pathService = pathService;
    }

    public String normalizeUserPath(String userPath) {
        String normalized = userPath;
        if(normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    public PathComponents extractPathComponents(String userPath) {
        if (userPath.isEmpty()) {
            return new PathComponents("/", "");
        }
        String path = userPath;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        boolean isDirectory = path.endsWith("/");
        if (isDirectory && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            if (isDirectory) {
                return new PathComponents("/", path + "/");
            } else {
                return new PathComponents("/", path);
            }
        } else {
            String parentPath = path.substring(0, lastSlashIndex) + "/";
            String name = path.substring(lastSlashIndex + 1);
            if (isDirectory) {
                name += "/";
            }
            return new PathComponents(parentPath, name);
        }
    }

    public List<String> getRelativePathsForZip(List<String> fullPathsFile, String fullPathDir) {
        List<String> relativePaths = new ArrayList<>();
        for (String path : fullPathsFile) {
            relativePaths.add(path.substring(fullPathDir.length()));
        }
        return relativePaths;
    }

    public String getRelativePath(Long userId, String fullFileOrDirectoryPath) {
        return fullFileOrDirectoryPath.substring(pathService.getUserFolder(userId).length());
    }

    public String extractFileName(String relativePath) {
        int lastSlash = relativePath.lastIndexOf('/');
        return (lastSlash == -1) ? relativePath : relativePath.substring(lastSlash + 1);
    }

    public String extractFolderName(String relativePath) {
        if (relativePath == null || relativePath.isEmpty() || relativePath.equals("/")) {
            return "";
        }
        String normalizedPath = relativePath.endsWith("/") ? relativePath : relativePath + "/";
        String path = normalizedPath.substring(0, normalizedPath.length() - 1);
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1) {
            return path;
        } else {
            return path.substring(lastSlash + 1);
        }
    }

    public String extractFirstComponent(String path) {
        int firstSlash = path.indexOf('/');
        return firstSlash != -1 ? path.substring(0, firstSlash + 1) : path;
    }

}


