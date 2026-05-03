package com.example.cloud_file_storage.exception.storage;

public class DirectoryOrFileNotFoundException extends RuntimeException {
    public DirectoryOrFileNotFoundException(String message) {
        super(message);
    }
}


