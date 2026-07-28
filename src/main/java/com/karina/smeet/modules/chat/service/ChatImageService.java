package com.karina.smeet.modules.chat.service;

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

public class ChatImageService {
    private final Cloudinary cloudinary;

    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    public String upload(MultipartFile file, UUID uploaderId) {
        validate(file);

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "smeet/chat",
                            "public_id", UUID.randomUUID().toString(),
                            "overwrite", false,
                            "transformation", ObjectUtils.asMap(
                                    "width", 1600,
                                    "height", 1600,
                                    "crop", "limit"
                            )
                    )
            );

            String url = (String) result.get("secure_url");
            log.info("Chat image uploaded by {}: {}", uploaderId, url);
            return url;

        } catch (IOException exception) {
            log.error("Cloudinary upload failed for user {}: {}", uploaderId, exception.getMessage());

            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new AppException(ErrorCode.FILE_EMPTY);

        if (file.getSize() > MAX_SIZE_BYTES)
            throw new AppException(ErrorCode.FILE_TOO_LARGE);

        if (!ALLOWED_TYPES.contains(file.getContentType()))
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
    }
}
