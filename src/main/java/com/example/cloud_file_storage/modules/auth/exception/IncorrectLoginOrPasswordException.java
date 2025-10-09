package com.example.cloud_file_storage.modules.auth.exception;

public class IncorrectLoginOrPasswordException extends Exception {
    public IncorrectLoginOrPasswordException(String message) {
        super(message);
    }
}
