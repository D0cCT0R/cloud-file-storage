package com.example.cloud_file_storage.modules.minio.service;


import com.example.cloud_file_storage.modules.minio.dto.PathComponents;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PathResolverService {

    public String normalizeUserPath(String userPath) {
        String normalized = userPath;
        if(normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    public PathComponents extractPathComponents(String normalizedPath) {
        if(normalizedPath.isEmpty()) {
            return new PathComponents("/", "");
        }
        String path = normalizedPath;
        if(path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if(path.startsWith("/")) {
            path = path.substring(1);
        }
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return new PathComponents("/", path);
        } else {
            String parentPath = path.substring(0, lastSlashIndex) + "/";
            String name = path.substring(lastSlashIndex + 1);
            return new PathComponents(parentPath, name);
        }
    }

    public List<String> getRelativePaths(List<String> fullPathsFile, String fullPathDirectory) {
        List<String> relativePaths = new ArrayList<>();
        for (String path : fullPathsFile) {
            relativePaths.add(getRelativePath(fullPathDirectory, path));
        }
        return relativePaths;
    }

    public String getRelativePath(String userDirectory, String fullFileOrDirectoryPath) {
        return fullFileOrDirectoryPath.substring(userDirectory.length());
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
}
