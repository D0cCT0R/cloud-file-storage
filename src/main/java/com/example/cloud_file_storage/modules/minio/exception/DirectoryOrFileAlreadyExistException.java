package com.example.cloud_file_storage.modules.minio.exception;

public class DirectoryOrFileAlreadyExistException extends Exception {
    public DirectoryOrFileAlreadyExistException(String message) {
        super(message);
    }
}
