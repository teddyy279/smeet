package com.karina.smeet.modules.chat.service;


import com.karina.smeet.common.exception.AppException;
import com.karina.smeet.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class ChatMediaService {
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private static final long MAX_VIDEO_SIZE_BYTES = 50 * 1024 * 1024;
    private static final long MAX_DOCUMENT_SIZE_BYTES = 20 * 1024 * 1024;
    private static final long MAX_AUDIO_SIZE_BYTES    = 10 * 1024 * 1024;

    private static final Set<String> VIDEO_TYPES = Set.of(
            "video/mp4", "video/quicktime", "video/webm"
    );

    private static final Set<String> DOCUMENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",// .docx
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",// .xlsx
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    );

    private static final Set<String> AUDIO_TYPES = Set.of(
            "audio/webm", "audio/mpeg", "audio/mp4", "audio/ogg", "audio/wav"
    );

    public String upload(MultipartFile file, UUID uploaderId) {
        validate(file);

        String key = buildKey(uploaderId, file.getOriginalFilename());

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            String url = String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
            log.info("File uploaded to S3: {}, url");
            return url;
        } catch (IOException exception) {
            log.error("S3 upload failed: {}", exception.getMessage());
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        String contentType = file.getContentType();

        if (VIDEO_TYPES.contains(contentType)) {
            if (file.getSize() > MAX_VIDEO_SIZE_BYTES)
                throw new AppException(ErrorCode.FILE_TOO_LARGE);

        } else if (DOCUMENT_TYPES.contains(contentType)) {
            if (file.getSize() > MAX_DOCUMENT_SIZE_BYTES)
                throw new AppException(ErrorCode.FILE_TOO_LARGE);

        } else if (AUDIO_TYPES.contains(contentType)) {
            if (file.getSize() > MAX_AUDIO_SIZE_BYTES)
                throw new AppException(ErrorCode.FILE_TOO_LARGE);

        } else {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    // key dạng: text/{uploader Id}/{uuid}-{tên gốc}
    private String buildKey(UUID uploaderId, String originalFilename) {
        String safeName = originalFilename != null
                ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_")
                : "file";
        return "chat/" + uploaderId + "/" + UUID.randomUUID() + "-" + safeName;
    }
}
