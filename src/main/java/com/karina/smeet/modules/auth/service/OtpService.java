package com.karina.smeet.modules.auth.service;

import com.karina.smeet.modules.auth.dto.request.OtpRequest;
import com.karina.smeet.modules.auth.dto.request.ResetPasswordRequest;

public interface OtpService {
    void sendOtp(OtpRequest request);

    void resetPassword(ResetPasswordRequest request);
}
