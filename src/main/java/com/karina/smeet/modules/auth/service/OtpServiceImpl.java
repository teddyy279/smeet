package com.karina.smeet.modules.auth.service;

import com.karina.smeet.common.exception.AppException;
import com.karina.smeet.common.exception.ErrorCode;
import com.karina.smeet.entity.postgre.User;
import com.karina.smeet.entity.postgre.UserAuthProvider;
import com.karina.smeet.enums.Provider;
import com.karina.smeet.modules.auth.dto.request.OtpRequest;
import com.karina.smeet.modules.auth.dto.request.ResetPasswordRequest;
import com.karina.smeet.modules.auth.repository.UserAuthProviderRepository;
import com.karina.smeet.modules.notification.facade.NotificationFacade;
import com.karina.smeet.modules.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class OtpServiceImpl implements OtpService{

    RedisTemplate<String, Object> redisTemplate;
    UserRepository userRepository;
    UserAuthProviderRepository userAuthProviderRepository;
    NotificationFacade notificationFacade;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void sendOtp(OtpRequest request) {
        String lockKey = "otp_lock:" + request.email();

        if(!userRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        if(Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new AppException(ErrorCode.OTP_TOO_MANY_REQUESTS);
        }

        String otp = String.format("%06d", new SecureRandom().nextInt(1000000));

        String redisKey = "otp:" + request.email();
        redisTemplate.opsForValue().set(redisKey, otp, 2, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(lockKey, "true", 60, TimeUnit.SECONDS);

        notificationFacade.sendOtpEmail(request.email(), otp);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String redisKey = "otp:" + request.email();

        String savedOtp = (String) redisTemplate.opsForValue().get(redisKey);

        if(savedOtp == null) {
            throw new AppException(ErrorCode.EXPIRED_OTP);
        }

        if(!savedOtp.equals(request.otp())) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));


        UserAuthProvider localProvider = userAuthProviderRepository.findByUserAndProvider(user, Provider.LOCAL)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_PROVIDER_NOT_FOUND));

        localProvider.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userAuthProviderRepository.save(localProvider);
        redisTemplate.delete(redisKey);
    }
}
