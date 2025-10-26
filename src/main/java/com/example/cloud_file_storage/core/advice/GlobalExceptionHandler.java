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
        log.info("Попытка регистрации существующего пользователя");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.warn("Ошибка валидации входных данных: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IncorrectLoginOrPasswordException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationEx(IncorrectLoginOrPasswordException ex) {
        log.warn("Ошибка аутентификации {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvalidPathException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPath(InvalidPathException ex) {
        log.warn("Неверный формат пути");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(FileAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleFileAlreadyExist(FileAlreadyExistException ex) {
        log.warn("Файл уже существует {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(DirectoryOrFileNotFound.class)
    public ResponseEntity<ErrorResponse> handleNotFoundDirectoryOrFile(DirectoryOrFileNotFound ex) {
        log.warn("Ошибка поиска директории {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Ресурс не найден"));
    }

    @ExceptionHandler(MinioIsNotAvailable.class)
    public ResponseEntity<ErrorResponse> handleMinioIsNotAvailable(MinioIsNotAvailable ex) {
        log.error("Минио недоступен: {}", ex.getMessage());
        return ResponseEntity.internalServerError().body(new ErrorResponse("Неизвестная ошибка"));
    }

    @ExceptionHandler({DataAccessResourceFailureException.class, InitializeBucketException.class})
    public ResponseEntity<ErrorResponse> handleInternalServerException(Exception  ex) {
        log.error("Внутренняя ошибка: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorResponse("Сервис временно недоступен, попробуйте позже"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknownError(Exception ex) {
        log.error("Обнаружена неизвестная ошибка: {}", ex.getMessage());
        return ResponseEntity.internalServerError().body(new ErrorResponse("Неизвестная ошибка"));
    }

}
