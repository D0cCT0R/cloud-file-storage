package com.example.cloud_file_storage.modules.storage.controller;

import com.example.cloud_file_storage.infrastructure.security.CustomUserDetails;
import com.example.cloud_file_storage.modules.storage.dto.storage.MinioDto;
import com.example.cloud_file_storage.modules.storage.exception.DirectoryOrFileNotFound;
import com.example.cloud_file_storage.modules.storage.exception.InvalidPathException;
import com.example.cloud_file_storage.modules.storage.dto.resource.DownloadResult;
import com.example.cloud_file_storage.modules.storage.service.resource.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@Tag(name = "Resource API", description = "Operation with files and directories")
@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    private final ResourceInfoService resourceInfoService;
    private final ResourceDeleteService resourceDeleteService;
    private final ResourceMoveService resourceMoveService;
    private final ResourceDownloadService resourceDownloadService;
    private final ResourceSearchService resourceSearchService;
    private final ResourceUploadService uploadService;

    @Autowired
    public ResourceController(ResourceInfoService resourceInfoService, ResourceDeleteService resourceDeleteService, ResourceMoveService resourceMoveService, ResourceDownloadService resourceDownloadService, ResourceSearchService resourceSearchService, ResourceUploadService uploadService) {
        this.resourceInfoService = resourceInfoService;
        this.resourceDeleteService = resourceDeleteService;
        this.resourceMoveService = resourceMoveService;
        this.resourceDownloadService = resourceDownloadService;
        this.resourceSearchService = resourceSearchService;
        this.uploadService = uploadService;
    }

    @Operation(summary = "Get resource Info", description = "Getting resource info and return it", responses = {
            @ApiResponse(responseCode = "200", description = "Getting information complete successfully"),
            @ApiResponse(responseCode = "400", description = "Not valid path"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @GetMapping
    public ResponseEntity<MinioDto> getResourceInfo(@RequestParam("path") String path, @AuthenticationPrincipal CustomUserDetails user) throws DirectoryOrFileNotFound, InvalidPathException {
        MinioDto minioDto = resourceInfoService.getResourceInfo(path, user.getId());
        return ResponseEntity.ok(minioDto);
    }

    @Operation(summary = "Delete resource", description = "Delete resource, no return", responses = {
            @ApiResponse(responseCode = "204", description = "Delete resource complete successfully"),
            @ApiResponse(responseCode = "400", description = "Not valid path"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @DeleteMapping
    public ResponseEntity<?> deleteResource(@RequestParam("path") String path, @AuthenticationPrincipal CustomUserDetails user) throws Exception {
        resourceDeleteService.deleteResource(path, user.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Move or Rename resource", description = "Move or Rename resource and return it", responses = {
            @ApiResponse(responseCode = "200", description = "Move or Rename resource complete successfully"),
            @ApiResponse(responseCode = "400", description = "Not valid path"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @GetMapping("/move")
    public ResponseEntity<MinioDto> moveResource(@RequestParam("from") String from, @RequestParam("to") String to, @AuthenticationPrincipal CustomUserDetails user) throws Exception {
        MinioDto minioDto = resourceMoveService.moveOrRenameResource(from, to, user.getId());
        return ResponseEntity.ok(minioDto);
    }

    @Operation(summary = "Upload resource", description = "Upload file or directory and return uploaded files list", responses = {
            @ApiResponse(responseCode = "201", description = "Upload resource complete successfully"),
            @ApiResponse(responseCode = "400", description = "Not valid body"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "409", description = "File or Directory already exists"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<MinioDto>> uploadResource(@RequestPart("object") List<MultipartFile> files, @RequestParam("path") String path, @AuthenticationPrincipal CustomUserDetails user) throws Exception {
        List<MinioDto> uploadFiles = uploadService.upload(path, user.getId(), files);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadFiles);
    }

    @Operation(summary = "Download resource", description = "Download directory or file and return StreamingResponseBody", responses = {
            @ApiResponse(responseCode = "200", description = "Download resource complete successfully"),
            @ApiResponse(responseCode = "400", description = "Not valid path"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadResource(@RequestParam String path, @AuthenticationPrincipal CustomUserDetails user) throws InvalidPathException, DirectoryOrFileNotFound {
        DownloadResult object = resourceDownloadService.downloadResource(path, user.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + object.fileName())
                .header("Content-Transfer-Encoding", "binary")
                .body(object.body());
    }

    @Operation(summary = "Search resource", description = "Search resource and return search result", responses = {
            @ApiResponse(responseCode = "200", description = "Search resource complete successfully"),
            @ApiResponse(responseCode = "400", description = "Not valid search query"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @GetMapping("/search")
    public ResponseEntity<List<MinioDto>> searchResource(@RequestParam String query, @AuthenticationPrincipal CustomUserDetails user) {
        List<MinioDto> searchResult = resourceSearchService.search(query, user.getId());
        return ResponseEntity.ok(searchResult);
    }
}
