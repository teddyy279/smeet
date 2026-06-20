package com.karina.smeet.modules.user.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.karina.smeet.common.exception.AppException;
import com.karina.smeet.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class AvatarService {
    private final Cloudinary cloudinary;

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    public String uploadAvatar(UUID userId, MultipartFile file) {
        validate(file);

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "smeet/avatars",
                            "public_id", userId.toString(),
                            "overwrite", true,
                            "transformation", ObjectUtils.asMap(
                                    "width", 256,
                                    "height", 256,
                                    "crop", "fill",
                                    "gravity", "face"
                            )
                    )
            );

            String url = (String) result.get("secure_url");
            log.info("Avatar uploaded for user {}: {}", userId, url);
            return url;

        } catch (IOException e) {
            log.error("Avatar upload failed for user {}: {}", userId, e.getMessage());
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        if(!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }
    }
}
