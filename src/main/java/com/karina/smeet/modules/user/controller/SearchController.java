package com.karina.smeet.modules.user.controller;

import com.cloudinary.Api;
import com.karina.smeet.common.response.ApiResponse;
import com.karina.smeet.modules.user.dto.response.SearchResponse;
import com.karina.smeet.modules.user.service.RecentSearchService;
import com.karina.smeet.modules.user.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/search")
public class SearchController {
    UserService userService;
    RecentSearchService recentSearchService;

    @GetMapping
    public ApiResponse<SearchResponse> search(
            @RequestParam String q,
            @AuthenticationPrincipal UUID currentUserId) {
        return ApiResponse.<SearchResponse>builder()
                .result(userService.search(q, currentUserId))
                .build();
    }

    @GetMapping("/global")
    public ApiResponse<SearchResponse> searchGlobal(
            @RequestParam String q,
            @AuthenticationPrincipal UUID currentUserId) {
        return ApiResponse.<SearchResponse>builder()
                .result(userService.searchGlobal(q, currentUserId))
                .build();
    }

    @GetMapping("/recent")
    public ApiResponse<List<RecentSearchService.RecentSearchItem>> getRecent(
            @AuthenticationPrincipal UUID currentUserId) {
        return ApiResponse.<List<RecentSearchService.RecentSearchItem>>builder()
                .result(recentSearchService.getRecent(currentUserId))
                .build();
    }

    @DeleteMapping("/recent/{targetId}")
    public ApiResponse<Void> removeRecent(
            @PathVariable String targetId,
            @AuthenticationPrincipal UUID currentUserId) {
        recentSearchService.remove(currentUserId, targetId);
        return ApiResponse.<Void>builder()
                .message("Search history deleted")
                .build();
    }

    @DeleteMapping("/recent")
    public ApiResponse<Void> clearRecent(@AuthenticationPrincipal UUID currentUserId) {
        recentSearchService.clear(currentUserId);
        return ApiResponse.<Void>builder()
                .message("All search history cleared")
                .build();
    }

    @PostMapping("/select/{userId}")
    public ApiResponse<Void> selectResult(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UUID currentUserId) {
        userService.selectSearchResult(currentUserId, userId);
        return ApiResponse.<Void>builder()
                .message("Search history saved")
                .build();
    }
}