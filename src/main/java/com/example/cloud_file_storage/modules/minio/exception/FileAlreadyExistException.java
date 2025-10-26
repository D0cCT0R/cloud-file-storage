package com.example.cloud_file_storage.modules.minio.exception;

public class FileAlreadyExistException extends Exception {
    public FileAlreadyExistException(String message) {
        super(message);
    }
}
