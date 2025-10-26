package com.example.cloud_file_storage.modules.minio.service;


import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
public class ZipService {
    //TODO сделать данный метод более эффективным
    public FileSystemResource createZip(List<InputStreamResource> files, List<String> relativePaths) throws IOException {
        Path tempZipFile = Files.createTempFile("download_", ".zip");
        try(ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(tempZipFile.toFile()))) {
              for(int i = 0; i < files.size(); i++) {
                  InputStream file =  files.get(i).getInputStream();
                  String relativePath = relativePaths.get(i);
                  ZipEntry zipEntry = new ZipEntry(relativePath);
                  zipOut.putNextEntry(zipEntry);
                  file.transferTo(zipOut);
                  zipOut.closeEntry();
                  file.close();
              }
        }
        return new FileSystemResource(tempZipFile.toFile());
    }
}
