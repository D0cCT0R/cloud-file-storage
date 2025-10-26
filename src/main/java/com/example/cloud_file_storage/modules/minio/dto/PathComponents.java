package com.example.cloud_file_storage.modules.minio.dto;

public record PathComponents (
        String parentPath,
        String name
) {
}
