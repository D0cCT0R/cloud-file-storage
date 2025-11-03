package com.example.cloud_file_storage.modules.storage.service.shared;


import com.example.cloud_file_storage.modules.storage.exception.InvalidPathException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ResourceValidationService {

    public void validateUserPath(String userPath) throws InvalidPathException {
        if(userPath.contains("\\")) {
            throw new InvalidPathException("Path not contains \\");
        }
        if(userPath.contains("//")) {
            throw new InvalidPathException("Invalid format");
        }
        if(userPath.contains("..")) {
            throw new InvalidPathException("Path traversal");
        }
        if (userPath.startsWith("user-") && userPath.contains("-files")) {
            log.warn("Discovered a technical path: {}", userPath);
        }
    }
}


