package com.example.cloud_file_storage.modules.minio.exception;

public class DirectoryOrFileNotFound extends Exception {
    public DirectoryOrFileNotFound(String message) {
        super(message);
    }
}
