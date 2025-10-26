package com.example.cloud_file_storage.modules.minio.resource.exception;

public class DirectoryExistException extends Exception {
    public DirectoryExistException(String message) {
        super(message);
    }
}
