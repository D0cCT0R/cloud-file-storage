package com.example.cloud_file_storage.common.web.advice;


import com.example.cloud_file_storage.modules.auth.exception.IncorrectLoginOrPasswordException;
import com.example.cloud_file_storage.modules.auth.exception.UserAlreadyExistException;
import com.example.cloud_file_storage.modules.storage.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExist(UserAlreadyExistException ex) {
        log.info("Attempt to register an existing user: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("User already exist"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.warn("Input data is incorrect: {}", ex.getMessage());
        return new ErrorResponse("Input data is incorrect");
    }

    @ExceptionHandler(IncorrectLoginOrPasswordException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthenticationEx(IncorrectLoginOrPasswordException ex) {
        log.warn("Authentication error {}", ex.getMessage());
        return new ErrorResponse("Incorrect login or password");
    }

    @ExceptionHandler(InvalidPathException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidPath(InvalidPathException ex) {
        log.warn("Invalid path: {}", ex.getMessage());
        return new ErrorResponse("Validation error");
    }

    @ExceptionHandler(DirectoryOrFileAlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleFileAlreadyExist(DirectoryOrFileAlreadyExistException ex) {
        log.warn("Resource already exist: {}", ex.getMessage());
        return new ErrorResponse("Resource already exist");
    }

    @ExceptionHandler(DirectoryOrFileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundDirectoryOrFile(DirectoryOrFileNotFoundException ex) {
        log.warn("Resource search error: {}", ex.getMessage());
        return new ErrorResponse("Resource not found");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMaxSizeException(
            MaxUploadSizeExceededException ex) {
        log.warn("File size limit exceeded: {}", ex.getMessage());
        return new ErrorResponse("File size limit 100KB");
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleMultipartException(MultipartException ex) {
        log.error("Multipart error", ex);
        return new ErrorResponse("Upload file error");
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthenticationException(AuthenticationException ex) {
        log.warn("User is not authorize", ex);
        return new ErrorResponse("Not authorize");
    }

    @ExceptionHandler(MinioIsNotAvailableException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleMinioIsNotAvailable(MinioIsNotAvailableException ex) {
        log.error("Minio unavailable", ex);
        return new ErrorResponse("Minio unavailable");
    }

    @ExceptionHandler({DataAccessResourceFailureException.class, InitializeBucketException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleInternalServerException(Exception ex) {
        log.error("Internal error: {}", ex.getMessage(), ex);
        return new ErrorResponse("Service unavailable, try later");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnknownError(Exception ex) {
        log.error("Unknown error: {}", ex.getMessage(), ex);
        return new ErrorResponse("Unknown error, please try later");
    }

}


