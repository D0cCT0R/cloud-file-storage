package com.example.cloud_file_storage.modules.minio.resource.controller;

import com.example.cloud_file_storage.modules.auth.entity.User;
import com.example.cloud_file_storage.modules.minio.dto.MinioDto;
import com.example.cloud_file_storage.modules.minio.exception.DirectoryOrFileNotFound;
import com.example.cloud_file_storage.modules.minio.exception.InvalidPathException;
import com.example.cloud_file_storage.modules.minio.resource.service.*;
import io.minio.errors.*;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
@RequestMapping("/resource")
public class ResourceController {

    private final ResourceInfoService resourceInfoService;
    private final ResourceDeleteService resourceDeleteService;
    private final ResourceMoveService resourceMoveService;
    private final ResourceDownloadService resourceDownloadService;
    private final ResourceSearchService resourceSearchService;

    public ResourceController(ResourceInfoService resourceInfoService, ResourceDeleteService resourceDeleteService, ResourceMoveService resourceMoveService, ResourceDownloadService resourceDownloadService, ResourceSearchService resourceSearchService) {
        this.resourceInfoService = resourceInfoService;
        this.resourceDeleteService = resourceDeleteService;
        this.resourceMoveService = resourceMoveService;
        this.resourceDownloadService = resourceDownloadService;
        this.resourceSearchService = resourceSearchService;
    }


    @GetMapping
    public ResponseEntity<?> getResourceInfo(@RequestParam("path") String path, @AuthenticationPrincipal User user) throws DirectoryOrFileNotFound, InvalidPathException {
        MinioDto minioDto = resourceInfoService.getResourceInfo(path, user.getId());
        return ResponseEntity.ok(minioDto);
    }


    @DeleteMapping
    public ResponseEntity<?> deleteResource(@RequestParam("path") String path, @AuthenticationPrincipal User user) throws Exception {
        resourceDeleteService.deleteResource(path, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/move")
    public ResponseEntity<?> moveResource(@RequestParam("from") String from, @RequestParam("to") String to, @AuthenticationPrincipal User user) throws Exception {
        MinioDto minioDto = resourceMoveService.moveOrRenameResource(from, to, user.getId());
        return ResponseEntity.ok(minioDto);
    }

    @GetMapping("/download")
    public ResponseEntity<?> downloadResource(@RequestParam String path, @AuthenticationPrincipal User user) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InvalidPathException, InternalException {
        Resource resource = resourceDownloadService.downloadResource(path, user.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename())
                .body(resource);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchResource(@RequestParam String query, @AuthenticationPrincipal User user) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<MinioDto> searchResult = resourceSearchService.search(query, user.getId());
        return ResponseEntity.ok(searchResult);
    }


}
