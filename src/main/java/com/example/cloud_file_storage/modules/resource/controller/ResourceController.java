package com.example.cloud_file_storage.modules.resource.controller;

import com.example.cloud_file_storage.modules.auth.entity.User;
import com.example.cloud_file_storage.modules.resource.dto.MinioDto;
import com.example.cloud_file_storage.modules.resource.exception.DirectoryOrFileNotFound;
import com.example.cloud_file_storage.modules.resource.exception.InvalidPathException;
import com.example.cloud_file_storage.modules.resource.service.ResourceDeleteService;
import com.example.cloud_file_storage.modules.resource.service.ResourceInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/resource")
public class ResourceController {

    private final ResourceInfoService resourceInfoService;
    private final ResourceDeleteService resourceDeleteService;

    public ResourceController(ResourceInfoService resourceInfoService, ResourceDeleteService resourceDeleteService) {
        this.resourceInfoService = resourceInfoService;
        this.resourceDeleteService = resourceDeleteService;
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
    public ResponseEntity<?> moveResource(@RequestParam("from") String from, @RequestParam("to") String to, @AuthenticationPrincipal User user) {

    }

}
