package com.example.cloud_file_storage.exception.storage;

public class DirectoryOrFileAlreadyExistException extends RuntimeException {
    public DirectoryOrFileAlreadyExistException(String message) {
        super(message);
    }
}


