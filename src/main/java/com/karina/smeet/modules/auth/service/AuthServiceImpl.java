package com.karina.smeet.modules.auth.service;


import com.karina.smeet.common.exception.AppException;
import com.karina.smeet.common.exception.ErrorCode;
import com.karina.smeet.common.utils.JwtUtil;
import com.karina.smeet.entity.postgre.RefreshToken;
import com.karina.smeet.entity.postgre.User;
import com.karina.smeet.entity.postgre.UserAuthProvider;
import com.karina.smeet.enums.Provider;
import com.karina.smeet.modules.auth.dto.request.LoginRequest;
import com.karina.smeet.modules.auth.dto.request.RegisterRequest;
import com.karina.smeet.modules.auth.dto.response.AuthResponse;
import com.karina.smeet.modules.auth.dto.response.RefreshTokenResponse;
import com.karina.smeet.modules.auth.repository.RefreshTokenRepository;
import com.karina.smeet.modules.auth.repository.UserAuthProviderRepository;
import com.karina.smeet.modules.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.karina.smeet.common.exception.ErrorCode.INVALID_CREDENTIALS;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class AuthServiceImpl implements AuthService{
    UserRepository userRepository;
    UserAuthProviderRepository userAuthProviderRepository;
    TokenService tokenService;
    JwtUtil jwtUtil;
    PasswordEncoder passwordEncoder;
    RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                //.password(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .build();

        userRepository.save(user);

        UserAuthProvider authProvider = UserAuthProvider.builder()
                .user(user)
                .provider(Provider.LOCAL)
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        userAuthProviderRepository.save(authProvider);

        tokenService.createRefreshToken(user, response);

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        String identifer = request.identifier();

        User user = identifer.contains("@")
                ? userRepository.findByEmail(identifer)
                    .orElseThrow(() -> new AppException(INVALID_CREDENTIALS))
                : userRepository.findByUsername(identifer)
                    .orElseThrow(() -> new AppException(INVALID_CREDENTIALS));

        UserAuthProvider authProvider = userAuthProviderRepository
                .findByUserAndProvider(user, Provider.LOCAL)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if(!passwordEncoder.matches(request.password(), authProvider.getPasswordHash())) {
            throw new AppException(INVALID_CREDENTIALS);
        }

        //var accessToken = jwtUtil.generateAccessToken(user.getId(),user.getEmail());
        tokenService.createRefreshToken(user, response);

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(HttpServletRequest request) {
        RefreshToken refreshToken = tokenService.validateRefreshToken(request);

        User user = refreshToken.getUser();

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());

        return new RefreshTokenResponse(accessToken, jwtUtil.getAccessTokenExpiry());
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        tokenService.deleteRefreshToken(request, response);
    }

    @Override
    public void logoutAll(UUID userId, HttpServletResponse response) {
        tokenService.deleteAllRefreshTokens(userId, response);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .accessToken(accessToken)
                .avatarUrl(user.getAvatarUrl())
                .accessTokenExpiresIn(jwtUtil.getAccessTokenExpiry())
                .build();
    }
}
