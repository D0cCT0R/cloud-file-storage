package com.example.cloud_file_storage.exception.auth;


public class UserAlreadyExistException extends RuntimeException{
    public UserAlreadyExistException(String message) {
        super(message);
    }
}


