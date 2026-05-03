package com.example.cloud_file_storage.exception.storage;

public class MinioIsNotAvailableException extends RuntimeException {
    public MinioIsNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}


