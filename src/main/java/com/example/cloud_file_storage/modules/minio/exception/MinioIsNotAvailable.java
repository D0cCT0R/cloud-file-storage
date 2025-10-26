package com.example.cloud_file_storage.modules.minio.exception;

public class MinioIsNotAvailable extends RuntimeException {
    public MinioIsNotAvailable(String message) {
        super(message);
    }
}
