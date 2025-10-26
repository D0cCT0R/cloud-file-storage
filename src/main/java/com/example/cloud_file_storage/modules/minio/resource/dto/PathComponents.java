package com.example.cloud_file_storage.modules.minio.resource.dto;

public record PathComponents (
        String parentPath,
        String name
) {
}
