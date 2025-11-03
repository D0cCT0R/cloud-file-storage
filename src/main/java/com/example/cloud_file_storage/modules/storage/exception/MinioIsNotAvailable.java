package com.example.cloud_file_storage.modules.storage.exception;

public class MinioIsNotAvailable extends RuntimeException {
    public MinioIsNotAvailable(String message, Throwable cause) {
        super(message, cause);
    }
}


