package com.example.cloud_file_storage.modules.storage.controller;

import com.example.cloud_file_storage.common.security.CustomUserDetails;
import com.example.cloud_file_storage.modules.storage.service.directory.DirectoryCreationService;
import com.example.cloud_file_storage.modules.storage.service.directory.DirectoryInfoService;
import com.example.cloud_file_storage.modules.storage.dto.storage.MinioDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Directory API", description = "Creating directory and getting directory content")
@RestController
@RequestMapping("/api/directory")
public class DirectoryController {

    private final DirectoryInfoService directoryInfoService;
    private final DirectoryCreationService directoryCreationService;

    @Autowired
    public DirectoryController(DirectoryInfoService directoryInfoService, DirectoryCreationService directoryCreationService) {
        this.directoryInfoService = directoryInfoService;
        this.directoryCreationService = directoryCreationService;
    }

    @Operation(summary = "Get directory content", description = "Getting directory content and return it", responses = {
            @ApiResponse(responseCode = "200", description = "Getting information complete successfully"),
            @ApiResponse(responseCode = "400", description = "Not valid or missing path"),
            @ApiResponse(responseCode = "401", description = "User not authentication"),
            @ApiResponse(responseCode = "404", description = "Directory not found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MinioDto> getDirectoryContent(@RequestParam String path, @AuthenticationPrincipal CustomUserDetails user) {
        return directoryInfoService.getDirectoryInfo(path, user.getId());
    }

    @Operation(summary = "Create directory", description = "Create directory and return it", responses = {
            @ApiResponse(responseCode = "201", description = "Create directory complete successfully"),
            @ApiResponse(responseCode = "400", description = "Not valid or missing path"),
            @ApiResponse(responseCode = "401", description = "User not authentication"),
            @ApiResponse(responseCode = "404", description = "Parent path not found"),
            @ApiResponse(responseCode = "409", description = "Directory already exist"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MinioDto createFolder(@RequestParam String path, @AuthenticationPrincipal CustomUserDetails user) {
        return directoryCreationService.createDirectory(path, user.getId());
    }
}


