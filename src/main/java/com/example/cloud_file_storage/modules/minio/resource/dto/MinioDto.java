package com.example.cloud_file_storage.modules.minio.resource.dto;

public record MinioDto (
        String path,
        String name,
        Long size,
        ResourceType type
) {}


