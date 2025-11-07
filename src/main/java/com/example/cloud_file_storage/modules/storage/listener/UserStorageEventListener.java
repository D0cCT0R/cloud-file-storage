package com.example.cloud_file_storage.modules.storage.listener;


import com.example.cloud_file_storage.modules.auth.event.UserRegisteredEvent;
import com.example.cloud_file_storage.modules.storage.service.directory.DirectoryCreationService;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;



@Component
@Slf4j
public class UserStorageEventListener {

    private final DirectoryCreationService creationService;

    @Autowired
    public UserStorageEventListener(DirectoryCreationService creationService) {
        this.creationService = creationService;
    }

    @EventListener
    @Async
    public void onUserRegistered(UserRegisteredEvent event) {
        try {
            creationService.createUserDirectory(event.getUserId());
        } catch (Exception e) {
            log.error("Failed to create user directory for user {}", event.getUserId(), e);
        }
    }
}


