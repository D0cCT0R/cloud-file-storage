package com.example.cloud_file_storage.modules.resource.service;


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
    private String getUserRootFolder(Long id) {
        return "user-" + id + "-files/";
    }
}
