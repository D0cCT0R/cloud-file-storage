package com.example.cloud_file_storage.modules.storage.service.resource;

import com.example.cloud_file_storage.modules.storage.exception.DirectoryOrFileNotFound;
import com.example.cloud_file_storage.modules.storage.exception.MinioIsNotAvailable;
import com.example.cloud_file_storage.modules.storage.dto.resource.DownloadResult;
import com.example.cloud_file_storage.modules.storage.dto.resource.FileData;
import com.example.cloud_file_storage.modules.storage.service.shared.*;
import com.example.cloud_file_storage.modules.storage.exception.InvalidPathException;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

    public DownloadResult downloadResource(String userPath, Long userId) throws DirectoryOrFileNotFound, InvalidPathException {
        try {
            log.info("Download resource for user.Path: {} , userID: {}", userPath, userId);
            String fullPath = resolver.resolveFullPath(userPath, userId);
            if (!minioHelper.objectExist(fullPath)) {
                throw new DirectoryOrFileNotFound("Directory or file not found");
            }
            if (minioHelper.isDirectory(fullPath)) {
                return downloadDirectory(fullPath, userPath);
            } else {
                return downloadFile(fullPath, userPath);
            }
        } catch (InvalidPathException | DirectoryOrFileNotFound e) {
            throw e;
        } catch (Exception e) {
            throw new MinioIsNotAvailable("Minio is not available", e);
        }
    }

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

    private DownloadResult downloadDirectory(String fullPath, String userPath) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
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
