package com.example.cloud_file_storage.modules.minio.resource.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.InputStreamResource;

import java.util.List;

@Data
@AllArgsConstructor
public class DownloadDirectoryDto {
    private List<String> allFileNameInDirectory;
    private List<InputStreamResource> allFileInDirectory;
}
