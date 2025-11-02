package com.example.cloud_file_storage.modules.storage.exception;

public class DirectoryOrFileAlreadyExistException extends Exception {
    public DirectoryOrFileAlreadyExistException(String message) {
        super(message);
    }
}
