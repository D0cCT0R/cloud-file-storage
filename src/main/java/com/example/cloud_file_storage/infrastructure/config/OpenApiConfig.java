package com.example.cloud_file_storage.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cloudFileStorageOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cloud File Storage Api")
                        .description("Rest Api cloud file storage")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Nikita")
                                .email("ershov.2004416@gmail.com")));
    }
}
