package com.collabrium.service;

import com.collabrium.dto.AuthRequest;
import com.collabrium.dto.AuthResponse;
import com.collabrium.dto.RegisterRequest;
import com.collabrium.exception.ApiException;
import com.collabrium.model.OtpType;
import com.collabrium.model.RefreshToken;
import com.collabrium.model.User;
import com.collabrium.repository.UserRepository;
import com.collabrium.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final OtpService otpService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, 
            JwtUtil jwtUtil, RefreshTokenService refreshTokenService, OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.otpService = otpService;
    }

    @Transactional
    @SuppressWarnings("null")
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("An account with this email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isEmailVerified(false)
                .is2faEnabled(false)
                .build();

        userRepository.save(user);

        otpService.generateAndSendOtp(user.getEmail(), OtpType.REGISTRATION);

        return AuthResponse.builder()
                .requiresOtp(true)
                .build();
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw ApiException.unauthorized("Invalid email or password");
            }

            if (!user.isEmailVerified()) {
                otpService.generateAndSendOtp(user.getEmail(), OtpType.REGISTRATION);
                throw ApiException.unauthorized("Email not verified. A new OTP has been sent to your email.");
            }

            if (user.is2faEnabled()) {
                otpService.generateAndSendOtp(user.getEmail(), OtpType.LOGIN);
                return AuthResponse.builder()
                        .requiresOtp(true)
                        .build();
            }

            return generateTokensForUser(user);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ApiException("Authentication service error: " + ex.getMessage(), org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public AuthResponse verifyRegistrationOtp(String email, String otpCode) {
        otpService.validateOtp(email, otpCode, OtpType.REGISTRATION);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("User not found"));

        user.setEmailVerified(true);
        userRepository.save(user);

        return generateTokensForUser(user);
    }

    @Transactional
    public AuthResponse verifyLoginOtp(String email, String otpCode) {
        otpService.validateOtp(email, otpCode, OtpType.LOGIN);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("User not found"));

        return generateTokensForUser(user);
    }

    private AuthResponse generateTokensForUser(User user) {
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .requiresOtp(false)
                .build();
    }
}
