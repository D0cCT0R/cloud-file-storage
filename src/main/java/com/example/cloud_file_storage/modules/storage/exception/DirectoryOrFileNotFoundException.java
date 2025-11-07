package com.example.cloud_file_storage.modules.storage.exception;

public class DirectoryOrFileNotFoundException extends RuntimeException {
    public DirectoryOrFileNotFoundException(String message) {
        super(message);
    }
}


