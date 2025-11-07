package com.example.cloud_file_storage.modules.storage.service.shared;


import com.example.cloud_file_storage.modules.storage.dto.resource.FileData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class ZipService {
    public StreamingResponseBody createZip(List<FileData> files, List<String> relativePaths) {
        return outputStream ->  {
                try(ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                    for(int i = 0; i < files.size(); i++) {
                        FileData file = files.get(i);
                        String relativePath = relativePaths.get(i);
                        try(InputStream fileStream = file.stream()) {
                            ZipEntry entry = new ZipEntry(relativePath);
                            zipOut.putNextEntry(entry);
                            fileStream.transferTo(zipOut);
                            zipOut.closeEntry();
                        } catch (Exception e) {
                            log.error("Error processing file. Path: {}", file.path());
                        }
                    }
                    zipOut.finish();
            }
        };
    }
}


