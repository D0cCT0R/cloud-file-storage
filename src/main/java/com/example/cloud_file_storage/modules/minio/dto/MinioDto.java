package com.example.cloud_file_storage.modules.minio.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record MinioDto (
        String path,
        String name,
        Long size,
        ResourceType type
) {}


