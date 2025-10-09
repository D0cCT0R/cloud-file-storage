package com.example.cloud_file_storage.modules.resource.service;


import com.example.cloud_file_storage.modules.resource.exception.InvalidPathException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ResourceValidationService {

    public void validateUserPath(String userPath) throws InvalidPathException {
        if(userPath == null) {
            throw new InvalidPathException("Путь не может быть null");
        }
        if(userPath.contains("//")) {
            throw new InvalidPathException("Неверный формат пути");
        }
        if(userPath.contains("..")) {
            throw new InvalidPathException("Path traversal запрещен");
        }
        if (userPath.startsWith("user-") && userPath.contains("-files")) {
            log.warn("Обнаружен технический путь: {}", userPath);
        }
    }
}
