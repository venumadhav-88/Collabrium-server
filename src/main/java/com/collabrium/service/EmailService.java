package com.collabrium.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otpCode, String purpose) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Collabrium - Your OTP for " + purpose);
        message.setText(
            "Hello,\n\n" +
            "Your One-Time Password (OTP) for " + purpose + " is:\n\n" +
            "  " + otpCode + "\n\n" +
            "This OTP is valid for 10 minutes. Do not share it with anyone.\n\n" +
            "If you did not request this, please ignore this email.\n\n" +
            "Regards,\nThe Collabrium Team"
        );
        mailSender.send(message);
    }
}
