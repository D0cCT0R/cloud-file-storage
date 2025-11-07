package com.example.cloud_file_storage.modules.storage.exception;

public class MinioIsNotAvailableException extends RuntimeException {
    public MinioIsNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}


