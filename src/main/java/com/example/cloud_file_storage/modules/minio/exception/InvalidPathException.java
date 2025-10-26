package com.example.cloud_file_storage.modules.minio.exception;

public class InvalidPathException extends Exception {
    public InvalidPathException(String message) {
        super(message);
    }
}
