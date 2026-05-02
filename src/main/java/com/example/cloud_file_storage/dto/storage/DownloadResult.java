package com.example.cloud_file_storage.dto.storage;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public record DownloadResult (
        String fileName,
        StreamingResponseBody body
) {
}


