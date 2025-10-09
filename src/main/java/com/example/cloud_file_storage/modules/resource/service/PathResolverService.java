package com.example.cloud_file_storage.modules.resource.service;


import com.example.cloud_file_storage.modules.resource.dto.PathComponents;
import org.springframework.stereotype.Service;

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
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return new PathComponents("/", path);
        } else {
            String parentPath = "/" + path.substring(0, lastSlashIndex) + "/";
            String name = path.substring(lastSlashIndex + 1);
            return new PathComponents(parentPath, name);
        }
    }
}
