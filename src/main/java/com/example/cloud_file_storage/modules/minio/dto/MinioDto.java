package com.example.cloud_file_storage.modules.minio.dto;

import lombok.Builder;

@Builder
public record MinioDto (
        String path,
        String name,
        Long size,
        ResourceType type
) {}


