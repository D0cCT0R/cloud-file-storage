package com.example.cloud_file_storage.modules.storage.dto.resource;

import java.io.InputStream;

public record FileData (
        String path,
        InputStream stream,
        Long size
) {
}


