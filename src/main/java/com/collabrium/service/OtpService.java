package com.collabrium.service;

import com.collabrium.exception.ApiException;
import com.collabrium.model.Otp;
import com.collabrium.model.OtpType;
import com.collabrium.repository.OtpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpRepository otpRepository;
    private final EmailService emailService;

    public OtpService(OtpRepository otpRepository, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void generateAndSendOtp(String email, OtpType type) {
        // Delete any existing OTPs for this email/type before generating new one
        otpRepository.deleteAllByEmailAndType(email, type);

        String otpCode = String.format("%06d", RANDOM.nextInt(1_000_000));
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        Otp otp = new Otp(email, otpCode, type, expiryDate);
        otpRepository.save(otp);

        String purpose = (type == OtpType.REGISTRATION) ? "Email Verification" : "Login (2FA)";
        emailService.sendOtpEmail(email, otpCode, purpose);
    }

    @Transactional
    public void validateOtp(String email, String otpCode, OtpType type) {
        Otp otp = otpRepository.findTopByEmailAndTypeOrderByIdDesc(email, type)
                .orElseThrow(() -> ApiException.badRequest("No OTP found. Please request a new one."));

        if (LocalDateTime.now().isAfter(otp.getExpiryDate())) {
            otpRepository.deleteAllByEmailAndType(email, type);
            throw ApiException.badRequest("OTP has expired. Please request a new one.");
        }

        if (!otp.getOtpCode().equals(otpCode)) {
            throw ApiException.badRequest("Invalid OTP. Please try again.");
        }

        // OTP is valid — delete it so it can't be reused
        otpRepository.deleteAllByEmailAndType(email, type);
    }
}
