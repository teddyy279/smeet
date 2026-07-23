package com.karina.smeet.modules.chat.dto.response;

import lombok.Builder;

@Builder
public record MediaUploadResponse(
        String url,
        String fileName,
        String contentType
) {}
