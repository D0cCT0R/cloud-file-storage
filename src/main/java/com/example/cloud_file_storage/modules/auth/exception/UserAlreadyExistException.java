package com.example.cloud_file_storage.modules.auth.exception;


public class UserAlreadyExistException extends RuntimeException{
    public UserAlreadyExistException(String message) {
        super(message);
    }
}


