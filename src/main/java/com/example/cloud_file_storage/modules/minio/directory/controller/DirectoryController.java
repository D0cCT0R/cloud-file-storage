package com.example.cloud_file_storage.modules.minio.directory.controller;

import com.example.cloud_file_storage.modules.auth.entity.User;
import com.example.cloud_file_storage.modules.minio.exception.DirectoryOrFileNotFound;
import com.example.cloud_file_storage.modules.minio.service.CreateDirectoryService;
import com.example.cloud_file_storage.modules.minio.directory.service.DirectoryInfoService;
import com.example.cloud_file_storage.modules.minio.dto.MinioDto;
import com.example.cloud_file_storage.modules.minio.exception.InvalidPathException;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Controller
@RequestMapping("/directory")
public class DirectoryController {

    private final DirectoryInfoService directoryInfoService;
    private final CreateDirectoryService createDirectoryService;

    @Autowired
    public DirectoryController(DirectoryInfoService directoryInfoService, CreateDirectoryService createDirectoryService) {
        this.directoryInfoService = directoryInfoService;
        this.createDirectoryService = createDirectoryService;
    }

    @GetMapping()
    public ResponseEntity<?> getDirectoryContent(@RequestParam String path, @AuthenticationPrincipal User user) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InvalidPathException, InternalException, DirectoryOrFileNotFound {
        List<MinioDto> directoryContent = directoryInfoService.getDirectoryInfo(path, user.getId());
        return ResponseEntity.ok().body(directoryContent);
    }

    @PostMapping
    public ResponseEntity<?> createFolder(@RequestParam String path, @AuthenticationPrincipal User user) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        MinioDto directory = createDirectoryService.createDirectory(path, user.getId());
        return ResponseEntity.ok(directory);
    }
}
