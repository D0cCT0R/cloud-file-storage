package com.example.cloud_file_storage.exception.auth;

public class IncorrectLoginOrPasswordException extends RuntimeException {
    public IncorrectLoginOrPasswordException(String message) {
        super(message);
    }
}


