package com.example.cloud_file_storage.modules.minio.service;


import org.springframework.stereotype.Service;

@Service
public class UserPathService {

    public String toFullPath(Long id, String userPath) {
        String userRoot = getUserRootFolder(id);
        if (userPath == null || userPath.equals("/") || userPath.isEmpty()) {
            return userRoot;
        }
        return userRoot + userPath;
    }
    public String getUserFolder(Long id) {
        return getUserRootFolder(id);
    }

    private String getUserRootFolder(Long id) {
        return "user-" + id + "-files/";
    }
}
