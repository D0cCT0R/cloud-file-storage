package com.example.cloud_file_storage.service.storage.directory.integration;


import com.example.cloud_file_storage.service.storage.directory.DirectoryCreationService;
import com.example.cloud_file_storage.util.MinioHelper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DirectoryCreationServiceTest {

    @Container
    static GenericContainer<?> minio = new GenericContainer<>(DockerImageName.parse("minio/minio:latest"))
            .withEnv("MINIO_ROOT_USER", "test123456")
            .withEnv("MINIO_ROOT_PASSWORD", "test123456")
            .withCommand("server /data --console-address :9001")
            .withExposedPorts(9000)
            .waitingFor(Wait.forHttp("/minio/health/live")
                    .forPort(9000)
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(2)))
            .withLogConsumer(outputFrame -> System.out.print(outputFrame.getUtf8String()));

    @DynamicPropertySource
    static void registerProperty(DynamicPropertyRegistry registry) {
        registry.add("minio.access-key", () -> "test123456");
        registry.add("minio.secret-key", () -> "test123456");
        registry.add("minio.url", () -> String.format("http://%s:%d", minio.getHost(), minio.getMappedPort(9000)));
        registry.add("minio.bucket-name", () -> "cloud-storage");
    }

    @Autowired
    private DirectoryCreationService creationService;

    @Autowired
    private MinioHelper minioHelper;

    @Test
    @SneakyThrows
    void createDirectory_success() {
        creationService.createDirectory("test/", 3L);
        assertTrue(minioHelper.objectExist("user-3-files/test/"));
    }
}
