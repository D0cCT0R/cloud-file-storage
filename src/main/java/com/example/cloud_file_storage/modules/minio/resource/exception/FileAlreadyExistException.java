package com.example.cloud_file_storage.modules.minio.resource.exception;

public class FileAlreadyExistException extends Exception {
    public FileAlreadyExistException(String message) {
        super(message);
    }
}
