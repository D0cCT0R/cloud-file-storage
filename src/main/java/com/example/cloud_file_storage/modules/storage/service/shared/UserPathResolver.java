package com.example.cloud_file_storage.modules.storage.service.shared;

import com.example.cloud_file_storage.modules.storage.exception.InvalidPathException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserPathResolver {

    private final ResourceValidationService validationService;
    private final PathResolverService resolverService;
    private final UserPathService pathService;

    @Autowired
    public UserPathResolver(ResourceValidationService validationService,
                            PathResolverService resolverService,
                            UserPathService pathService) {
        this.validationService = validationService;
        this.resolverService = resolverService;
        this.pathService = pathService;
    }

    public String resolveFullPath(String userPath, Long userId) throws InvalidPathException {
        validationService.validateUserPath(userPath);
        String normalizedUserPath = resolverService.normalizeUserPath(userPath);
        return pathService.toFullPath(userId, normalizedUserPath);
    }
}


