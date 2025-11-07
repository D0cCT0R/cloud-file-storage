package com.example.cloud_file_storage.modules.storage.service.shared;


import com.example.cloud_file_storage.modules.storage.dto.storage.PathComponents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PathResolverService {

    private final UserPathService pathService;
    private static final String ROOT_PATH = "/";
    private static final String EMPTY_STRING = "";
    private static final String DIRECTORY_SEPARATOR = "/";
    private static final int NOT_FOUND = -1;
    private static final int START_INDEX = 0;
    private static final int SINGLE_CHARACTER = 1;
    private static final int DIRECTORY_SUFFIX_LENGTH = 1;

    @Autowired
    public PathResolverService(UserPathService pathService) {
        this.pathService = pathService;
    }

    public String normalizeUserPath(String userPath) {
        String normalized = userPath;
        if (normalized.startsWith(DIRECTORY_SEPARATOR)) {
            normalized = normalized.substring(SINGLE_CHARACTER);
        }
        return normalized;
    }

    public PathComponents extractPathComponents(String userPath) {
        if (userPath.isEmpty()) {
            return new PathComponents(ROOT_PATH, EMPTY_STRING);
        }

        String path = userPath;
        if (path.startsWith(DIRECTORY_SEPARATOR)) {
            path = path.substring(SINGLE_CHARACTER);
        }

        boolean isDirectory = path.endsWith(DIRECTORY_SEPARATOR);
        if (isDirectory && path.length() > SINGLE_CHARACTER) {
            path = path.substring(START_INDEX, path.length() - DIRECTORY_SUFFIX_LENGTH);
        }

        int lastSlashIndex = path.lastIndexOf(DIRECTORY_SEPARATOR);
        if (lastSlashIndex == NOT_FOUND) {
            if (isDirectory) {
                return new PathComponents(ROOT_PATH, path + DIRECTORY_SEPARATOR);
            } else {
                return new PathComponents(ROOT_PATH, path);
            }
        } else {
            String parentPath = path.substring(START_INDEX, lastSlashIndex) + DIRECTORY_SEPARATOR;
            String name = path.substring(lastSlashIndex + SINGLE_CHARACTER);
            if (isDirectory) {
                name += DIRECTORY_SEPARATOR;
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
        int lastSlash = relativePath.lastIndexOf(DIRECTORY_SEPARATOR);
        return (lastSlash == NOT_FOUND) ? relativePath : relativePath.substring(lastSlash + SINGLE_CHARACTER);
    }

    public String extractFolderName(String relativePath) {
        if (relativePath == null || relativePath.isEmpty() || relativePath.equals(ROOT_PATH)) {
            return EMPTY_STRING;
        }

        String normalizedPath = relativePath.endsWith(DIRECTORY_SEPARATOR) ? relativePath : relativePath + DIRECTORY_SEPARATOR;
        String path = normalizedPath.substring(START_INDEX, normalizedPath.length() - DIRECTORY_SUFFIX_LENGTH);
        int lastSlash = path.lastIndexOf(DIRECTORY_SEPARATOR);

        if (lastSlash == NOT_FOUND) {
            return path;
        } else {
            return path.substring(lastSlash + SINGLE_CHARACTER);
        }
    }

    public String extractFirstComponent(String path) {
        int firstSlash = path.indexOf(DIRECTORY_SEPARATOR);
        return firstSlash != NOT_FOUND ? path.substring(START_INDEX, firstSlash + SINGLE_CHARACTER) : path;
    }
}


