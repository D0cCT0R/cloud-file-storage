package com.example.cloud_file_storage.dto.storage;

import java.io.InputStream;

public record FileData (
        String path,
        InputStream stream,
        Long size
) {
}


