package com.example.cloud_file_storage.core.advice;


import com.example.cloud_file_storage.modules.auth.exception.IncorrectLoginOrPasswordException;
import com.example.cloud_file_storage.modules.auth.exception.UserAlreadyExistException;
import com.example.cloud_file_storage.modules.minio.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExist(UserAlreadyExistException ex) {
        log.info("Attempt to register an existing user: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("User already exist"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.warn("Input data is incorrect: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Input data is incorrect"));
    }

    @ExceptionHandler(IncorrectLoginOrPasswordException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationEx(IncorrectLoginOrPasswordException ex) {
        log.warn("Authentication error {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Incorrect login or password"));
    }

    @ExceptionHandler(InvalidPathException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPath(InvalidPathException ex) {
        log.warn("Invalid path: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Validation error"));
    }

    @ExceptionHandler(DirectoryOrFileAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleFileAlreadyExist(DirectoryOrFileAlreadyExistException ex) {
        log.warn("Resource already exist: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Resource already exist"));
    }

    @ExceptionHandler(DirectoryOrFileNotFound.class)
    public ResponseEntity<ErrorResponse> handleNotFoundDirectoryOrFile(DirectoryOrFileNotFound ex) {
        log.warn("Resource search error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Resource not found"));
    }

    @ExceptionHandler(MinioIsNotAvailable.class)
    public ResponseEntity<ErrorResponse> handleMinioIsNotAvailable(MinioIsNotAvailable ex) {
        log.error("Minio unavailable", ex);
        return ResponseEntity.internalServerError().body(new ErrorResponse("Minio unavailable"));
    }

    @ExceptionHandler({DataAccessResourceFailureException.class, InitializeBucketException.class})
    public ResponseEntity<ErrorResponse> handleInternalServerException(Exception  ex) {
        log.error("Internal error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorResponse("Service unavailable, try later"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknownError(Exception ex) {
        log.error("Unknown error: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(new ErrorResponse("Unknown error, please try later"));
    }

}
