package com.example.cloud_file_storage.service.storage.resource;

import com.example.cloud_file_storage.exception.storage.DirectoryOrFileNotFoundException;
import com.example.cloud_file_storage.exception.storage.MinioIsNotAvailableException;
import com.example.cloud_file_storage.dto.storage.DownloadResult;
import com.example.cloud_file_storage.dto.storage.FileData;
import com.example.cloud_file_storage.exception.storage.InvalidPathException;
import com.example.cloud_file_storage.util.MinioHelper;
import com.example.cloud_file_storage.util.PathResolverService;
import com.example.cloud_file_storage.util.UserPathResolver;
import com.example.cloud_file_storage.util.ZipService;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ResourceDownloadService {
    private final ZipService zipService;
    private final MinioHelper minioHelper;
    private final PathResolverService resolverService;
    private final UserPathResolver resolver;

    @Autowired
    public ResourceDownloadService(ZipService zipService, MinioHelper minioHelper,
                                   PathResolverService resolverService, UserPathResolver resolver) {
        this.zipService = zipService;
        this.minioHelper = minioHelper;
        this.resolverService = resolverService;
        this.resolver = resolver;
    }
    @WithSpan
    public DownloadResult downloadResource(String userPath, Long userId) {
        try {
            log.info("Download resource for user.Path: {} , userID: {}", userPath, userId);
            String fullPath = resolver.resolveFullPath(userPath, userId);
            if (!minioHelper.objectExist(fullPath)) {
                throw new DirectoryOrFileNotFoundException("Directory or file not found");
            }
            if (minioHelper.isDirectory(fullPath)) {
                return downloadDirectory(fullPath, userPath);
            } else {
                return downloadFile(fullPath, userPath);
            }
        } catch (InvalidPathException | DirectoryOrFileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new MinioIsNotAvailableException("Minio is not available", e);
        }
    }
    @WithSpan
    private DownloadResult downloadFile(String fullPath, String userPath) throws Exception {
        InputStream file = minioHelper.downloadFile(fullPath);
        String filename = Paths.get(userPath).getFileName().toString();
        StreamingResponseBody streamingBody = outputStream -> {
            try (file) {
                file.transferTo(outputStream);
            }
        };

        return new DownloadResult(filename, streamingBody);
    }
    @WithSpan
    private DownloadResult downloadDirectory(String fullPath, String userPath) throws Exception {
        List<FileData> filesInDirectory = minioHelper.downloadAllFileInDirectory(fullPath);
        List<String> relativePaths = resolverService.getRelativePathsForZip(
                filesInDirectory.stream().map(FileData::path).collect(Collectors.toList()),
                fullPath
        );
        String fileName = Paths.get(userPath).getFileName() + ".zip";
        StreamingResponseBody strBody = zipService.createZip(filesInDirectory, relativePaths);
        log.debug("Download directory complete successfully. Path: {}", fullPath);
        return new DownloadResult(fileName, strBody);
    }
}


