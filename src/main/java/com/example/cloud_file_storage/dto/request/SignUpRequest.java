package com.example.cloud_file_storage.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "Поле username не должно быть пустым")
        @Size(min = 3, max = 50, message = "Поле username должно состоять от 4 до 50 символов")
        String username,
        @NotBlank(message = "Поле password не должно быть пустым")
        @Size(min = 5, max = 30, message = "Поле password должно состоять от 6 до 30 символов")
        String password
) {
}
